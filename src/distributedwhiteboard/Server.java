package distributedwhiteboard;

import distributedwhiteboard.gui.WhiteboardCanvas;
import distributedwhiteboard.gui.WhiteboardGUI;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Listens for UDP connections from other instances of the Distributed 
 * Whiteboard application.
 *
 * @author 6266215
 * @version 1.0
 * @since 2015-03-12
 */
public class Server implements Runnable
{
    /** The singleton instance of {@link Server}. */
    private static Server INSTANCE;
    /** The maximum size of a {@link DatagramPacket} buffer. */
    private static final int BUFFER_SIZE = new WhiteboardMessage().encode().length;
    /** A UDP {@link DatagramSocket} to listen for connections on. */
    private DatagramSocket socket;
    /** A port number to listen on. */
    private final int port;
    /** The IP address of the host this server is running on. */
    private String hostAddress;
    /** The {@link Thread} to run this server in the background on. */
    private Thread serverThread;
    /** Lets the server continue to execute in the background. */
    private boolean runServer;
    
    /**
     * Creates a new instance of {@link Server}. This is private to force usage 
     * of the singleton instance. There should never be more than one server 
     * running per instance of the application.
     * 
     * @since 1.0
     */
    private Server()
    {
        this.socket = null;
        this.serverThread = null;
        this.runServer = false;
        this.port = 55551;
        try {
            this.hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            this.hostAddress = "UNKNOWN";
        }
    }
    
    /**
     * Creates a new instance of {@link Server} using the specified port number.
     * This is private to force usage of the singleton instance. There should 
     * never be more than one server running per instance of the application.
     * 
     * @param port The port number to run the server on.
     * @throws IllegalArgumentException Thrown if the port number is not a valid
     *  TCP/IP port.
     * @since 1.0
     */
    private Server(int port) throws IllegalArgumentException
    {
        if (port < 0 || port > 65536) {
            throw new IllegalArgumentException(
                "Port number must be between 0 and 65536"
            );
        }
        this.socket = null;
        this.serverThread = null;
        this.runServer = false;
        this.port = port;
        try {
            this.hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            this.hostAddress = "UNKNOWN";
        }
    }
    
    /**
     * Gets the singleton instance of {@link Server} which will run on port 
     * 55551 unless a previous singleton has been created already.
     * 
     * @return The singleton instance of {@link Server}.
     * @since 1.0
     */
    public synchronized static Server getInstance()
    {
        if (Server.INSTANCE == null)
            Server.INSTANCE = new Server();
        
        return Server.INSTANCE;
    }
    
    /**
     * Gets the singleton instance of {@link Server} which will run on the 
     * specified port number unless a previous singleton has been created 
     * already.
     * 
     * @param port The port number to run the server on.
     * @return The singleton instance of {@link Server}.
     * @throws IllegalArgumentException Thrown if the port number is not a valid
     *  TCP/IP port.
     * @since 1.0
     */
    public synchronized static Server getInstance(int port) 
            throws IllegalArgumentException
    {
        if (Server.INSTANCE == null)
            Server.INSTANCE = new Server(port);
        
        return Server.INSTANCE;
    }
    
    /**
     * Opens the {@link DatagramSocket} and starts executing this {@link Server}
     *  in its background thread.
     * 
     * @return Returns true if the server can start, false if it fails for any 
     * reason.
     * @since 1.0
     */
    public boolean startServer()
    {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException ex) {
            serverError("Couldn't create DatagramSocket.\n%s", 
                    ex.getMessage());
            return false;
        }
        
        serverThread = new Thread(this);
        serverThread.setName("Whiteboard Listener");
        try {
            serverThread.start();
        } catch (IllegalThreadStateException ex) {
            serverError("Server is already running!");
            return false;
        }
        
        serverMessage("Started server", hostAddress, port);
        Client.getInstance().setHost(hostAddress, port);
        return true;
    }
    
    /**
     * Checks to see whether this {@link Server} is still running or not.
     * 
     * @return Returns true if the server is running, false otherwise.
     * @since 1.0
     */
    public synchronized boolean isRunning() { return this.runServer; }
    
    /**
     * Safely shuts the server down and stops the background thread.
     * 
     * @since 1.0
     */
    public void stopServer()
    {
        runServer = false;
        try {
            socket.close();
            serverThread.join();
        } catch (InterruptedException iEx) {
            serverError("Server interrupted during shutdown!\n%s", 
                    iEx.getMessage());
        }
        serverMessage("Server stopped", hostAddress, port);
    }
    
    private void handleMessage(WhiteboardMessage msg)
    {
        if (msg == null) return;
        WhiteboardCanvas canvas = WhiteboardGUI.getInstance().getCanvas();
        
        switch (msg.getDrawMode()) {
            case LINE:
            case POLYGON:
            case FREEFORM_LINE:
                canvas.drawLine(msg.getStartPoint(), msg.getEndPoint(), 
                        msg.getColour(), msg.getLineWeight());
                break;
            case RECTANGLE:
                Dimension d = new Dimension(msg.getEndPoint().x, msg.getEndPoint().y);
                canvas.drawRectangle(msg.getStartPoint(), d, msg.getColour(), 
                        msg.isFilled(), msg.hasBorder(), msg.getBorderWeight(), 
                        msg.getBorderColour());
                break;
            case TEXT:
                canvas.drawText(msg.getText(), msg.getStartPoint(), 
                        msg.getFont(), msg.getColour());
                break;
            default:
                serverError("Unknown drawmode.");
        }
    }
    
    private void serverMessage(String fmtString, Object...args)
    {
        serverPrint(System.out, fmtString, args);
    }
    
    private void serverError(String fmtString, Object...args)
    {
        serverPrint(System.err, fmtString, args);
    }
    
    private void serverPrint(PrintStream strm, String fmtString, Object...args)
    {
        fmtString = String.format(fmtString, args);
        strm.printf("(%s:%d) - %s\n", this.hostAddress, this.port, fmtString);
    }
    
    /**
     * Listens for incoming UDP connections in the background and responds to 
     * them as appropriate.
     * 
     * @since 1.0
     */
    @Override
    public void run()
    {
        runServer = true;
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
        
        while(runServer) {
            serverMessage("Listening...");
            try {
                socket.receive(packet);
                serverMessage("Recieved packet");
                handleMessage(WhiteboardMessage.decodeMessage(buffer));
            } catch (IOException ioEx) {
                // Only print errors while the server is running.
                if (runServer) {
                    serverError(ioEx.getMessage());
                }
            }
        }
    }
}
