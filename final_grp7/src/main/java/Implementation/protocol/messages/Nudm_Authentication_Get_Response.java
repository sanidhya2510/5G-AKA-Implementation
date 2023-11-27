package Implementation.protocol.messages;

import Implementation.protocol.data.Data_5G_HE_AV;
import Implementation.structure.Message;

public class Nudm_Authentication_Get_Response implements Message {
   

    public final Data_5G_HE_AV heAV;

   

    //If SUCI was sent in Request, return SUPI
    public final byte[] SUPI;

    @Override
    public String getName() {
        return "Nudm_Authentication_Get Response";
    }

    public Nudm_Authentication_Get_Response(Data_5G_HE_AV heAV, byte[] SUPI) {
        this.heAV = heAV;
        this.SUPI = SUPI;
    }
}
