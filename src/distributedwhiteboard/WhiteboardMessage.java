package distributedwhiteboard;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.io.Serializable;
import static distributedwhiteboard.Conversions.*;

/**
 * Contains the details of a message being sent between two distributed 
 * whiteboards. The contents of the message includes the {@link DrawMode} to use
 * , the start and end {@link Point}s and the drawing {@link Color}.
 * 
 * @author 6266215
 * @version 1.2
 * @since 2015-03-15
 */
public class WhiteboardMessage extends NetMessage implements Serializable
{
    /** Serialisation ID. */
    private static final long serialVersionUID = 5459762541371665893L;
    
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
    /** The scale of the image as a multiplier. Clamped between 0.0  and 1.0. */
    public final float imageScale;
    public final int imageHash;
    
    /**
     * Creates a {@link WhiteboardMessage} with no contents, this is usually 
     * used to work out the maximum size of a {@link WhiteboardMessage}.
     * 
     * @since 1.0
     */
    public WhiteboardMessage()
    {
        super(MessageType.DRAW);
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
        this.imageScale = 0;
        this.imageHash = 0;
    }
    
    /**
     * Creates a new {@link WhiteboardMessage} that contains a message to draw 
     * between two points.
     * 
     * @param p1 The starting {@link Point} for this drawing.
     * @param p2 The ending {@link Point} for this drawing.
     * @param drawCol The {@link Color} to draw with.
     * @param weight The thickness of the lines used when drawing.
     * @since 1.0
     */
    public WhiteboardMessage(Point p1, Point p2, Color drawCol, int weight)
    {
        super(MessageType.DRAW);
        this.mode = DrawMode.LINE;
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
        this.imageScale = 0;
        this.imageHash = 0;
    }
    
    /**
     * Creates a new {@link WhiteboardMessage} that contains a message to draw 
     * between two points with a border and/or a fill.
     * 
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
    public WhiteboardMessage(Point p1,  Point p2, Color drawCol, 
            int weight, boolean fill, boolean border, Color bCol)
    {
        super(MessageType.DRAW);
        this.mode = DrawMode.RECTANGLE;
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
        this.imageScale = 0;
        this.imageHash = 0;
    }
    
    /**
     * Creates a new {@link WhiteboardMessage} that contains a message to draw 
     * the specified character in the provided {@link Font}.
     * 
     * @param p1 The origin for the text to render from as a {@link Point}.
     * @param drawCol The {@link Color} to draw the text with.
     * @param f The {@link Font} for the text.
     * @param text A character to draw using the previous specified settings.
     * @since 1.0
     */
    public WhiteboardMessage(Point p1, Color drawCol, Font f, char text)
    {
        super(MessageType.DRAW);
        this.mode = DrawMode.TEXT;
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
        this.imageScale = 0;
        this.imageHash = 0;
    }
    
    /**
     * Creates a {@link WhiteboardMessage} for drawing an image between two 
     * specified points. The origin is specified in p1, and the end point is 
     * specified in p2. This message does not contain the image data itself, 
     * instead it indicates that the {@link Server} should start listening on 
     * TCP for an image.
     * 
     * @param p1 The top-left corner position of the image as a {@link Point}.
     * @param scale The scaling of the image. This mode must be between 0 and 
     * 100, and will be divided by 100 to create a multiplier.
     * @param hash The unique hash code for the image to be drawn.
     * @since 1.2
     */
    public WhiteboardMessage(Point p1, int scale, int hash)
    {
        super(MessageType.DRAW);
        this.mode = DrawMode.IMAGE;
        this.startPoint = p1;
        this.endPoint = new Point();
        this.drawColour = Color.BLACK;
        this.lineWeight = 1;
        this.fillShape = false;
        this.hasBorder = false;
        this.borderWeight = 1;
        this.borderCol = Color.LIGHT_GRAY;
        this.font = new Font("Serif", Font.PLAIN, 12);
        this.textChar = '\0';
        this.imageScale = scale/100.0f;
        this.imageHash = hash;
    }
        
    /**
     * Encodes this {@link WhiteboardMessage} into a byte array so it can be 
     * sent across networks. The array is generated from the {@link String} 
     * representation of this message.
     * 
     * @return Returns a byte array representation of this message.
     * @since 1.0
     */
    @Override
    public byte[] encode() { return this.toString().getBytes(); }
    
