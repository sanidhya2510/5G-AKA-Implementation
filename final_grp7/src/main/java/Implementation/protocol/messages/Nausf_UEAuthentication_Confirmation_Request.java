package Implementation.protocol.messages;

import Implementation.structure.Message;


public class Nausf_UEAuthentication_Confirmation_Request implements Message {
    

    public final byte[] RESstar;

    @Override
    public String getName() {
        return "Nausf_UEAuthentication Confirmation Request";
    }

    public Nausf_UEAuthentication_Confirmation_Request(byte[] RESstar) {
        this.RESstar = RESstar;
    }
}
