package distributedwhiteboard;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.font.TextAttribute;
import java.io.Serializable;
import java.util.Map;

/**
 * Contains the details of a message being sent between two distributed 
 * whiteboards. The contents of the message includes the {@link DrawMode} to use
 * , the start and end {@link Point}s and the drawing {@link Color}.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-03-12
 */
public class WhiteboardMessage implements Serializable
{
    /** Serialisation ID. */
    private static final long serialVersionUID = 5459762541371665893L;
    // <editor-fold defaultstate="collapsed" desc="Message sizes.">
    /** The size of a encoded {@link Point} object. */
    private static final byte POINT_SIZE = 8;
    /** The size of a encoded {@link Color} object. */
    private static final byte COLOUR_SIZE = 6;
    /** The maximum size a {@link Font} name can be. */
    private static final byte FONT_NAME_SIZE = 30;
    /** The size of a {@link Font} style once encoded. */
    private static final byte FONT_STY_SZ = 1;
    /** The total size of an encoded {@link Font} object. */
    private static final byte FONT_SIZE = FONT_NAME_SIZE+FONT_STY_SZ+3;
    
    /** The byte offset for the first {@link Point} object. */
    private static final byte POINT_ONE_OFFSET = 1;
    /** The byte offset for the second {@link Point}, this is optional. */
    private static final byte POINT_TWO_OFFSET = POINT_ONE_OFFSET+POINT_SIZE;
    /** The offset for the {@link Color} object used by shapes. */
    private static final byte COLOUR_OFFSET = POINT_TWO_OFFSET+POINT_SIZE;
    /** The byte offset of the line weight value. */
    private static final byte WEIGHT_OFFSET = COLOUR_OFFSET+COLOUR_SIZE;
    
    /** The byte offset of the {@link Color} object used by text. */
    private static final byte FONT_COL_OFFSET = POINT_TWO_OFFSET;
    /** The byte offset of the text character position. */
    private static final byte TEXT_OFFSET = FONT_COL_OFFSET+COLOUR_SIZE;
    /** The byte offset for the {@link Font} object. */
    private static final byte FONT_OFFSET = TEXT_OFFSET+1;
    /** The index in a string where the {@link Font} style begins.*/
    private static final byte FONT_STYLE_OFFSET = FONT_NAME_SIZE;
    /** The index in a string where the {@link Font} underline attribute is. */
    private static final byte FONT_UNDER_OFFSET = FONT_STYLE_OFFSET+FONT_STY_SZ;
    /** The index in a string where the {@link Font} size begins.*/
    private static final byte FONT_SIZE_OFFSET = FONT_UNDER_OFFSET+1;
    
    /** The byte offset for the shape fill boolean. */
    private static final byte FILL_OFFSET = COLOUR_OFFSET+COLOUR_SIZE;
    /** The byte offset for the shape border boolean. */
    private static final byte BORDER_OFFSET = FILL_OFFSET+1;
    /** The byte offset for the {@link Color} object used by borders. */
    private static final byte BORDER_COL_OFFSET = BORDER_OFFSET+1;
    /** The byte offset for the border weight value. */
    private static final byte BORDER_W_OFFSET = BORDER_COL_OFFSET+COLOUR_SIZE;
    // </editor-fold>
    
    /** The {@link MessageType} of this {@link WhiteboardMessage}. */
    //public final MessageType type;
    /** The {@link DrawMode} for this message if it's a drawing message. */
    public final DrawMode mode;
    /** The {@link Point} to start drawing the object from. */
    public final Point startPoint;
    /** The {@link Point} to stop drawing the object at (optional). */
    public final Point endPoint;
    /** The {@link Color} to draw the object with. */
    public final Color drawColour;
    /** 
     * The thickness of any {@link DrawMode#LINE}, 
     * {@link DrawMode#FREEFORM_LINE}, or {@link DrawMode#POLYGON} objects.
     */
    public final int lineWeight;
    /** Should the shape be filled or just be an outline? */
    public final boolean fillShape;
    /** Does this shape have a border? */
    public final boolean hasBorder;
    /**
     * The thickness of a border if {@link WhiteboardMessage#hasBorder} is set
     * to true.
     */
    public final int borderWeight;
    /**
     * The {@link Color} of the border if {@link WhiteboardMessage#hasBorder} is
     *  set to true.
     */
    public final Color borderCol;
    /** The {@link Font} to use when drawing {@link DrawMode#TEXT}. */
    public final Font font;
    /** The character to draw. */
    public final char textChar;
    
