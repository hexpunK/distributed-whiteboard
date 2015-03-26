package distributedwhiteboard;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.font.TextAttribute;
import java.util.Map;

/**
 * Various conversions from {@link Object} types to {@link String} encodings.
 * 
 * @author 6266215
 * @version 1.1
 * @since 2015-03-26
 */
public class Conversions
{
    // <editor-fold defaultstate="collapsed" desc="Message sizes.">
    /** The size of a encoded {@link Point} object. */
    protected static final int POINT_SIZE = 8;
    /** The size of a encoded {@link Color} object. */
    protected static final int COLOUR_SIZE = 6;
    /** The maximum size a {@link Font} name can be. */
    protected static final int FONT_NAME_SIZE = 30;
    /** The size of a {@link Font} style once encoded. */
    protected static final int FONT_STY_SZ = 1;
    /** The total size of an encoded {@link Font} object. */
    protected static final int FONT_SIZE = FONT_NAME_SIZE+FONT_STY_SZ+3;
    
    /** The byte offset for the first {@link Point} object. */
    protected static final int POINT_ONE_OFFSET = NetMessage.TYPE_OFFSET+2;
    /** The byte offset for the second {@link Point}, this is optional. */
    protected static final int POINT_TWO_OFFSET = POINT_ONE_OFFSET+POINT_SIZE;
    /** The offset for the {@link Color} object used by shapes. */
    protected static final int COLOUR_OFFSET = POINT_TWO_OFFSET+POINT_SIZE;
    /** The byte offset of the line weight mode. */
    protected static final int WEIGHT_OFFSET = COLOUR_OFFSET+COLOUR_SIZE;
    
    /** The byte offset of the {@link Color} object used by text. */
    protected static final int FONT_COL_OFFSET = POINT_TWO_OFFSET;
    /** The byte offset of the text character position. */
    protected static final int TEXT_OFFSET = FONT_COL_OFFSET+COLOUR_SIZE;
    /** The byte offset for the {@link Font} object. */
    protected static final int FONT_OFFSET = TEXT_OFFSET+1;
    /** The index in a string where the {@link Font} style begins.*/
    protected static final int FONT_STY_OFFSET = FONT_NAME_SIZE;
    /** The index in a string where the {@link Font} underline attribute is. */
    protected static final int FONT_UNDER_OFFSET = FONT_STY_OFFSET+FONT_STY_SZ;
    /** The index in a string where the {@link Font} size begins.*/
    protected static final int FONT_SIZE_OFFSET = FONT_UNDER_OFFSET+1;
    
    /** The byte offset for the shape fill boolean. */
    protected static final int FILL_OFFSET = COLOUR_OFFSET+COLOUR_SIZE;
    /** The byte offset for the shape border boolean. */
    protected static final int BORDER_OFFSET = FILL_OFFSET+1;
    /** The byte offset for the {@link Color} object used by borders. */
    protected static final int BORDER_COL_OFFSET = BORDER_OFFSET+1;
    /** The byte offset for the border weight mode. */
    protected static final int BORDER_W_OFFSET = BORDER_COL_OFFSET+COLOUR_SIZE;
    // </editor-fold>
    
    /**
     * Converts a {@link Point} object to a {@link String}. The encoded string
     *  will have the following format; <pre>xxxxyyyy</pre>. These values can be
     *  zero padded if they do not fill the entire x or y range.
     * 
     * @param p The {@link Point} to encode.
     * @return Returns a {@link String} containing the points x and y values.
     * @since 1.1
     */
    public static String pointToString(Point p)
    {
        return String.format("%04d%04d", p.x, p.y);
    }
    
    /**
     * Converts a {@link Color} object to a {@link String}. The encoded string 
     * will follow a standard hexadecimal format of; <pre>rrggbb</pre>.
     * 
     * @param c The {@link Color} to encode.
     * @return Returns a {@link String} containing the red, green and blue 
     * values in their two character hexadecimal representation.
     * @since 1.1
     */
    public static String colorToString(Color c)
    {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        
        String s = String.format("%02x%02x%02x", r, g, b);
        return s;
    }
    
