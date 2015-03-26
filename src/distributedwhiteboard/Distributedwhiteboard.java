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
