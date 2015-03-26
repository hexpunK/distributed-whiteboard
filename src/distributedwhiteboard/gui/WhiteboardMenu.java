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
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A {@link JMenuBar} extension that provides the core controls for the 
 * {@link WhiteboardGUI} menu bar. As with JMenuBar this can be added to if 
 * extra buttons are needed.
 * 
 * @author 6266215
 * @version 1.1
 * @since 2015-03-17
 */
public final class WhiteboardMenu extends JMenuBar implements ActionListener
{
    /** Serialisation ID. */
    private static final long serialVersionUID = -4631267601508940759L;
    /** The owning component. */
    private final Component parent;
    // The various menus.
    private final JMenu fileMenu, demoMenu, saveMenu, clientsMenu;
    // File menu items.
    private final JMenuItem connectItem, portItem, nameItem, disconnectItem, 
            exitItem, clearItem;
    // A listing of all supported image types to save to.
    private final ArrayList<JMenuItem> saveItems;
    // Help menu iems.
    private final JMenuItem redrawItem, lossItem;
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
        
        this.nameItem = new JMenuItem("Set Name");
        this.nameItem.setMnemonic('n');
        
        this.connectItem = new JMenuItem("Connect", connectIcon);
        this.connectItem.setMnemonic('c');
        
        this.disconnectItem = new JMenuItem("Disconnect", disconnectIcon);
        this.disconnectItem.setMnemonic('d');
        
        this.exitItem = new JMenuItem("Exit", closeIcon);
        this.exitItem.setMnemonic('x');
        
        this.clearItem = new JMenuItem("Reset");
        this.clearItem.setMnemonic('r');
        
        this.fileMenu = new JMenu("File");
        this.fileMenu.setMnemonic('f');
        this.fileMenu.add(this.connectItem);
        this.fileMenu.add(this.portItem);
        this.fileMenu.add(this.nameItem);
        this.fileMenu.add(this.disconnectItem);
        this.fileMenu.add(new JSeparator());
        this.fileMenu.add(this.saveMenu);
        this.fileMenu.add(this.clearItem);
        this.fileMenu.add(new JSeparator());
        this.fileMenu.add(this.exitItem);
        
        this.redrawItem = new JMenuItem("Redraw");
        this.redrawItem.setMnemonic('r');
        
        this.lossItem = new JMenuItem("Packet Loss");
        this.lossItem.setMnemonic('l');
        
        this.demoMenu = new JMenu("Demo");
        this.demoMenu.setMnemonic('d');
        this.demoMenu.add(this.redrawItem);
        this.demoMenu.add(new JSeparator());
        this.demoMenu.add(this.lossItem);
        
        this.clientsMenu = new JMenu("Clients");
        
        this.add(this.fileMenu);
        this.add(this.demoMenu);
        this.add(this.clientsMenu);
        
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
        nameItem.addActionListener(this);
        connectItem.addActionListener(this);
        disconnectItem.addActionListener(this);
        exitItem.addActionListener(this);
        clearItem.addActionListener(this);
        redrawItem.addActionListener(this);
        lossItem.addActionListener(this);
        
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
     * Updates the list of clients connected to this network. Adds a new menu 
     * item to the "Clients" menu for each name provided.
     * 
     * @param names A {@link String} array of the client names known.
     * @since 1.1
     */
    public void setClientList(String[] names)
    {
        clientsMenu.removeAll();
        for (String client : names) {
            JMenuItem item = new JMenuItem(client);
            item.setEnabled(false);
            clientsMenu.add(item);
        }
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
                && saveItems.contains((JMenuItem)source)) {
            // Save the current canvas to a file based on the button clicked.
            saveImage(ae);
        } else if (source == clearItem) {
            // Clear the canvas.
            WhiteboardGUI.getInstance().getCanvas().clearCanvas();
        } else if (source == exitItem) {
            // Quit the application.
            parent.dispatchEvent(new WindowEvent((JFrame)parent, 
                    WindowEvent.WINDOW_CLOSING));
        } else if (source == portItem) {
            // Change the port.
            String portStr = JOptionPane.showInputDialog(parent, 
                    "Enter port number:", 
                    "Select Port", 
                    JOptionPane.QUESTION_MESSAGE);
            try {
                server.setPort(Integer.parseInt(portStr));
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(parent, 
                        "Port numbers can only be whole numbers.", 
                        "Invalid Port", 
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), 
                        "Invalid Port", JOptionPane.ERROR_MESSAGE);
            }
        } else if (source == nameItem) {
            // Set the users display name.
            String answ = JOptionPane.showInputDialog(parent, 
                    "Enter display name:", 
                    "Set Name", 
                    JOptionPane.QUESTION_MESSAGE);
            String title = String.format("Distributed Whiteboard - %s", answ);
            ((JFrame)parent).setTitle(title);
            Client.getInstance().setClientName(answ);
        }else if (source == connectItem) {
            // Connect to the network.
            if (!server.startServer()) {
                JOptionPane.showMessageDialog(parent, 
                        "Error connecting to the network. Is this port in use?", 
                        "Connection Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            client.startClient();
            connectItem.setEnabled(!client.isEnabled());
            disconnectItem.setEnabled(client.isEnabled());
        } else if (source == disconnectItem) {
            // Disconnect from the network.
            server.stopServer();
            client.stopClient();
            connectItem.setEnabled(!client.isEnabled());
            disconnectItem.setEnabled(client.isEnabled());
        } else if (source == redrawItem) {
            // Slowly redraw the canvas.
            server.slowRedraw(100);
        } else if (source == lossItem) {
            // Change the packet loss simulation setting.
            final JOptionPane dialog = new JOptionPane();
            
            final JSlider slider = new JSlider(0, 100);
            slider.setValue(Server.getPacketLossRatio());
            slider.setMajorTickSpacing(10);
            slider.setPaintLabels(true);
            slider.setPaintTicks(true);
            slider.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent e)
                {
                    if (!slider.getValueIsAdjusting())
                        dialog.setInputValue((int)slider.getValue());
                }
            });
            
            dialog.setMessage(new Object[] {"Packet Loss (%):", slider});
            dialog.setMessageType(JOptionPane.QUESTION_MESSAGE);
            dialog.setOptionType(JOptionPane.OK_CANCEL_OPTION);
            dialog.setInputValue(Server.getPacketLossRatio());
            dialog.createDialog("Set Packet Loss").setVisible(true);
            
            Server.setPacketLossRatio((int)dialog.getInputValue());
        }
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
        JPEG, JPG,
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
