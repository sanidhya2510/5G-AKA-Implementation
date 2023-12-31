package Implementation.protocol.entities;

import Implementation.App;
import Implementation.helper.Calculator;
import Implementation.helper.Converter;
import Implementation.helper.SHA256;
import Implementation.protocol.messages.*;
import Implementation.structure.Entity;
import Implementation.structure.Message;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class SEAF extends Entity {

    public AUSF ausf = null;
    public UE ue = null;

    //Saving the Kseaf for the corresponding SUPI in hex-format.
    private HashMap<String, byte[]> Kseafs = new HashMap<>();

    final byte[] servingNetworkName;

    public SEAF(byte[] servingNetworkName) {
        this.servingNetworkName = servingNetworkName;
    }

    @Override
    public String getName() {
        return "SEAF";
    }

    //This Queue contains a boolean to remember if the comparison of HXRES* and HRES* was successful.
    private Queue<Boolean> authenticationConfirmationQueue = new LinkedList<>();

    @Override
    public void onReceiveMessage(Message message, Entity sender) {

        //step 1 receiving
        if (message instanceof N1_Registration_Request && sender instanceof UE) {
            //Received N1 Registration Request
            N1_Registration_Request n1 = (N1_Registration_Request) message;
            UE ue = (UE) sender;
            System.out.println(ue.getName() + " -> " + getName() + " : " + n1.getName());
            

            Nausf_UEAuthentication_Authenticate_Request authRequest = getAuthRequest(n1, ue);
            //step 2 sending
            sendMessage(authRequest, this.ausf);
        } 
        //step 6 receive
        else if (message instanceof Nausf_UEAuthentication_Authenticate_Response && sender instanceof AUSF) {
            //Received Nausf_UEAuthentication_ Authenticate Response
            Nausf_UEAuthentication_Authenticate_Response authResponse = (Nausf_UEAuthentication_Authenticate_Response) message;
            AUSF ausf = (AUSF) sender;
            System.out.println(ausf.getName() + " -> " + getName() + " : " + authResponse.getName());
            

            Authentication_Request authRequest = getAuthRequest(authResponse, ausf);

            sendMessage(authRequest, this.ue);
        }
            //step 9 receive
         else if (message instanceof Authentication_Response && sender instanceof UE) {
            //Received Authentication Response
            Authentication_Response authResponse = (Authentication_Response) message;
            UE ue = (UE) sender;
            //print for step 9
            System.out.println(ue.getName() + " -> " + getName() + " : " + authResponse.getName());
            
            //MAIN DECISION MAKING OF AUTHENTICATION
            if (checkExpiryTimer(authResponse, ue) && calculateHresAndCompare(authResponse, ue)) {
                //Consider authentication as successful.
                System.out.println("  " + getName() + " is considering the authentication as successful.");
                authenticationConfirmationQueue.add(true);
            } else {
                //Consider authentication as unsuccessful.
               System.err.println("  " + getName() + " is considering the authentication as unsuccessful.");
               authenticationConfirmationQueue.add(false);
            }
            Nausf_UEAuthentication_Confirmation_Request confirmRequest = getConfirmRequest(authResponse, ue);

            sendMessage(confirmRequest, this.ausf);
        }
        // step 12 receive of confirmation response
         else if (message instanceof Nausf_UEAuthentication_Confirmation_Response && sender instanceof AUSF) {
            //Received Nausf_UEAuthentication_ Authenticate Response (/Confirmation Response)
            Nausf_UEAuthentication_Confirmation_Response confirmResponse = (Nausf_UEAuthentication_Confirmation_Response) message;
            AUSF ausf = (AUSF) sender;
             System.out.println(ausf.getName() + " -> " + getName() + " : " + confirmResponse.getName());
            boolean hashComparisonWasSuccessful = false;
            if (!authenticationConfirmationQueue.isEmpty()) {
                hashComparisonWasSuccessful = authenticationConfirmationQueue.poll();
            }

            if (hashComparisonWasSuccessful && confirmResponse.wasSuccessful && confirmResponse.Kseaf != null) {
                if (confirmResponse.SUPI != null) {
                    this.Kseafs.put(Converter.bytesToHex(confirmResponse.SUPI), confirmResponse.Kseaf);
                }
                App.reportAuthResult(true);
            } else {
                Authentication_Reject authenticationReject = new Authentication_Reject();
                sendMessage(authenticationReject, this.ue);
            }
        } else if (message instanceof Authentication_Failure && sender instanceof UE) {
            //Received Authentication Failure
            Authentication_Failure authFailure = (Authentication_Failure) message;
            UE ue = (UE) sender;
            System.out.println(ue.getName() + " -> " + getName() + " : " + authFailure.getName());
            App.reportAuthResult(false);
            
        } else {
            String messageName = message == null ? "?" : message.getName();
            String senderName = sender == null ? "?" : sender.getName();
            System.err.println(senderName + " -> " + getName() + " : " + messageName);
            
        }
    }

    //if the n1 request has SUCI then it is directly forwarded
    //if the n1 request has 5g-GUTI then the SUPI is extracted and forwarded
    private Nausf_UEAuthentication_Authenticate_Request getAuthRequest(N1_Registration_Request n1, UE ue) {
        if (n1.SUCI != null) {
            return new Nausf_UEAuthentication_Authenticate_Request(n1.SUCI, true, this.servingNetworkName);
        } else if (n1.fiveGGUTI != null) {
            byte[] SUPI = n1.fiveGGUTI.getSUPI();
            return new Nausf_UEAuthentication_Authenticate_Request(SUPI, false, this.servingNetworkName);
        }
        return null;
    }

    
    private boolean checkExpiryTimer(Authentication_Response authResponse, UE ue) {
        return true;
    }

    private HXRESstar hXRESstar = new HXRESstar();

    private Authentication_Request getAuthRequest(Nausf_UEAuthentication_Authenticate_Response authResponse, AUSF ausf) {
        //Storing the HXRES* temporary.
        hXRESstar.HXRESstar = authResponse.seAV.HXRESstar;
        hXRESstar.RAND = authResponse.seAV.RAND;

        return new Authentication_Request(authResponse.seAV.RAND, authResponse.seAV.AUTN);
    }


    private boolean calculateHresAndCompare(Authentication_Response authResponse, UE ue) {
       
        byte[] P0 = this.hXRESstar.RAND;
        byte[] P1 = authResponse.RESstar;
        byte[] S = Converter.concatenateBytes(P0, P1);
        byte[] HRESstar = SHA256.encode(S);
        return Calculator.equals(this.hXRESstar.HXRESstar, HRESstar);
    }

    private Nausf_UEAuthentication_Confirmation_Request getConfirmRequest(Authentication_Response authResponse, UE ue) {
        return new Nausf_UEAuthentication_Confirmation_Request(authResponse.RESstar);
    }

    private static class HXRESstar {
        byte[] HXRESstar;
        byte[] RAND;
    }

    public byte[] getKseafForSUPI(byte[] SUPI) {
        return this.Kseafs.get(Converter.bytesToHex(SUPI));
    }
}
