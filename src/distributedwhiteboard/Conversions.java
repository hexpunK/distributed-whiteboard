package distributedwhiteboard;

/**
 *
 * @author Jordan
 */
public class Conversions
{
    /**
     * Converts a {@link String} to a properly formed IPv4 IP address.
     * 
     * @param str The encoded String to convert.
     * @return The decoded IP address as a String. If the String cannot be 
     * decoded properly, this will return null instead.
     * @since 1.0
     */
    public static String stringToIP(String str)
    {
        String[] octetStrs = new String[4];
        
        try {
            octetStrs[0] = str.substring(0, 3);
            octetStrs[1] = str.substring(3, 6);
            octetStrs[2] = str.substring(6, 9);
            octetStrs[3] = str.substring(9, 12);
        } catch (IndexOutOfBoundsException indEx) {
            System.err.println("IP Address is not properly formed.");
            return null;
        }
        
        int[] octets = new int[octetStrs.length];
        try {
            for (int i = 0; i < octetStrs.length; i++)
                octets[i] = Integer.parseInt(octetStrs[i]);
        } catch (NumberFormatException nfe) {
            System.err.println("IP Address contained non-numeric character.");
            return null;
        }
        
        return String.format("%d.%d.%d.%d", octets[0], octets[1], 
                octets[2], octets[3]);
    }
    
    /**
     * Converts a standard representation of an IPv4 address to an encoded 
     * String to be sent over the network. This strips out the periods and pads 
     * each octet to 3 characters in length.
     * 
     * @param address The IPv4 formatted address to convert to an encoded form.
     * @return Returns the encoded String if it is encoded, otherwise returns 
     * null.
     * @since 1.0
     */
    public static String ipToString(String address)
    {
        String[] octetStrs = address.split("\\.");
        if (octetStrs.length != 4) return null;
        
        int[] octets = new int[octetStrs.length];
        
        for (int i = 0; i < octets.length; i++) {
            try {
                octets[i] = Integer.parseInt(octetStrs[i]);
            } catch (NumberFormatException nfe) {
                System.err.println("Provided IP address is incorrectly formed.");
                return null;
            }
        }
        
        return String.format("%03d%03d%03d%03d", 
                octets[0], octets[1], octets[2], octets[3]);
    }
}
