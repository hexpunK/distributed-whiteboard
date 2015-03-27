package distributedwhiteboard;

import distributedwhiteboard.gui.WhiteboardGUI;
import javax.swing.SwingUtilities;

/**
 * Starts the {@link WhiteboardGUI}.
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
        boolean runAutomated = false;
        
        for(String arg : args) {
            if (arg.equals("--robot") || arg.equals("-r")) {
                runAutomated = true;
                break;
            }
        }
        
        if (runAutomated) {
            System.out.println("Running automated Whiteboard client.");
            RoboClient robot = new RoboClient();
            Thread robotThread = new Thread(robot);
            robotThread.start();
            try {
                robotThread.join();
            } catch (InterruptedException ex) {
                System.err.println("Robot interrupted.");
            }
        } else {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    WhiteboardGUI.getInstance();
                }
            });
        }
    }
}
