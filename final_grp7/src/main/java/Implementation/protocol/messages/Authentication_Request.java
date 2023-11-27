package Implementation.protocol.messages;

import Implementation.protocol.data.Data_AUTN;
import Implementation.structure.Message;

public class Authentication_Request implements Message {
   
    public final byte[] RAND;
    public final Data_AUTN AUTN;

    @Override
    public String getName() {
        return "Authentication Request";
    }

    public Authentication_Request(byte[] RAND, Data_AUTN AUTN) {
        this.RAND = RAND;
        this.AUTN = AUTN;
    }
}
