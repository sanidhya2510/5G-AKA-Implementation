package Implementation.protocol.entities;

import Implementation.App;
import Implementation.helper.Converter;
import Implementation.protocol.additional.KDF;
import Implementation.protocol.additional.SIDF;
import Implementation.protocol.data.Data_5G_HE_AV;
import Implementation.protocol.data.Data_AUTN;
import Implementation.protocol.additional.AVGenerator;
import Implementation.protocol.messages.Authentication_Information;
import Implementation.protocol.messages.Nudm_Authentication_Get_Response;
import Implementation.protocol.messages.Nudm_UEAuthentication_Get_Request;
import Implementation.structure.Entity;
import Implementation.structure.Message;

import java.security.PrivateKey;

public class UDM extends Entity {

    private final byte[] K;
    private final byte[] AMF;

    private final PrivateKey privateKey;

    public UDM(byte[] K, byte[] AMF, PrivateKey privateKey) {
        this.K = K;
        this.AMF = AMF;
        this.privateKey = privateKey;
    }

    @Override
    public String getName() {
        return "UDM";
    }

    @Override
    public void onReceiveMessage(Message message, Entity sender) {

        //step 3 receive
        if (message instanceof Nudm_UEAuthentication_Get_Request && sender instanceof AUSF) {
            //Received Nudm_UEAuthentication_ Get Request
            Nudm_UEAuthentication_Get_Request getRequest = (Nudm_UEAuthentication_Get_Request) message;
            AUSF ausf = (AUSF) sender;

            //print for step 3
            System.out.println(ausf.getName() + " -> " + getName() + " : " + getRequest.getName());
            
            //step 4 calculation
            Data_5G_HE_AV AV = generateAVsAndInvokeSIDF(getRequest, ausf);

            Nudm_Authentication_Get_Response authInfoResp = getGetResponse(getRequest, ausf, AV);

            //step 5 sending
            sendMessage(authInfoResp, ausf);
        } else if (message instanceof Authentication_Information && sender instanceof AUSF) {
            //Received Authentication Information
            Authentication_Information authInformation = (Authentication_Information) message;
            AUSF ausf = (AUSF) sender;
            System.out.println(ausf.getName() + " -> " + getName() + " : " + authInformation.getName());
                if (authInformation.authenticationSuccessful) {
                    System.out.println("  " + getName() + " is considering the authentication as successful.");
                } else {
                    System.err.println("  " + getName() + " is considering the authentication as unsuccessful.");
                }
            
        } else {
            String messageName = message == null ? "?" : message.getName();
            String senderName = sender == null ? "?" : sender.getName();
            System.err.println(senderName + " -> " + getName() + " : " + messageName);
            
        }
    }

    private Data_5G_HE_AV generateAVsAndInvokeSIDF(Nudm_UEAuthentication_Get_Request getRequest, AUSF ausf) {
        AVGenerator.AV av = AVGenerator.generate(this.K, this.AMF);

        byte[] KEY = Converter.concatenateBytes(av.CK, av.IK);

        byte[] Fc_Kausf = Converter.intToBytes(0x6A);
        byte[][] Pis_Kausf = {
                getRequest.servingNetworkName,
                av.SQNxorAK
        };
        int SQNxorAKLength = Converter.shrinkBytes(av.SQNxorAK).length == 0 ? 0x00 : 0x06;
        byte[][] Lis_Kausf = {
                null,
                Converter.intToBytes(SQNxorAKLength)
        };
        byte[] Kausf = KDF.deriveKey(KEY, Fc_Kausf, Pis_Kausf, Lis_Kausf);

        byte[] Fc_XRESstar = Converter.intToBytes(0x6B);
        byte[][] Pis_XRESstar = {
                getRequest.servingNetworkName,
                av.RAND,
                av.XRES
        };
        int RANDLength = Converter.shrinkBytes(av.RAND).length == 0 ? 0x00 : 0x10;
        byte[][] Lis_XRESstar = {
                null,
                Converter.intToBytes(RANDLength),
                null
        };
        byte[] XRESstar = KDF.deriveKey(KEY, Fc_XRESstar, Pis_XRESstar, Lis_XRESstar);

        Data_AUTN AUTN = new Data_AUTN(av.SQNxorAK, av.AMF, av.MAC);
        System.out.println("Authentication vector created in the UDM");
        return new Data_5G_HE_AV(av.RAND, AUTN, XRESstar, Kausf);
    }

    private Nudm_Authentication_Get_Response getGetResponse(Nudm_UEAuthentication_Get_Request getRequest,
                                                            AUSF ausf, Data_5G_HE_AV av) {
        byte[] SUPI = null;
        if (getRequest.SUCI != null) {
            SUPI = SIDF.deconcealSUCI(getRequest.SUCI, this.privateKey);
        }
        return new Nudm_Authentication_Get_Response(av, SUPI);
    }

}
