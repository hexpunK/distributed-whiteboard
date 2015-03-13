package distributedwhiteboard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.Serializable;

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
    private static final long serialVersionUID = 5459762541371665893L;
    
    private static final byte POINT_SIZE = 8;
    private static final byte COLOUR_SIZE = 6;
    private static final byte FONT_NAME_SIZE = 20;
    private static final byte FONT_STYLE_SIZE = 1;
    private static final byte FONT_SIZE = FONT_NAME_SIZE+FONT_STYLE_SIZE+2;
    
    private static final byte POINT_ONE_OFFSET = 1;
    private static final byte POINT_TWO_OFFSET = POINT_ONE_OFFSET+POINT_SIZE;
    private static final byte COLOUR_OFFSET = POINT_TWO_OFFSET+POINT_SIZE;
    private static final byte WEIGHT_OFFSET = COLOUR_OFFSET+COLOUR_SIZE;
    
    private static final byte FONT_COL_OFFSET = POINT_TWO_OFFSET;
    private static final byte TEXT_OFFSET = FONT_COL_OFFSET+COLOUR_SIZE;
    private static final byte FONT_OFFSET = TEXT_OFFSET+1;
    private static final byte FONT_STYLE_OFFSET = FONT_NAME_SIZE;
    private static final byte FONT_SIZE_OFFSET = FONT_STYLE_OFFSET+FONT_STYLE_SIZE;
    
    private static final byte FILL_OFFSET = COLOUR_OFFSET+COLOUR_SIZE;
    private static final byte BORDER_OFFSET = FILL_OFFSET+1;
    private static final byte BORDER_COL_OFFSET = BORDER_OFFSET+1;
    private static final byte BORDER_W_OFFSET = BORDER_COL_OFFSET+COLOUR_SIZE;
    
    //private final MessageType type;
    private final DrawMode mode;
    private final Point startPoint;
    private final Point endPoint;
    private final Color drawColour;
    private final int lineWeight;
    private final boolean fillShape;
    private final boolean hasBorder;
    private final int borderWeight;
    private final Color borderCol;
    private final Font font;
    private final char textChar;
    
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
    
    public DrawMode getDrawMode() { return this.mode; }
    
    public Point getStartPoint() { return this.startPoint; }
    
    public Point getEndPoint() { return this.endPoint; }
    
    public Color getColour() { return this.drawColour; }
    
    public int getLineWeight() { return this.lineWeight; }
    
    public boolean isFilled() { return this.fillShape; }
    
    public boolean hasBorder() { return this.hasBorder; }
    
    public Color getBorderColour() { return this.borderCol; }
    
    public int getBorderWeight() { return this.borderWeight; }
    
    public Font getFont() { return this.font; }
    
    public char getText() { return this.textChar; }
    
    public byte[] encode() { return this.toString().getBytes(); }
    
    public static WhiteboardMessage decodeMessage(byte[] message)
    {
        String messageStr = new String(message).trim();
        DrawMode m = DrawMode.parseChar(messageStr.charAt(0));
        
        Point p1 = stringToPoint(messageStr.substring(POINT_ONE_OFFSET, POINT_TWO_OFFSET));
        Point p2;
        Color col;
        int weight;
        
        switch (m) {
            case FREEFORM_LINE:
            case LINE:
            case POLYGON:
                p2 = stringToPoint(messageStr.substring(POINT_TWO_OFFSET, COLOUR_OFFSET));
                col = stringToColor(messageStr.substring(COLOUR_OFFSET, WEIGHT_OFFSET));
                weight = Integer.valueOf(messageStr.substring(WEIGHT_OFFSET));
                
                if (m == null || p1 == null || p2 == null || col == null || weight < 1)
                    return null;
                return new WhiteboardMessage(m, p1, p2, col, weight);
            case RECTANGLE:
                p2 = stringToPoint(messageStr.substring(POINT_TWO_OFFSET, COLOUR_OFFSET));
                col = stringToColor(messageStr.substring(COLOUR_OFFSET, FILL_OFFSET));
                
                String bVal = messageStr.substring(FILL_OFFSET, BORDER_OFFSET);
                boolean fill = bVal.equals("t");
                bVal = messageStr.substring(BORDER_OFFSET, BORDER_COL_OFFSET);
                boolean border = bVal.equals("t");
                
                Color borderCol = stringToColor(messageStr.substring(BORDER_COL_OFFSET, BORDER_W_OFFSET));
                weight = Integer.valueOf(messageStr.substring(BORDER_W_OFFSET));
                
                if (m == null || p1 == null || p2 == null || col == null || borderCol == null || weight < 1)
                    return null;
                return new WhiteboardMessage(m, p1, p2, col, weight, fill, border, borderCol);
            case TEXT:
                col = stringToColor(messageStr.substring(FONT_COL_OFFSET, TEXT_OFFSET));
                char text = messageStr.charAt(TEXT_OFFSET);
                Font font = stringToFont(messageStr.substring(FONT_OFFSET));
                
                if (m == null || p1 == null || font == null)
                    return null;
                return new WhiteboardMessage(m, p1, col, font, text);
        }
        
        return null;
    }
    
    private static String pointToString(Point p)
    {
        return String.format("%04d%04d", p.x, p.y);
    }
    
    private static String colorToString(Color c)
    {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        
        String s = String.format("%02x%02x%02x", r, g, b);
        return s;
    }
    
    private static String fontToString(Font f)
    {
        StringBuilder name = new StringBuilder(f.getName());
        int paddingNeeded = FONT_NAME_SIZE - name.length();
        for (int i = 0; i < paddingNeeded; i++)
            name.append(" ");
        
        return String.format("%s%d%02d", name.toString(), f.getStyle(), f.getSize());
    }
    
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
    
    private static int hexToInt(String hex)
    {
        if (hex.length() != 2) return -1;
        
        return Integer.parseInt(hex.trim(), 16);
    }
    
    private static Color stringToColor(String s)
    {
        if (s.length() != COLOUR_SIZE) {
            System.err.println("Provided String is not a hexadecimal colour.");
            return null;
        }
        
        String rChars = s.substring(0, 2);
        String gChars = s.substring(2, 4);
        String bChars = s.substring(4);
        
        int r = hexToInt(rChars);
        int g = hexToInt(gChars);
        int b = hexToInt(bChars);
        if (r < 0 || g < 0 || b < 0) {
            System.err.println("A colour component was negative. Invalid.");
            return null;
        }
        
        return new Color(r, g, b);
    }
    
    private static Font stringToFont(String s)
    {
        if (s.length() != FONT_SIZE) {
            System.err.println("Provided String is not a valid font.");
            return null;
        }
        
        String name = s.substring(0, FONT_NAME_SIZE).trim();
        int style = Integer.valueOf(s.substring(FONT_STYLE_OFFSET, FONT_SIZE_OFFSET));
        int size = Integer.valueOf(s.substring(FONT_SIZE_OFFSET));
        
        return new Font(name, style, size);
    }

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
    
    public enum MessageType
    {
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
