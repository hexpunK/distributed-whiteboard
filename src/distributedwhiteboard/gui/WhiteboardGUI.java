package distributedwhiteboard.gui;

import distributedwhiteboard.Client;
import distributedwhiteboard.WhiteboardServer;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * The main GUI for the Distributed Whiteboard application. Holds an instance of
 *  {@link WhiteboardCanvas} to allow a user to draw and view drawings from 
 * other networked users.
 * 
 * @author 6266215
 * @version 1.1
 * @since 2015-03-12
 */
public class WhiteboardGUI extends JFrame implements Runnable 
{
    /** Serialisation ID. */
    private static final long serialVersionUID = -4404291511660285311L;
    /** Singleton instance of this {@link WhiteboardGUI}. */
    private static final WhiteboardGUI INSTANCE = new WhiteboardGUI();
    /** The layout manager for the components. */
    private final SpringLayout layout;
    /** The menu bar for this {@link WhiteboardGUI}. */
    private final WhiteboardMenu menu;
    /** A {@link WhiteboardCanvas} to allow the user to draw. */
    private final WhiteboardCanvas canvas;
    /** A set of {@link WhiteboardControls} for the {@link WhiteboardCanvas}. */
    private final WhiteboardControls controls;
    /** A {@link JScrollPane} to hold the canvas in case it's too large. */
    private final JScrollPane scroller;
    /** Repaints the GUI constantly in the background. */
    private Thread repainter;
    
    /**
     * Creates a new instance of the {@link WhiteboardGUI}, setting up all the 
     * components and the containing frame. This is private as the class is a 
     * singleton to prevent multiple windows being open simultaneously from a 
     * single client.
     * 
     * @since 1.0
     */
    private WhiteboardGUI()
    {
        // Set the look and feel to the system style if possible.
        String sysFeel = UIManager.getSystemLookAndFeelClassName();
        String crossFeel = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(sysFeel);
        } catch (ClassNotFoundException 
                | InstantiationException 
                | IllegalAccessException 
                | UnsupportedLookAndFeelException ex) {
            System.err.println("Couldn't load system look and feel. "
                    + "Reverting to cross-platform.");
            try {
                UIManager.setLookAndFeel(crossFeel);
            } catch (ClassNotFoundException 
                    | InstantiationException 
                    | IllegalAccessException 
                    | UnsupportedLookAndFeelException innerEx) {
                System.err.println("Couldn't load cross-platform look and "
                        + "feel.");
            }
        }
        
        this.layout = new SpringLayout();
        Container contentPane = this.getContentPane();
        contentPane.setLayout(layout);
        
        this.menu = new WhiteboardMenu(this);
        this.setJMenuBar(this.menu);
        
        this.canvas = new WhiteboardCanvas(800, 800);
        this.controls = new WhiteboardControls(canvas);
        
        this.scroller = new JScrollPane(canvas);
        this.scroller.setBackground(Color.LIGHT_GRAY);
        
        // Add the canvas and controls to the main GUI. Canvas above controls.
        contentPane.add(controls);
        this.layout.putConstraint(SpringLayout.WEST, controls, 5, 
                SpringLayout.WEST, contentPane);
        this.layout.putConstraint(SpringLayout.SOUTH, controls, -5, 
                SpringLayout.SOUTH, contentPane);
        this.layout.putConstraint(SpringLayout.EAST, controls, -5, 
                SpringLayout.EAST, contentPane);
        
        contentPane.add(scroller);
        this.layout.putConstraint(SpringLayout.NORTH, scroller, 5, 
                SpringLayout.NORTH, contentPane);
        this.layout.putConstraint(SpringLayout.WEST, scroller, 5, 
                SpringLayout.WEST, contentPane);
        this.layout.putConstraint(SpringLayout.SOUTH, scroller, -5, 
                SpringLayout.NORTH, controls);
        this.layout.putConstraint(SpringLayout.EAST, scroller, -5, 
                SpringLayout.EAST, contentPane);
        
