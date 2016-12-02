
import java.util.ArrayList;

/**
 * Provides simple conversion functions
 */

public class Converter {

    public static ArrayList<Integer> ByteArrayToIntegerArrayList(byte[] ba)
    {
        ArrayList<Integer> intValues = new ArrayList<>();

        for(int i = 0; i < ba.length; i ++)
        {
            int v = ba[i];
            intValues.add(v);
        }
        return intValues;
    }

    public static byte[] IntegerArrayListToByteArray(ArrayList<Integer> ia)
    {
        
        byte[] ba = new byte[ia.size()];
        for(int i = 0; i < ia.size(); i ++)
        {
            ba[i] = ia.get(i).byteValue();
        }
        return ba;
    }
    
    public static String HexStringToSeparatedHexString(String s)  //16
    {
        int index = 0;
        String rtnString = "";

        while(index < s.length())
        {
            rtnString += s.substring(index, index+2);  //(start,length)
            index += 2;

            if(index != s.length())
                rtnString += " ";
        }
        return rtnString;
    }
    
    public static int ByteArrayToInt(byte [] buff, int index)
    {
        int rtValue = (int) buff[index];
        rtValue = (int) (rtValue << 24);        //left 24 bit
        rtValue &= 0xFF000000;                  //int 32bits 0xFF0000000 function: L24 turn 0
        
        int temp = (int) buff[index + 1];
        temp = (int) (temp << 16);
        temp &= 0x00FF0000;                      //H8 turn 0
        rtValue = (int) (rtValue | temp);

        temp = (int) buff[index + 2];
        temp = (int) (temp << 8);
        temp &= 0x0000FF00;                      //H16 turn 0
        rtValue = (int) (rtValue | temp);

        temp = (int) buff[index + 3];
        temp &= 0x000000FF;                      //H24 turn 0
        rtValue = (int) (rtValue | temp);
        return rtValue;
    }
    
    public static int ByteToInt(byte [] buff, int index)
    {
        int rtValue = (int) buff[index];
        for(int i=0;i<buff.length;i++)
            rtValue +=(buff[i]&0xff)<<(24-8*i);
        return rtValue;
    }
    
    public static char ByteArrayToChar(byte [] buff, int index)
    {
        char rtValue = (char) buff[index];
        rtValue = (char) (rtValue << 8);
        rtValue &= 0xFF00;

        char temp = (char) buff[index + 1];
        temp &= 0x00FF;
        rtValue = (char) (rtValue | temp);
        return rtValue;
    }

    public static byte [] IntToByteArray(int c)
    {
        byte [] byteArray = new byte [4];
        byteArray [0] = (byte)((c >> 24) & 0x000000ff);
        byteArray [1] = (byte)((c >> 16) & 0x000000ff);
        byteArray [2] = (byte)((c >> 8) & 0x000000ff);
        byteArray [3] = (byte)(c & 0x000000ff);
        return byteArray;
    }

    public static byte [] CharToByteArray(char c)
    {
        byte [] byteArray = new byte [2];
        byteArray [0] = (byte)((c >> 8) & 0x00ff);
        byteArray [1] = (byte)(c & 0x00ff);
        return byteArray;
    }

    public static int [] HexStringToInt(String s)
    {
        // the size of the byte array
        int length = s.length() / 8;
        int [] intArray = new int[length];

        int index = 0;
        for(int i = 0; i < length; i ++)
        {
            String subStr = s.substring(index, index + 8);
            //intArray [i] = Integer.parseInt(subStr, 16); //cannot handle input greater than 0x7FFF FFFF
            intArray [i] = (int)Long.parseLong(subStr, 16);
            index += 8;
        }
        return intArray;
    }

    public static char [] HexStringToChar(String s)
    {
        // the size of the byte array
        int length = s.length() / 4;
        char [] charArray = new char[length];

        int index = 0;
        for(int i = 0; i < length; i ++)
        {
            String subStr = s.substring(index, index + 4);
            charArray [i] = (char)(Integer.parseInt(subStr, 16) & 0x0000FFFF);
            index += 4;
        }
        return charArray;
    }

