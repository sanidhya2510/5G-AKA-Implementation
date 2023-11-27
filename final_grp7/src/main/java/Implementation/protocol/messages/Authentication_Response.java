package Implementation.protocol.messages;

import Implementation.structure.Message;

public class Authentication_Response implements Message {

    //RES*
    public final byte[] RESstar;

    @Override
    public String getName() {
        return "Authentication Response";
    }

    public Authentication_Response(byte[] RESstar) {
        this.RESstar = RESstar;
    }
}