    /**
     * Creates an empty {@link DrawMode#TEXT} message, this is mostly used to 
     * get the largest possible message for setting the server byte buffer size.
     * 
     * @since 1.0
     */
    public WhiteboardMessage()
    {
        this.mode = DrawMode.TEXT;
        this.startPoint = new Point();
        this.endPoint = new Point();
        this.drawColour = Color.BLACK;
        this.lineWeight = 1;
        this.fillShape = false;
        this.hasBorder = false;
        this.borderWeight = 1;
        this.borderCol = Color.LIGHT_GRAY;
        this.font = new Font("Serif", Font.PLAIN, 12);
        this.textChar = '\0';
    }
    
    /**
     * Creates a new {@link WhiteboardMessage} that contains a message to draw 
     * between two points.
     * 
     * @param mode The {@link DrawMode} to send.
     * @param p1 The starting {@link Point} for this drawing.
     * @param p2 The ending {@link Point} for this drawing.
     * @param drawCol The {@link Color} to draw with.
     * @param weight The thickness of the lines used when drawing.
     * @since 1.0
     */
    public WhiteboardMessage(DrawMode mode, Point p1, 
            Point p2, Color drawCol, int weight)
    {
        this.mode = mode;
        this.startPoint = p1;
        this.endPoint = p2;
        this.drawColour = drawCol;
        this.lineWeight = weight;        
        this.fillShape = false;
        this.hasBorder = false;
        this.borderWeight = 1;
        this.borderCol = Color.LIGHT_GRAY;
        this.font = new Font("Serif", Font.PLAIN, 12);
        this.textChar = '\0';
    }
    
    /**
     * Creates a new {@link WhiteboardMessage} that contains a message to draw 
     * between two points with a border and/or a fill.
     * 
     * @param mode The {@link DrawMode} to send.
     * @param p1 The starting {@link Point} for this drawing.
     * @param p2 The ending {@link Point} for this drawing.
     * @param drawCol The {@link Color} to draw with.
     * @param weight The thickness of the lines used when drawing a border.
     * @param fill Set this to true to fill the centre of this drawing with the 
     * drawing colour.
     * @param border Set this to true to draw a border around the shape.
     * @param bCol The {@link Color} to use when drawing the border.
     * @since 1.0
     */
    public WhiteboardMessage(DrawMode mode, Point p1,  Point p2, Color drawCol, 
            int weight, boolean fill, boolean border, Color bCol)
    {
        this.mode = mode;
        this.startPoint = p1;
        this.endPoint = p2;
        this.drawColour = drawCol;
        this.lineWeight = 1;        
        this.fillShape = fill;
        this.hasBorder = border;
        this.borderWeight = weight;
        this.borderCol = bCol;
        this.font = new Font("Serif", Font.PLAIN, 12);
        this.textChar = '\0';
    }
    
    /**
     * Creates a new {@link WhiteboardMessage} that contains a message to draw 
     * the specified character in the provided {@link Font}.
     * 
     * @param mode
     * @param p1
     * @param drawCol
     * @param f
     * @param text 
     */
    public WhiteboardMessage(DrawMode mode, Point p1, Color drawCol, Font f, 
            char text)
    {
        this.mode = mode;
        this.startPoint = p1;
        this.endPoint = new Point();
        this.drawColour = drawCol;
        this.lineWeight = 1;        
        this.fillShape = false;
        this.hasBorder = false;
        this.borderWeight = 1;
        this.borderCol = Color.LIGHT_GRAY;
        this.font = f;
        this.textChar = text;
    }
        
    /**
     * Encodes this {@link WhiteboardMessage} into a byte array so it can be 
     * sent across networks. The array is generated from the {@link String} 
     * representation of this message.
     * 
     * @return Returns a byte array representation of this message.
     * @since 1.0
     */
    public byte[] encode() { return this.toString().getBytes(); }
    
