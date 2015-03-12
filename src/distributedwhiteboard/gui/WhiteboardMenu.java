package distributedwhiteboard.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A {@link JMenuBar} extension that provides the core controls for the 
 * {@link WhiteboardGUI} menu bar. As with JMenuBar this can be added to if 
 * extra buttons are needed.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-03-12
 */
public final class WhiteboardMenu extends JMenuBar implements ActionListener
{
    /** The owning component. */
    private final Component parent;
    // The various menus.
    private final JMenu fileMenu, helpMenu, saveMenu;
    // File menu items.
    private final JMenuItem connectItem, disconnectItem, exitItem;
    // A listing of all supported image types to save to.
    private final ArrayList<JMenuItem> saveItems;
    // Help menu iems.
    private final JMenuItem aboutItem, helpItem;
    
    /**
     * Creates a new instance of {@link WhiteboardMenu} with a file and help 
     * menu set up.
     * 
     * @param parent The {@link Component} that this {@link WhiteboardMenu} is 
     * attached to.
     * @since 1.0
     */
    public WhiteboardMenu(Component parent)
    {
        super();
        
        this.parent = parent;
        
        saveItems = new ArrayList<>();
        for (SaveType type : SaveType.values()) {
            if (type == SaveType.UNSUPPORTED) continue;
            
            JMenuItem saveItem = new JMenuItem(type.toString());
            saveItem.setMnemonic(type.getMnemonic());
            saveItems.add(saveItem);
        }
        
        this.saveMenu = new JMenu("Save As...");
        this.saveMenu.setMnemonic('s');
        for (JMenuItem saveItem : saveItems)
            this.saveMenu.add(saveItem);
        
        this.connectItem = new JMenuItem("Connect");
        this.connectItem.setMnemonic('c');
        
        this.disconnectItem = new JMenuItem("Disconnect");
        this.disconnectItem.setMnemonic('d');
        
        this.exitItem = new JMenuItem("Exit");
        this.exitItem.setMnemonic('x');
        
        this.fileMenu = new JMenu("File");
        this.fileMenu.setMnemonic('f');
        this.fileMenu.add(this.connectItem);
        this.fileMenu.add(this.disconnectItem);
        this.fileMenu.add(new JSeparator());
        this.fileMenu.add(this.saveMenu);
        this.fileMenu.add(new JSeparator());
        this.fileMenu.add(this.exitItem);
        
        this.aboutItem = new JMenuItem("About");
        this.aboutItem.setMnemonic('a');
        
        this.helpItem = new JMenuItem("Guide");
        this.helpItem.setMnemonic('g');
        
        this.helpMenu = new JMenu("Help");
        this.helpMenu.setMnemonic('h');
        this.helpMenu.add(this.aboutItem);
        this.helpMenu.add(new JSeparator());
        this.helpMenu.add(this.helpItem);
        
        this.add(this.fileMenu);
        this.add(this.helpMenu);
        
        setupListeners();
    }
    
    /**
     * Sets up the {@link ActionListener}s for each button.
     * 
     * @since 1.0
     */
    private void setupListeners()
    {
        for (JMenuItem saveItem : saveItems)
            saveItem.addActionListener(this);
        connectItem.addActionListener(this);
        disconnectItem.addActionListener(this);
        exitItem.addActionListener(this);
        aboutItem.addActionListener(this);
        helpItem.addActionListener(this);
    }

    /**
     * Attempts to save the image to a selected file.
     * 
     * @param ae The {@link ActionEvent} that triggered this save operation.
     * @since 1.0
     */
    private void saveImage(ActionEvent ae)
    {
        String action = ae.getActionCommand();
        SaveType type;
        
        if (action.equals(SaveType.BMP.toString()))
            type = SaveType.BMP;
        else if (action.equals(SaveType.GIF.toString()))
            type = SaveType.GIF;
        else if (action.equals(SaveType.JPEG.toString()))
            type = SaveType.JPEG;
        else if (action.equals(SaveType.PNG.toString()))
            type = SaveType.PNG;
        else
            type = SaveType.UNSUPPORTED;
        
        FileNameExtensionFilter filter = 
                new FileNameExtensionFilter(type.name(), 
                        type.name(), 
                        type.name().toLowerCase(), 
                        type.name().toUpperCase());
        
        JFileChooser filePicker = new JFileChooser();
        filePicker.setFileSelectionMode(JFileChooser.FILES_ONLY);
        filePicker.setFileFilter(filter);
        filePicker.showDialog(parent, "Select File");
        
        File file = filePicker.getSelectedFile();
        int extensionInd = file.getName().lastIndexOf(".");
        String extension = "";
        if (extensionInd >= 0)
            extension = file.getName().substring(extensionInd+1);
        if (extension.isEmpty() || !extension.equals(type.name().toLowerCase()))
            file = new File(file.toString() + "." + type.name().toLowerCase());
        
        ((WhiteboardGUI)parent).saveCanvas(file, type);
    }
    
    /**
     * Handles the various {@link JMenuItem} interactions.
     * 
     * @param ae The {@link ActionEvent} generated by clicking a menu item.
     * @since 1.0
     */
    @Override
    public void actionPerformed(ActionEvent ae) 
    {
        Object source = ae.getSource();
        
        if (source instanceof JMenuItem 
                && saveItems.contains((JMenuItem)source))
            saveImage(ae);
        else if (source == exitItem)
            parent.dispatchEvent(new WindowEvent((JFrame)parent, WindowEvent.WINDOW_CLOSING));
        else if (source == connectItem)
            throw new UnsupportedOperationException("Connect is not implemented.");
        else if (source == disconnectItem)
            throw new UnsupportedOperationException("Disconnect is not implemented.");
        else if (source == aboutItem)
            throw new UnsupportedOperationException("About is not implemented.");
        else if (source == helpItem)
            throw new UnsupportedOperationException("Help is not implemented.");
    }
    
    /**
     * An enumeration of the supported file types to save the {@link 
     * WhiteboardCanvas} to.
     * 
     * @version 1.0
     * @since 1.0
     */
    public enum SaveType 
    {
        /** Save an image in the JPEG format. */
        JPEG,
        /** Save an image in the PNG format. */
        PNG,
        /** Save an image in the BMP format. */
        BMP,
        /** Save an image in the GIF format. */
        GIF,
        /** If no supported type is used, catch it with this. */
        UNSUPPORTED;
        
        /**
         * Gets the mnemonic character for this {@link SaveType}. This will be 
         * the first character of the name in lower case.
         * 
         * @return Returns a character containing the mnemonic character.
         * @since 1.0
         */
        public char getMnemonic()
        {
            return (this.name().toLowerCase().charAt(0));
        }
        
        /**
         * Returns the name of this {@link SaveType} in camel-case.
         * 
         * @return The name of this type in lower case with the first character 
         * capitalised.
         * @since 1.0
         */
        @Override        
        public String toString() 
        {
            String lCase = this.name().substring(1).toLowerCase();
            return (this.name().substring(0, 1) + lCase);
        }
    };
}
