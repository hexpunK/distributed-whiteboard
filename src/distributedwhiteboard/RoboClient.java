package distributedwhiteboard;

import distributedwhiteboard.gui.WhiteboardCanvas;
import distributedwhiteboard.gui.WhiteboardGUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.util.Random;

/**
 * An automated client that will draw a certain number (randomly chosen) of 
 * things to the Distributed Whiteboard network.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-03-27
 */
public class RoboClient implements Runnable
{
    /** A subset of the safe ports this {@link RoboClient} can use. */
    private static final int[] SAFE_PORTS = {55551, 55552, 55553, 55554, 55555};
    /** The maximum number of attempts to connect to the network to make. */
    private static final int MAX_RETRIES = 5;
    /** The upper limit of random shapes to draw. */
    private static final int MAX_RANDOMS = 50;
    /** The delay in ms between drawing operations. */
    private static final long DELAY = 100;
    /** An alphabet of safe characters to draw. */
    private static final char[] ALPHABET = {'A', 'B', 'C', 'D', 'E', 'F', 'G',
        'a', 'b', 'c', 'd', 'e', 'f', 'g'};
    /** A {@link Random} to generate random shape data through. */
    private final Random rand;
    
    /**
     * Creates a new {@link RoboClient}.
     * 
     * @since 1.0
     */
    public RoboClient()
    {
        this.rand = new Random();
    }

    /**
     * Runs the {@link RoboClient} drawing task in the background.
     * 
     * @since 1.0
     */
    @Override
    public void run()
    {
        System.out.println("Starting automated client.");
        int runs = 0;
        int runLimit = rand.nextInt(MAX_RANDOMS);
        
        Server server = Server.getInstance();
        Client client = Client.getInstance();
        
        int attempt = 0;
        while (!server.startServer()) {
            System.err.println("Couldn't use port. Trying another.");
            if (attempt >= MAX_RETRIES) return;
            server.setPort(SAFE_PORTS[rand.nextInt(SAFE_PORTS.length)]);
            attempt++;
        }
        client.startClient();
        while (!client.isEnabled()) {}
        DrawMode[] modes = DrawMode.values();
        WhiteboardGUI gui = WhiteboardGUI.getInstance();
        WhiteboardCanvas canvas = gui.getCanvas();
        
        // Wait 2 seconds to finish network discovery.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            System.err.println("Something interrupted the RoboClient.");
            System.err.println("Disconnecting from the network to be safe.");
            runLimit = -1;
        }
        
