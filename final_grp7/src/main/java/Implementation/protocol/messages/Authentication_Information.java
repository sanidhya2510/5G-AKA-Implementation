package Implementation.protocol.messages;

import Implementation.structure.Message;


public class Authentication_Information implements Message {

    public final boolean authenticationSuccessful;

    @Override
    public String getName() {
        return "Authentication Information";
    }

    public Authentication_Information(boolean authenticationSuccessful) {
        this.authenticationSuccessful = authenticationSuccessful;
    }
}
