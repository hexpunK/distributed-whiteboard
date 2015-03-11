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
 *
 * @author Jordan
 */
public class WhiteboardCanvas extends JPanel implements Runnable
{
    private static final long serialVersionUID = 973789183742060090L;
    private final BufferedImage canvas;
    private final Thread repainter;
    
    public WhiteboardCanvas(int width, int height)
    {
        super();
        this.canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = this.canvas.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        this.setFocusable(true);
        this.repainter = new Thread(this);
        this.repainter.setName("Whiteboard Repainter");
        this.repainter.setPriority(Thread.MIN_PRIORITY);
        this.repainter.start();
    }
    
    public Point drawLine(Point start, Point end, Color colour, int size)
    {
        Graphics2D g = (Graphics2D)canvas.getGraphics();
        if (colour != null)
            g.setColor(colour);
        if (size > 0)
            g.setStroke(new BasicStroke(size));
        
        g.drawLine(start.x, start.y, end.x, end.y);
        this.repaint();
        return end;
    }
    
    public Point drawText(String text, Point point, Font font, Color colour)
    {
        Graphics2D g = (Graphics2D)canvas.getGraphics();
        Font f = new Font("Serif", Font.PLAIN, 12);
        if (colour != null)
            g.setColor(colour);
        if (font != null)
            f = font;
        
        g.setFont(f);
        g.drawString(text, point.x, point.y);
        FontMetrics metrics = g.getFontMetrics();
        
        Point nextPoint = new Point(point);
        nextPoint.x += metrics.stringWidth(text);
        
        this.repaint();
        return nextPoint;
    }
    
    public Point drawRectangle(Point origin, Dimension size, Color colour, 
            boolean fillShape, boolean border, int weight, Color borderColour)
    {
        size = new Dimension(
                Math.abs(size.width), 
                Math.abs(size.height)
        );
        System.out.printf("Drawing rectangle.\n\tPos - x: %d y: %d, w : %d, h: %d\n", origin.x, origin.y, size.width, size.height);
        Rectangle rect = new Rectangle(origin, size);
        drawShape(rect, colour, fillShape, border, weight, borderColour);
        
        Point result = new Point(origin);
        result.x += size.width;
        result.y += size.height;
        return result;
    }
    
    public void drawShape(Shape s, Color colour, boolean fillShape, 
            boolean border, int weight, Color borderColour)
    {
        Graphics2D g = (Graphics2D)canvas.getGraphics();
        if (colour != null)
            g.setColor(colour);
        
        if (fillShape)
            g.fill(s);
        else
            g.draw(s);
        
        if (border) {
            if (borderColour != null)
                g.setColor(borderColour);
            g.setStroke(new BasicStroke(weight));
            g.draw(s);
        }
        this.repaint();
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(canvas.getWidth(), canvas.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        Graphics2D graphics = (Graphics2D)g.create();
        Map<Key, Object> hintMap = new HashMap<>();
        hintMap.put(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        hintMap.put(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        RenderingHints hints = new RenderingHints(hintMap);
        graphics.setRenderingHints(hints);
        graphics.drawImage(canvas, 0, 0, null);
    }

    @Override
    public void run()
    {
        while (true) {
            repaint();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                
            }
        }
    }
}