        System.out.printf("Creating %d random drawings%n", runLimit);
        while (runs <= runLimit) {
            DrawMode mode = modes[rand.nextInt(DrawMode.values().length)];
            
            switch (mode) {
                case LINE:
                case FREEFORM_LINE:
                case POLYGON:
                    drawRandomLine(canvas);
                    break;
                case RECTANGLE:
                    drawRandomRectangle(canvas);
                    break;
                case TEXT:
                    drawRandomText(canvas);
                    break;
                default:
            }
            runs++;
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException ex) { }
        }
        System.out.println("Automated client stopping.");
        WhiteboardGUI.getInstance().dispatchEvent(new WindowEvent(gui, 
                    WindowEvent.WINDOW_CLOSING));
    }
    
    /**
     * Draws a random line to the provided {@link WhiteboardCanvas}. The origin,
     *  end point, colour and weight of the line are chosen at complete random 
     * with the canvas size acting as an upper bound for positions.
     * 
     * @param canvas The {@link WhiteboardCanvas} to draw to.
     * @since 1.0
     */
    private void drawRandomLine(WhiteboardCanvas canvas)
    {
        int ranP1X = rand.nextInt(canvas.getWidth());
        int ranP1Y = rand.nextInt(canvas.getHeight());
        Point p1 = new Point(ranP1X, ranP1Y);
        
        int ranP2X = rand.nextInt(canvas.getWidth());
        int ranP2Y = rand.nextInt(canvas.getHeight());
        Point p2 = new Point(ranP2X, ranP2Y);
        
        float ranRed = rand.nextFloat();
        float ranGreen = rand.nextFloat();
        float ranBlue = rand.nextFloat();
        Color colour = new Color(ranRed, ranGreen, ranBlue);
        
        int weight = rand.nextInt(8);
        
        canvas.drawLine(p1, p2, colour, weight);
        // Send out a network message about this drawing.
        WhiteboardMessage msg = new WhiteboardMessage(p1, p2, colour, weight);
        msg.addUniqueID();
        Client.getInstance().broadCastMessage(msg);
        Server.messages.put(msg.getUniqueID(), msg);
    }
    
    /**
     * Draws a random rectangle to the specified {@link WhiteboardCanvas}. The 
     * origin, size, colour, border settings and shape fill are randomly 
     * chosen.
     * 
     * @param canvas The {@link WhiteboardCanvas} to draw on.
     * @since 1.0
     */
    private void drawRandomRectangle(WhiteboardCanvas canvas)
    {
        int ranOriginX = rand.nextInt(canvas.getWidth());
        int ranOriginY = rand.nextInt(canvas.getHeight());
        Point origin = new Point(ranOriginX, ranOriginY);
        
        int ranSizeX = rand.nextInt(canvas.getWidth());
        int ranSizeY = rand.nextInt(canvas.getHeight());
        Point size = new Point(ranSizeX, ranSizeY);
        
        float ranRed = rand.nextFloat();
        float ranGreen = rand.nextFloat();
        float ranBlue = rand.nextFloat();
        Color colour = new Color(ranRed, ranGreen, ranBlue);
        
        boolean filled = rand.nextBoolean();
        boolean border = rand.nextBoolean();
        int weight = rand.nextInt(8);
        ranRed = rand.nextFloat();
        ranGreen = rand.nextFloat();
        ranBlue = rand.nextFloat();
        Color borderCol = new Color(ranRed, ranGreen, ranBlue);
        
        WhiteboardMessage msg = new WhiteboardMessage(origin, size, 
                    colour, weight, filled, border, borderCol);
        // Calculate the rectangle size.
        Dimension rectSize = new Dimension(
            size.x - origin.x, 
            size.y - origin.y
        );
            
        // Adjust the origin point to allow for negative widths and heights.
        if (rectSize.width < 0 && rectSize.height > 0) { 
            origin = new Point(size.x, origin.y);
        } else if (rectSize.width > 0 && rectSize.height < 0) { 
            origin = new Point(origin.x, size.y);
        } else if (rectSize.width < 0 && rectSize.height < 0) { 
            origin = new Point(size.x, size.y);
        }
        Client.getInstance().broadCastMessage(msg);
        Server.messages.put(msg.getUniqueID(), msg);
        canvas.drawRectangle(origin, rectSize, colour, filled, border, weight, 
                borderCol);
    }
    
    /**
     * Writes random text to the {@link WhiteboardCanvas} specified. The origin,
     *  colour and character drawn are randomly chosen. The {@link Font} used is
     *  fixed to "Serif-Plain size: 14".
     * 
     * @param canvas The {@link WhiteboardCanvas} to draw the text on.
     * @since 1.0
     */
    private void drawRandomText(WhiteboardCanvas canvas)
    {
        int ranOriginX = rand.nextInt(canvas.getWidth());
        int ranOriginY = rand.nextInt(canvas.getHeight());
        Point origin = new Point(ranOriginX, ranOriginY);
        
        char text = ALPHABET[rand.nextInt(ALPHABET.length)];
        
        float ranRed = rand.nextFloat();
        float ranGreen = rand.nextFloat();
        float ranBlue = rand.nextFloat();
        Color colour = new Color(ranRed, ranGreen, ranBlue);
        
        Font font = new Font("Serif", Font.PLAIN, 14);
        
        canvas.drawText(text, origin, font, colour);
        WhiteboardMessage msg = new WhiteboardMessage(origin, colour, font, 
                text);
        msg.addUniqueID();
        Client.getInstance().broadCastMessage(msg);
        Server.messages.put(msg.getUniqueID(), msg);
    }
}
