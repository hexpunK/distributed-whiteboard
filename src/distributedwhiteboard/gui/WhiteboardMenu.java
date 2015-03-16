package distributedwhiteboard.gui;

import distributedwhiteboard.Client;
import distributedwhiteboard.Server;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
    /** Serialisation ID. */
    private static final long serialVersionUID = -4631267601508940759L;
    /** The owning component. */
    private final Component parent;
    // The various menus.
    private final JMenu fileMenu, helpMenu, saveMenu;
    // File menu items.
    private final JMenuItem connectItem, portItem, disconnectItem, exitItem;
    // A listing of all supported image types to save to.
    private final ArrayList<JMenuItem> saveItems;
    // Help menu iems.
    private final JMenuItem aboutItem, helpItem;
    // Icons for the various menu items.
    private final ImageIcon closeIcon, helpIcon, imageIcon, connectIcon, 
            disconnectIcon;
    
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
        
        String path = "/distributedwhiteboard/gui/assets/";
        closeIcon = WhiteboardGUI.createIcon(path+"process-stop.png");
        helpIcon = WhiteboardGUI.createIcon(path+"help-browser.png");
        imageIcon = WhiteboardGUI.createIcon(path+"image-x-generic.png");
        connectIcon = WhiteboardGUI.createIcon(path+"mail-send-receive.png");
        disconnectIcon = WhiteboardGUI.createIcon(path+"system-log-out.png");
        
        saveItems = new ArrayList<>();
        for (SaveType type : SaveType.values()) {
            if (type == SaveType.UNSUPPORTED) continue;
            
            JMenuItem saveItem = new JMenuItem(type.name(), imageIcon);
            saveItem.setMnemonic(type.getMnemonic());
            saveItems.add(saveItem);
        }
        
        this.saveMenu = new JMenu("Save As...");
        this.saveMenu.setMnemonic('s');
        for (JMenuItem saveItem : saveItems)
            this.saveMenu.add(saveItem);
        
        this.portItem = new JMenuItem("Set Port");
        this.portItem.setMnemonic('p');
        
        this.connectItem = new JMenuItem("Connect", connectIcon);
        this.connectItem.setMnemonic('c');
        
        this.disconnectItem = new JMenuItem("Disconnect", disconnectIcon);
        this.disconnectItem.setMnemonic('d');
        
        this.exitItem = new JMenuItem("Exit", closeIcon);
        this.exitItem.setMnemonic('x');
        
        this.fileMenu = new JMenu("File");
        this.fileMenu.setMnemonic('f');
        this.fileMenu.add(this.connectItem);
        this.fileMenu.add(this.portItem);
        this.fileMenu.add(this.disconnectItem);
        this.fileMenu.add(new JSeparator());
        this.fileMenu.add(this.saveMenu);
        this.fileMenu.add(new JSeparator());
        this.fileMenu.add(this.exitItem);
        
        this.aboutItem = new JMenuItem("About");
        this.aboutItem.setMnemonic('a');
        
        this.helpItem = new JMenuItem("Guide", helpIcon);
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
        
        portItem.addActionListener(this);
        connectItem.addActionListener(this);
        disconnectItem.addActionListener(this);
        exitItem.addActionListener(this);
        aboutItem.addActionListener(this);
        helpItem.addActionListener(this);
        
        connectItem.setEnabled(!Client.getInstance().isEnabled());
        disconnectItem.setEnabled(Client.getInstance().isEnabled());
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
        
        if (action.equals(SaveType.BMP.name()))
            type = SaveType.BMP;
        else if (action.equals(SaveType.GIF.name()))
            type = SaveType.GIF;
        else if (action.equals(SaveType.JPEG.name()))
            type = SaveType.JPEG;
        else if (action.equals(SaveType.PNG.name()))
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
        if (file == null) return;
        
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
        Server server = Server.getInstance();
        Client client = Client.getInstance();
        
        if (source instanceof JMenuItem 
                && saveItems.contains((JMenuItem)source))
            // Save the current canvas to a file based on the button clicked.
            saveImage(ae);
        else if (source == exitItem)
            // Quit the application.
            parent.dispatchEvent(new WindowEvent((JFrame)parent, 
                    WindowEvent.WINDOW_CLOSING));
        else if (source == portItem) {
            // Change the port.
            String portStr = JOptionPane.showInputDialog(parent, 
                    "Enter port number:", "Select Port", 
                    JOptionPane.QUESTION_MESSAGE);
            try {
                server.setPort(Integer.parseInt(portStr));
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(parent, "Port numbers can only be"
                        + " whole numbers.", "Invalid Port", 
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), 
                        "Invalid Port", JOptionPane.ERROR_MESSAGE);
            }
        } else if (source == connectItem) {
            // Connect to the network.
            server.startServer();
            client.startClient();
            connectItem.setEnabled(!client.isEnabled());
            disconnectItem.setEnabled(client.isEnabled());
        } else if (source == disconnectItem) {
            // Disconnect from the network.
            server.stopServer();
            client.stopClient();
            connectItem.setEnabled(!client.isEnabled());
            disconnectItem.setEnabled(client.isEnabled());
        } else if (source == aboutItem)
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