    /**
     * Gets the largest possible size of a {@link WhiteboardMessage} so that the
     *  receiving packet buffer can be made large enough to hold any possible 
     * messages.
     * 
     * @return The maximum size of a {@link WhiteboardMessage} as an int.
     * @since 1.2
     */
    public static int getLargestSize() 
    { 
        return new WhiteboardMessage().encode().length; 
    }
    
    /**
     * Converts a provided byte array into a {@link WhiteboardMessage} if it 
     * contains all the required information.
     * 
     * @param message The byte array to convert to a new message.
     * @return Returns a new {@link WhiteboardMessage} if the byte array is 
     * properly formed, null otherwise.
     * @since 1.0
     */
    public static WhiteboardMessage decode(byte[] message)
    {
        String messageStr = new String(message).trim();
        String uID = messageStr.substring(ID_OFFSET, RELY_OFFSET);
        String rID = messageStr.substring(RELY_OFFSET, TYPE_OFFSET);
        DrawMode m = DrawMode.parseChar(messageStr.charAt(TYPE_OFFSET+1));
        WhiteboardMessage msg;
        
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
                
                if (m == null || p1 == null || p2 == null || col == null)
                    return null;
                
                msg = new WhiteboardMessage(p1, p2, col, weight);
                msg.setUniqueID(uID);
                msg.setRequiredID(rID);
                return msg;
            case RECTANGLE:
                p2 = stringToPoint(messageStr.substring(POINT_TWO_OFFSET, 
                        COLOUR_OFFSET));
                col = stringToColor(messageStr.substring(COLOUR_OFFSET, 
                        FILL_OFFSET));
                
                // Work out if the boolean values were encoded to true or false.
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
                        || borderCol == null)
                    return null;
                
                msg = new WhiteboardMessage(p1, p2, col, weight, fill, 
                        border, borderCol);
                msg.setUniqueID(uID);
                msg.setRequiredID(rID);
                return msg;
            case TEXT:
                col = stringToColor(messageStr.substring(FONT_COL_OFFSET, 
                        TEXT_OFFSET));
                char text = messageStr.charAt(TEXT_OFFSET);
                Font font = stringToFont(messageStr.substring(FONT_OFFSET,
                        FONT_OFFSET+FONT_SIZE));
                
                if (m == null || p1 == null || font == null)
                    return null;
                
                msg = new WhiteboardMessage(p1, col, font, text);
                msg.setUniqueID(uID);
                msg.setRequiredID(rID);
                return msg;
            case IMAGE:
                String sclStr = messageStr.substring(POINT_TWO_OFFSET, 
                        POINT_TWO_OFFSET+3);
                String hashStr = messageStr.substring(POINT_TWO_OFFSET+3);
                
                if (sclStr == null || sclStr.isEmpty()) return null;
                int scaling;
                try {
                    scaling = Integer.parseInt(sclStr);
                } catch (NumberFormatException nfe) {
                    System.err.println("Scaling value was not a number.");
                    return null;
                }
                
                if (hashStr == null || hashStr.isEmpty()) return null;
                int hash;
                try {
                    hash = Integer.parseInt(hashStr);
                } catch (NumberFormatException nfe) {
                    System.err.println("Image hash value was not a number.");
                    return null;
                }
                
                msg = new WhiteboardMessage(p1, scaling, hash);
                msg.setUniqueID(uID);
                msg.setRequiredID(rID);
                return msg;                
        }
        
        return null;
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
        
        sb.append(super.toString());
        sb.append(mode.mode);
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
            case IMAGE:
                sb.append(String.format("%03d", (int)(imageScale*100)));
                sb.append(String.format("%010d", imageHash));
        }
        
        return sb.toString();
    }

    /**
     * Compares this {@link WhiteboardMessage} against another {@link Object}.
     * 
     * @param obj The {@link Object} to compare against.
     * @return Returns true if the comparing {@link Object} is a {@link 
     * WhiteboardMessage} and if {@link NetMessage#equals(java.lang.Object)} 
     * also returns true. Returns false otherwise.
     * @since 1.2
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (!(obj instanceof WhiteboardMessage)) return false;
        return super.equals(obj);
    }

    /**
     * Returns the hash code for this {@link WhiteboardMessage} based on the 
     * hash code of the underlying {@link NetMessage}.
     * 
     * @return Returns the hash code for this {@link WhiteboardMessage} as 
     * generated by {@link NetMessage#hashCode()}.
     * @since 1.2
     */
    @Override
    public int hashCode()
    {
        return super.hashCode();
    }
}
