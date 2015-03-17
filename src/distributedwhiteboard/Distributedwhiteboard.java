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
        int port = 55551;
        
        for (int i = 0; i < args.length; i++) {
            switch(args[i]) {
                case "-p":
                case "--port":
                    // Allow the port to be specified.
                    try {
                        port = Integer.parseInt(args[i+1]);
                        i++;
                    } catch (NumberFormatException nEx) {
                        System.err.println("Port value must be a number.");
                    }
                    break;
            }
        }
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                WhiteboardGUI.getInstance();
            }
        });
        
        try {
            Server.getInstance(port);
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