    /**
     * Converts a provided byte array into a {@link WhiteboardMessage} if it 
     * contains all the required information.
     * 
     * @param message The byte array to convert to a new message.
     * @return Returns a new {@link WhiteboardMessage} if the byte array is 
     * properly formed, null otherwise.
     * @since 1.0
     */
    public static WhiteboardMessage decodeMessage(byte[] message)
    {
        String messageStr = new String(message).trim();
        DrawMode m = DrawMode.parseChar(messageStr.charAt(0));
        
        Point p1 = stringToPoint(messageStr.substring(POINT_ONE_OFFSET, 
                POINT_TWO_OFFSET));
        Point p2;
        Color col;
        int weight;
        
        switch (m) {
            case FREEFORM_LINE:
            case LINE:
            case POLYGON:
                p2 = stringToPoint(messageStr.substring(POINT_TWO_OFFSET, 
                        COLOUR_OFFSET));
                col = stringToColor(messageStr.substring(COLOUR_OFFSET, 
                        WEIGHT_OFFSET));
                weight = Integer.valueOf(messageStr.substring(WEIGHT_OFFSET,
                        WEIGHT_OFFSET+2));
                
                if (m == null || p1 == null || p2 == null || col == null 
                        || weight < 1)
                    return null;
                
                return new WhiteboardMessage(m, p1, p2, col, weight);
            case RECTANGLE:
                p2 = stringToPoint(messageStr.substring(POINT_TWO_OFFSET, 
                        COLOUR_OFFSET));
                col = stringToColor(messageStr.substring(COLOUR_OFFSET, 
                        FILL_OFFSET));
                
                String bVal = messageStr.substring(FILL_OFFSET, BORDER_OFFSET);
                boolean fill = bVal.equals("t");
                bVal = messageStr.substring(BORDER_OFFSET, BORDER_COL_OFFSET);
                boolean border = bVal.equals("t");
                
                Color borderCol = stringToColor(
                        messageStr.substring(BORDER_COL_OFFSET, BORDER_W_OFFSET)
                );
                weight = Integer.valueOf(messageStr.substring(BORDER_W_OFFSET, 
                        BORDER_W_OFFSET+2));
                
                if (m == null || p1 == null || p2 == null || col == null 
                        || borderCol == null || weight < 1)
                    return null;
                
                return new WhiteboardMessage(m, p1, p2, col, weight, fill, 
                        border, borderCol);
            case TEXT:
                col = stringToColor(messageStr.substring(FONT_COL_OFFSET, 
                        TEXT_OFFSET));
                char text = messageStr.charAt(TEXT_OFFSET);
                Font font = stringToFont(messageStr.substring(FONT_OFFSET,
                        FONT_OFFSET+FONT_SIZE));
                
                if (m == null || p1 == null || font == null)
                    return null;
                
                return new WhiteboardMessage(m, p1, col, font, text);
        }
        
        return null;
    }
    
    /**
     * Converts a {@link Point} object to a {@link String}. The encoded string
     *  will have the following format; <pre>xxxxyyyy</pre>. These values can be
     *  zero padded if they do not fill the entire x or y range.
     * 
     * @param p The {@link Point} to encode.
     * @return Returns a {@link String} containing the points x and y values.
     * @since 1.0
     */
    private static String pointToString(Point p)
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
     * @since 1.0
     */
    private static String colorToString(Color c)
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
     * @since 1.0
     */
    private static String fontToString(Font f)
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
     * xxxxyyyy</pre>, if the value is low enough these can be zero padded.
     * 
     * @param s The string to convert.
     * @return Returns a new {@link Point} if the string can be decoded, null 
     * otherwise.
     * @since 1.0
     */
    private static Point stringToPoint(String s)
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
     * @since 1.0
     */
    private static Color stringToColor(String s)
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
     * @since 1.0
     */
    private static Font stringToFont(String s)
    {
        if (s.length() != FONT_SIZE) {
            System.err.println("Provided String is not a valid font.");
            return null;
        }
        
        String name = s.substring(0, FONT_NAME_SIZE).trim();
        int style = Integer.valueOf(s.substring(FONT_STYLE_OFFSET, 
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
     * Creates a {@link String} representation of this {@link WhiteboardMessage}
     *  , the message structure will vary based on the requirements of the 
     * message.
     * 
     * @return Returns a new {@link String}.
     * @since 1.0
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        //sb.append(type.value);
        sb.append(mode.value);
        sb.append(pointToString(startPoint));
        switch (mode) {
            case FREEFORM_LINE:
            case LINE:
            case POLYGON:
                sb.append(pointToString(endPoint));
                sb.append(colorToString(drawColour));
                sb.append(String.format("%02d", lineWeight));
                break;
            case RECTANGLE:
                sb.append(pointToString(endPoint));
                sb.append(colorToString(drawColour));
                sb.append((fillShape ? "t" : "f"));
                sb.append((hasBorder ? "t" : "f"));
                sb.append(colorToString(borderCol));
                sb.append(String.format("%02d", borderWeight));
                break;
            case TEXT:
                sb.append(colorToString(drawColour));
                sb.append(textChar);
                sb.append(fontToString(font));
                break;
        }
        
        return sb.toString();
    }
    
    /**
     * Message types to help the server understand what a {@link 
     * WhiteboardMessage} is trying to communicate.
     * 
     * @version 1.0
     * @since 1.0
     */
    public enum MessageType
    {
        /** This message contains data for a drawing object. */
        DRAW('d');
        
        public final char value;
        private MessageType(char value) { this.value = value; }
        
        @Override        
        public String toString()
        {
            return String.format("%s(%c)", this.name(), this.value);
        }
    }
}
