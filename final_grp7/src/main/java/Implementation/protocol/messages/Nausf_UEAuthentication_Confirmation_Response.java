package Implementation.protocol.messages;

import Implementation.structure.Message;


public class Nausf_UEAuthentication_Confirmation_Response implements Message {
    
    public final boolean wasSuccessful;

    //Kseaf
    public final byte[] Kseaf;

    //SUPI: Might be null.
    public final byte[] SUPI;

    @Override
    public String getName() {
        return "Nausf_UEAuthentication Confirmation Response";
    }

    public Nausf_UEAuthentication_Confirmation_Response(boolean wasSuccessful, byte[] Kseaf, byte[] SUPI) {
        this.wasSuccessful = wasSuccessful;
        this.Kseaf = Kseaf;
        this.SUPI = SUPI;
    }
}