    /**
     * Converts a {@link Font} to a {@link String}. The encoded string will have
     *  the following format;
     * <ol>
     * <li>20 characters containing a font name.</li>
     * <li>1 character containing an integer for the font style.</li>
     * <li>2 characters containing an integer for the font size.</li>
     * </ol>
     * 
     * @param f The {@link Font} to encode.
     * @return Returns a {@link String} containing an encoded {@link Font}.
     * @since 1.1
     */
    public static String fontToString(Font f)
    {
        StringBuilder name = new StringBuilder(f.getName());
        int paddingNeeded = FONT_NAME_SIZE - name.length();
        for (int i = 0; i < paddingNeeded; i++)
            name.append(" ");
        
        Map attribs = f.getAttributes();
        char u = (attribs.get(TextAttribute.UNDERLINE) != null) ? 't' : 'f';
        
        return String.format("%s%d%c%02d", name.toString(), 
                f.getStyle(), u, f.getSize());
    }
    
    /**
     * Attempts to convert a {@link String} into a {@link Point}. This method 
     * expects the string to contain an x and y component in the format; <pre>
     * xxxxyyyy</pre>, if the mode is low enough these can be zero padded.
     * 
     * @param s The string to convert.
     * @return Returns a new {@link Point} if the string can be decoded, null 
     * otherwise.
     * @since 1.1
     */
    public static Point stringToPoint(String s)
    {
        if (s.length() != POINT_SIZE) {
            System.err.println("Provided String is not a valid point.");
            return null;
        }
        String x = s.substring(0, POINT_SIZE/2);
        String y = s.substring(POINT_SIZE/2);
        
        int[] point = new int[2];
        try {
            point[0] = Integer.parseInt(x);
            point[1] = Integer.parseInt(y);
        } catch (NumberFormatException nfEx) {
            return null;
        }
        
        return new Point(point[0], point[1]);
    }
        
    /**
     * Converts a hexadecimal {@link String} into a new {@link Color} object. 
     * This method only accepts hexadecimal values in the form of; <pre>
     * rrggbb</pre>. Any other formats will return null.
     * 
     * @param s The string to convert.
     * @return Returns a new {@link Color} object if the string can be decoded, 
     * null otherwise.
     * @since 1.1
     */
    public static Color stringToColor(String s)
    {
        if (s.length() != COLOUR_SIZE) {
            System.err.println("Provided String is not a hexadecimal colour.");
            return null;
        }
        
        String rChars = s.substring(0, 2);
        String gChars = s.substring(2, 4);
        String bChars = s.substring(4);
        
        int r = Integer.parseInt(rChars, 16);
        int g = Integer.parseInt(gChars, 16);
        int b = Integer.parseInt(bChars, 16);
        
        if ((r < 0 || r > 255) || (g < 0 || g > 255) || (b < 0 || b > 255)) {
            System.err.println("A colour component was invalid.");
            return null;
        }
        
        return new Color(r, g, b);
    }
    
    /**
     * Attempts to convert a {@link String} to a {@link Font}. This expects an 
     * encoded string that has the following structure;
     * <ol>
     * <li>20 characters containing a font name.</li>
     * <li>1 character containing an integer for the font style.</li>
     * <li>1 character containing an boolean for the underline status.</li>
     * <li>2 characters containing an integer for the font size.</li>
     * </ol>
     * If the String is not in this format, it will fail to create a new font.
     * 
     * @param s The string to convert.
     * @return Returns a new {@link Font} if the string can be decoded, null 
     * otherwise.
     * @since 1.1
     */
    public static Font stringToFont(String s)
    {
        if (s.length() != FONT_SIZE) {
            System.err.println("Provided String is not a valid font.");
            return null;
        }
        
        String name = s.substring(0, FONT_NAME_SIZE).trim();
        int style = Integer.valueOf(s.substring(FONT_STY_OFFSET, 
                FONT_UNDER_OFFSET));
        char under = s.charAt(FONT_UNDER_OFFSET);
        int size = Integer.valueOf(s.substring(FONT_SIZE_OFFSET));
        
        Font f = new Font(name, style, size);
        Map attribs = f.getAttributes();
        if (under == 't') {
            attribs.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        } else {
            attribs.put(TextAttribute.UNDERLINE, null);
        }
        f = f.deriveFont(attribs);
        
        return f;
    }
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