    public static byte [] HexStringToByte(String s)
    {
        // the size of the byte array
        int length = s.length() / 2;
        byte [] byteArray = new byte[length];

        int index = 0;
        for(int i = 0; i < length; i ++)
        {
            String subStr = s.substring(index, index + 2);
            byteArray [i] = (byte)(Integer.parseInt(subStr, 16) & 0x000000FF);
            index += 2;
        }
        return byteArray;
    }

    public static String ByteArrayToHexString(byte [] b)
    {
        String output = "";
        for(int i = 0; i < b.length; i ++)
            output += ByteToHexString(b[i]);
        return output;
    }

    public static String ByteToHexString(byte b)
    {
        if((b & 0x80) != 0)
        {
            char c = (char) b;
            c &= 0x00FF;
            return Integer.toHexString(c);
        }
        else
        {
            if((b & 0xF0) == 0)
                return "0" + Integer.toHexString(b);
            else
                return Integer.toHexString(b);
        }
    }

    public static String CharArrayToHexString(char [] c)
    {
        String output = "";
        for(int i = 0; i < c.length; i ++)
            output += CharToHexString(c[i]);
        return output;
    }

    public static String CharToHexString(char c)
    {
        String output = "";

        if((c & 0x8000) != 0)
        {
            int i = (int) c;
            i &= 0x0000FFFF;
            return Integer.toHexString(i);
        }
        else
        {
            if((c & 0xF000) != 0)
                return Integer.toHexString(c);
            else
            {
                output += "0";
                if((c & 0x0F00) != 0)
                    return output += Integer.toHexString(c);
                else
                {
                    output += "0";
                    if((c & 0x00F0) != 0)
                        return output += Integer.toHexString(c);
                    else
                    {
                        output += "0";
                        if((c & 0x000F) != 0)
                            return output += Integer.toHexString(c);
                        else
                        {
                            output += "0";
                            return output;
                        }
                    }
                }
            }
        }
    }

    public static String IntToHexString(int c)
    {
        String output;
        char h = (char) ((c >> 16) & 0x0000FFFF);
        char l = (char) (c & 0x0000FFFF);

        output = CharToHexString(h) + CharToHexString(l);
        return output;
    }
    
    static public double bytesToRealNumber(byte[] b)
    {
        //Directly convert 4 or 8 bytes to floating point representation. Truncate if necessary.
        if (b.length == 4)
        {
            return (double) byteArrayToFloat(b);
        }
        if (b.length == 8)
        {
            return byteArrayToDouble(b);
        }

        //-----truncation----
        int size = 0;
        if (b.length > 8) //truncate to 8
        {
            size = 8;
        }
        if (b.length > 4 && b.length < 8) //truncate to 4
        {
            size = 4;
        }
        if (size < 4) //not enough data to convert
        {
            return Double.NaN;
        }

        //----conversion-----
        byte[] newBytes = new byte[size];
        for (int i = 0; i < size; i++)
        {
            newBytes[i] = b[i];
        }
        if (size == 8)
        {
            return byteArrayToDouble(newBytes);
        }
        else
        {
            return (double) byteArrayToFloat(newBytes);
        }
    }
    
    static public float byteArrayToFloat(byte[] bytes)
    {
        int bits = ((0xFF & bytes[3]) | ((0xFF & bytes[2]) << 8) | ((0xFF & bytes[1]) << 16) | ((0xFF & bytes[0]) << 24));

        float floatValue = Float.intBitsToFloat(bits);
        return floatValue;
    }

    /**
     * Convert 8 bytes to double precision floating point, according to IEEE-754
     * @param bytes Input byte array
     * @return Numerical representation of the byte array
     */
    static public double byteArrayToDouble(byte[] bytes)
    {
        long bits = ((0xFF & bytes[7]) | ((0xFF & bytes[6]) << 8) | ((0xFF & bytes[5]) << 16) | ((0xFF & bytes[4]) << 24)
                | (0xFF & bytes[3] << 32) | (0xFF & bytes[2] << 40) | (0xFF & bytes[1] << 48) | (0xFF & bytes[0] << 56));

        double doubleValue = Double.longBitsToDouble(bits);
        return doubleValue;
    }
}


