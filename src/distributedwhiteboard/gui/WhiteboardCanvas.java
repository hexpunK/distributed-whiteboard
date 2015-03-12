package distributedwhiteboard.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

/**
 * Provides a canvas for the user or application to draw to. This canvas is 
 * based on the {@link BufferedImage} class, allowing it to be saved if needed.
 * 
 * @author 6266215
 * @version 1.2
 * @since 2015-03-12
 */
public class WhiteboardCanvas extends JPanel
{
    /** Serialisation ID. */
    private static final long serialVersionUID = 973789183742060090L;
    /** The {@link BufferedImage} to draw to. */
    private final BufferedImage canvas;
    
    /**
     * Creates a new instance of the {@link WhiteboardCanvas} class, with the 
     * specified width and height for the image.
     * 
     * @param width The width of the canvas as an int.
     * @param height The height of the canvas as an int.
     * @since 1.0
     */
    public WhiteboardCanvas(int width, int height)
    {
        super();
        this.canvas = new BufferedImage(width, height, 
                BufferedImage.TYPE_INT_RGB);
        Graphics g = this.canvas.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        this.setFocusable(true);
    }
    
    /**
     * Gets the {@link BufferedImage} that this {@link WhiteboardCanvas} is 
     * drawing to.
     * 
     * @return The canvas image as a {@link BufferedImage}.
     * @since 1.2
     */
    public BufferedImage getImage() { return this.canvas; }
    
    /**
     * Draws a line on the canvas between the specified points with the provided
     *  colour and line weight.
     * 
     * @param start The starting point to draw from as a {@link Point}.
     * @param end The ending point to draw to as a {@link Point}.
     * @param colour The colour of the line as an {@link Color}. If null is 
     * provided, the line will draw using {@link Color#BLACK}.
     * @param size The width of the line as an int. A size of zero or less will 
     * be ignored and replace with '1'.
     * @return Returns the {@link Point} this line stopped drawing at.
     * @since 1.0
     */
    public Point drawLine(Point start, Point end, Color colour, int size)
    {
        Graphics2D g = (Graphics2D)canvas.getGraphics();
        Color col = Color.BLACK;
        int weight = 1;
        if (colour != null)
            col = colour;
        if (size > 0)
            weight = size;
        
        g.setColor(col);
        g.setStroke(new BasicStroke(weight));
        g.drawLine(start.x, start.y, end.x, end.y);
        this.repaint();
        return end;
    }
    
    /**
     * Draws text to the canvas with the specified {@link Font} and 
     * {@link Color}.
     * 
     * @param text The text to display as a {@link String}.
     * @param point The point to draw the text at as an {@link Point}.
     * @param font The font settings (family, size, style) as a {@link Font}. If
     *  null is provided, the text will use 'Serif' at 12pt.
     * @param colour The colour to draw the text in as a {@link Color}. If null 
     * is provided, the text will draw with {@link Color#BLACK}.
     * @return Returns the point that this text stopped drawing at.
     * @since 1.0
     */
    public Point drawText(String text, Point point, Font font, Color colour)
    {
        Graphics2D g = (Graphics2D)canvas.getGraphics();
        Font f = new Font("Serif", Font.PLAIN, 12);
        Color col = Color.BLACK;
        if (colour != null)
            col = colour;
        if (font != null)
            f = font;
        
        g.setColor(col);
        g.setFont(f);
        g.drawString(text, point.x, point.y);
        FontMetrics metrics = g.getFontMetrics();
        
        Point nextPoint = new Point(point);
        nextPoint.x += metrics.stringWidth(text);
        
        this.repaint();
        return nextPoint;
    }
    
    /**
     * Draws a {@link Rectangle} to this canvas.
     * 
     * @param origin The starting {@link Point} for drawing this rectangle.
     * @param size The size of the {@link Rectangle} as a {@link Dimension}.
     * @param colour The {@link Color} to draw this rectangle with. If null 
     * is provided, {@link Color#BLACK} will be used instead.
     * @param fillShape If set to true, the rectangle will be draw with the 
     * selected colour filling in the empty areas. If false, the rectangle will 
     * just draw an outline.
     * @param border If true, the rectangle will draw with a border.
     * @param weight The thickness of the border as an int. If a value zero or 
     * lower is passed in, the border will use a thickness of '1'.
     * @param borderColour The colour to draw the border with. If null is 
     * provided, {@link Color#LIGHT_GRAY} will be used instead.
     * @return Returns the {@link Point} this rectangle stopped drawing at.
     * @since 1.1
     */
    public Point drawRectangle(Point origin, Dimension size, Color colour, 
            boolean fillShape, boolean border, int weight, Color borderColour)
    {
        size = new Dimension(
                Math.abs(size.width), 
                Math.abs(size.height)
        );
        Rectangle rect = new Rectangle(origin, size);
        drawShape(rect, colour, fillShape, border, weight, borderColour);
        
        Point result = new Point(origin);
        result.x += size.width;
        result.y += size.height;
        return result;
    }
    
    /**
     * Draws a AWT {@link Shape} object to the canvas. This is a generic version
     *  of the method that has a few wrapper implementations such as 
     * {@link WhiteboardCanvas#drawRectangle(java.awt.Point, java.awt.Dimension,
     *  java.awt.Color, boolean, boolean, int, java.awt.Color)}.
     * 
     * @param s The {@link Shape} to draw.
     * @param colour The {@link Color} to draw this {@link Shape} with. If null 
     * is provided, {@link Color#BLACK} will be used instead.
     * @param fillShape If set to true, the shape will be draw with the selected
     *  colour filling in the empty areas. If false, the shape will just draw an
     *  outline.
     * @param border If true, the shape will draw with a border.
     * @param weight The thickness of the border as an int. If a value zero or 
     * lower is passed in, the border will use a thickness of '1'.
     * @param borderColour The colour to draw the border with. If null is 
     * provided, {@link Color#LIGHT_GRAY} will be used instead.
     * @since 1.1
     */
    public void drawShape(Shape s, Color colour, boolean fillShape, 
            boolean border, int weight, Color borderColour)
    {
        Graphics2D g = (Graphics2D)canvas.getGraphics();
        Color col = Color.BLACK;
        Color borderCol = Color.LIGHT_GRAY;
        int borderWeight = 1;
        
        if (colour != null)
            col = colour;
        if (borderColour != null)
            borderCol = borderColour;
        if (weight > 0)
            borderWeight = weight;
        
        g.setColor(col);
        if (fillShape)
            g.fill(s);
        else
            g.draw(s);
        
        if (border) {
            g.setColor(borderCol);
            g.setStroke(new BasicStroke(borderWeight));
            g.draw(s);
        }
        this.repaint();
    }

    /**
     * Gets the size this {@link WhiteboardCanvas} would prefer to use as a 
     * {@link Dimension}. This size is calculated based on the width and height 
     * of the canvas used internally.
     * 
     * @return The {@link Dimension} containing the width and height of this 
     * {@link WhiteboardCanvas}.
     * @since 1.0
     */
    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Paints the contents of the {@link BufferedImage} canvas to the component.
     * 
     * @param g The {@link Graphics} instance to draw with.
     * @since 1.0
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        Graphics2D graphics = (Graphics2D)g.create();
        Map<Key, Object> hintMap = new HashMap<>();
        // Set any rendering hints.
        hintMap.put(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        hintMap.put(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        
        RenderingHints hints = new RenderingHints(hintMap);
        graphics.setRenderingHints(hints);
        graphics.drawImage(canvas, 0, 0, null);
    }
}
