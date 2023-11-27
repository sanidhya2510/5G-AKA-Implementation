package Implementation.protocol.additional;

import Implementation.helper.Converter;
import Implementation.helper.HmacSHA256;

public class MAF {//Message authentication functions

    public static byte[] f1(byte[] key, byte[] data) {
        
        byte[] longVersion = Converter.expandBytesToLength(HmacSHA256.encode(key, data), 8);
        byte[] result = new byte[ParameterLength.MAC];
        System.arraycopy(longVersion, 0, result, 0, result.length);
        return result;
    }

    public static byte[] f2(byte[] key, byte[] data) {
       
        return HmacSHA256.encode(key, data);
    }
}
