package Implementation.protocol.messages;

import Implementation.structure.Message;

public class Authentication_Reject implements Message {


    @Override
    public String getName() {
        return "Authentication Reject";
    }
}
