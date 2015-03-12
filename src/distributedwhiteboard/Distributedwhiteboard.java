package distributedwhiteboard;

import distributedwhiteboard.gui.WhiteboardGUI;
import javax.swing.SwingUtilities;

/**
 * Starts the UDP listener {@link Server} and the {@link WhiteboardGUI}.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-03-10
 */
public class Distributedwhiteboard 
{

    /**
     * Main entry point.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        // Print the available drawing modes.
        System.out.println("Drawing Modes:");
        for (DrawMode mode : DrawMode.values())
            System.out.println(mode);
        System.out.println();
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                WhiteboardGUI.getInstance();
            }
        });
        
        Server server = Server.getInstance();
        if (!server.startServer())
            return;
        
        int totes = 0;
        while (server.isRunning()) {
            if (totes > 20)
                server.stopServer();
            try {
                Thread.sleep(200);
            } catch (Exception e) {}
            totes += 1;
        }
    }
}
