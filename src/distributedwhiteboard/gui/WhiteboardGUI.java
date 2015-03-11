package distributedwhiteboard.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Jordan
 */
public class WhiteboardGUI extends JFrame
{
    private static final WhiteboardGUI INSTANCE = new WhiteboardGUI();
    private static final long serialVersionUID = -4404291511660285311L;
    private final SpringLayout layout;
    private final WhiteboardCanvas canvas;
    private final WhiteboardControls controls;
    private final JScrollPane scroller;
    
    private WhiteboardGUI()
    {
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
        this.canvas = new WhiteboardCanvas(800, 600);
        this.controls = new WhiteboardControls(canvas);
        this.controls.setupLayout();
        this.scroller = new JScrollPane(canvas);
        this.scroller.setBackground(Color.LIGHT_GRAY);
        
        contentPane.add(controls);
        this.layout.putConstraint(SpringLayout.WEST, controls, 5, SpringLayout.WEST, contentPane);
        this.layout.putConstraint(SpringLayout.SOUTH, controls, -5, SpringLayout.SOUTH, contentPane);
        this.layout.putConstraint(SpringLayout.EAST, controls, -5, SpringLayout.EAST, contentPane);
        contentPane.add(scroller);
        this.layout.putConstraint(SpringLayout.NORTH, scroller, 5, SpringLayout.NORTH, contentPane);
        this.layout.putConstraint(SpringLayout.WEST, scroller, 5, SpringLayout.WEST, contentPane);
        this.layout.putConstraint(SpringLayout.SOUTH, scroller, -5, SpringLayout.NORTH, controls);
        this.layout.putConstraint(SpringLayout.EAST, scroller, -5, SpringLayout.EAST, contentPane);
        
        this.setContentPane(contentPane);
        this.setTitle("Distributed Whiteboard");
        this.setSize(1024, 768);
        this.setLocationByPlatform(true);
        this.setResizable(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
    
    public static WhiteboardGUI getInstance()
    {
        return WhiteboardGUI.INSTANCE;
    }
}
