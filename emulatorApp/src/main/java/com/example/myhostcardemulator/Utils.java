package com.example.myhostcardemulator;

class Utils {

    private static String HEX_CHARS = "0123456789ABCDEF";

    /**
     * Convert a hex number string to a byte array
     * @param data A hex number string
     * @return The hex numbers as a byte array
     */
    static byte[] hexStringToByteArray(String data){

        byte[] result = new byte[data.length() / 2];

        for (int i = 0; i < data.length(); i += 2) {
            int firstIndex = HEX_CHARS.indexOf(data.charAt(i));
            int secondIndex = HEX_CHARS.indexOf(data.charAt(i + 1));

            int octet = firstIndex << 4 | secondIndex;
            result[i >> 1] = (byte) octet;
        }

        return result;
    }

    private static char[] HEX_CHARS_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * Convert a byte array to one hex string
     * @param byteArray A byte array
     * @return A string of all the bytes in byteArray as hex
     */
    static String toHex(byte[] byteArray){
        StringBuffer result = new StringBuffer();

        for(byte it : byteArray){
            int octet = it;
            int firstIndex = (octet & 0xF0) >>> 4;
            int secondIndex = octet & 0x0F;
            result.append(HEX_CHARS_ARRAY[firstIndex]);
            result.append(HEX_CHARS_ARRAY[secondIndex]);
        }

        return result.toString();
    }
}