        // Set up the rest of the JFrame.
        this.setContentPane(contentPane);
        this.setTitle("Distributed Whiteboard");
        this.setSize(828, 893);
        this.setLocationByPlatform(true);
        this.setResizable(true);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);
        //startWindow();
    }
    
    /**
     * Gets the singleton instance of {@link WhiteboardGUI} for this application
     *  instance.
     * 
     * @return The {@link WhiteboardGUI} singleton instance.
     * @since 1.0
     */
    public static WhiteboardGUI getInstance()
    {
        return WhiteboardGUI.INSTANCE;
    }
    
    /**
     * Creates a new {@link ImageIcon} from the specified path.
     *
     * @param path The file path and file name of the image to load.
     * @return A new {@link ImageIcon} instance.
     * @since 1.1
     */
    public static ImageIcon createIcon(String path)
    {
        return WhiteboardGUI.createIcon(path, "");
    }
    
    /**
     * Creates a new {@link ImageIcon} from the specified path and sets the
     * description in the image to the specified String.
     *
     * @param path The file path and file name of the image to load.
     * @param description The description of the image.
     * @return A new {@link ImageIcon} instance.
     * @since 1.1
     */
    public static ImageIcon createIcon(String path, String description)
    {
        java.net.URL imgURL = WhiteboardGUI.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    /**
     * Starts the repainting thread and completes any extra initialisation that 
     * cannot be done in the constructor.
     * 
     * @since 1.1
     */
    private void startWindow()
    {
        // Repaint the window constantly due to a weird rendering bug on Win.
        repainter = new Thread(this);
        repainter.setName("Whiteboard Repainter");
        repainter.setPriority(Thread.MIN_PRIORITY);
        repainter.start();
    }
    
    /**
     * Saves the current {@link WhiteboardCanvas} to a file of the specified 
     * {@link WhiteboardMenu.SaveType}.
     * 
     * @param file A {@link File} to write to.
     * @param type The {@link WhiteboardMenu.SaveType} to specify the format of 
     * the saved canvas.
     * @return Returns true if the canvas was saved, false otherwise.
     * @since 1.1
     */
    public boolean saveCanvas(File file, WhiteboardMenu.SaveType type)
    {
        switch (type) {
            case BMP:
            case PNG:
            case GIF:
            case JPEG:
                break;
            case UNSUPPORTED:
            default:
                return false;
        }
            
        BufferedImage img = canvas.getImage();
        try {
            ImageIO.write(img, type.name().toLowerCase(), file);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets the current {@link WhiteboardCanvas} this {@link WhiteboardGUI} is 
     * drawing to.
     * 
     * @return A {@link WhiteboardCanvas} instance.
     * @since 1.1
     */
    public WhiteboardCanvas getCanvas() { return this.canvas; }

    /**
     * Stop the repainting thread gracefully when the window sends the 
     * {@link WindowEvent#WINDOW_CLOSING} event.
     * 
     * @param e The {@link WindowEvent} sent by this {@link WhiteboardGUI}.
     * @since 1.0
     */
    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            WhiteboardServer server = WhiteboardServer.getInstance();
            Client client = Client.getInstance();
            if (server != null)
                server.stopServer();
            if (client != null)
                client.stopClient();
            if (this.repainter != null) {
                try {
                    WhiteboardGUI.this.repainter.interrupt();
                    WhiteboardGUI.this.repainter.join();
                    System.out.println("Repainter stopped.");
                } catch (InterruptedException ex) {
                    System.err.println("Failed to stop repainter gracefully.");
                }
            }
            dispose();
        }
    }
    
    /**
     * Repaints the entire window every 10 milliseconds to solve a rendering 
     * error on Windows.
     * 
     * @since 1.0
     */
    @Override
    public void run()
    {
        while (true) {
            repaint();
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) { 
                break;
            }
        }
    }
}
