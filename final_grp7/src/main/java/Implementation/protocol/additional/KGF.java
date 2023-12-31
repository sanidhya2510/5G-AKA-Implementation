package Implementation.protocol.additional;

import Implementation.helper.Converter;
import Implementation.helper.HmacSHA256;

//3GPP TS 33.102 V15.1.0 Page 26
public class KGF {//Key generating functions

    public static byte[] f3(byte[] key, byte[] data) {
        //3GPP TS 33.102 V15.1.0 Page 26
        //MARK: Derivation 4
        byte[] longVersion = Converter.expandBytesToLength(HmacSHA256.encode(key, data), 16);
        byte[] result = new byte[ParameterLength.CK];
        System.arraycopy(longVersion, 0, result, 0, result.length);
        return result;
    }

    public static byte[] f4(byte[] key, byte[] data) {

        byte[] longVersion = Converter.expandBytesToLength(HmacSHA256.encode(key, data), 16);
        byte[] result = new byte[ParameterLength.IK];
        System.arraycopy(longVersion, 0, result, 0, result.length);
        return result;
    }

    public static byte[] f5(byte[] key, byte[] data) {
        
        byte[] longVersion = Converter.expandBytesToLength(HmacSHA256.encode(key, data), 6);
        byte[] result = new byte[ParameterLength.AK];
        System.arraycopy(longVersion, 0, result, 0, result.length);
        return result;
    }
}
