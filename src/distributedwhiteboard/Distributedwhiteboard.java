package distributedwhiteboard;

import distributedwhiteboard.gui.WhiteboardGUI;
import javax.swing.SwingUtilities;

/**
 *
 * @author Jordan
 */
public class Distributedwhiteboard 
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        for (DrawMode mode : DrawMode.values())
            System.out.println(mode);
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                WhiteboardGUI gui = WhiteboardGUI.getInstance();
            }
        });
    }
    
}
