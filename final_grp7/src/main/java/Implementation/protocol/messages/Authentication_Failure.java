package Implementation.protocol.messages;

import Implementation.structure.Message;

public class Authentication_Failure implements Message {
   

    @Override
    public String getName() {
        return "Authentication Failure";
    }
}
