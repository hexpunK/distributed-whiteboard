package distributedwhiteboard;

import distributedwhiteboard.gui.WhiteboardCanvas;
import distributedwhiteboard.gui.WhiteboardGUI;
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
 * @version 1.1
 * @since 2015-03-13
 */
public class Server implements Runnable
{
    /** The singleton instance of {@link Server}. */
    private static Server INSTANCE;
    /** The maximum size of a {@link DatagramPacket} buffer. */
    private static final int BUFFER_SIZE 
            = new WhiteboardMessage().encode().length;
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
    
    public Pair<String, Integer> getServerDetails()
    {
        return new Pair<>(hostAddress, port);
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
    
    /**
     * Processes a {@link WhiteboardMessage} and ensures that the action 
     * described by the message is sent to the right component of this program.
     * 
     * @param msg The {@link WhiteboardMessage} received that needs processing.
     * @since 1.1
     */
    private void handleWhiteboardMessage(WhiteboardMessage msg)
    {
        if (msg == null) return;
        WhiteboardCanvas canvas = WhiteboardGUI.getInstance().getCanvas();
        
        switch (msg.mode) {
            case LINE:
            case POLYGON:
            case FREEFORM_LINE:
                canvas.drawLine(msg.startPoint, msg.endPoint, 
                        msg.drawColour, msg.lineWeight);
                break;
            case RECTANGLE:
                Dimension d = new Dimension(msg.endPoint.x, msg.endPoint.y);
                canvas.drawRectangle(msg.startPoint, d, msg.drawColour, 
                        msg.fillShape, msg.hasBorder, msg.borderWeight, 
                        msg.borderCol);
                break;
            case TEXT:
                canvas.drawText(msg.textChar, msg.startPoint, msg.font, 
                        msg.drawColour);
                break;
            default:
                serverError("Unknown drawmode.");
        }
    }
    
    /**
     * Prints the specified {@link String} to the {@link System#out} stream.
     * 
     * @param fmtString The {@link String} to format and print.
     * @param args A number of {@link Object}s to use when formatting the string
     * @since 1.1
     */
    private void serverMessage(String fmtString, Object...args)
    {
        serverPrint(System.out, fmtString, args);
    }
    
    /**
     * Prints the specified {@link String} to the {@link System#err} stream.
     * 
     * @param fmtString The {@link String} to format and print.
     * @param args A number of {@link Object}s to use when formatting the string
     * @since 1.1
     */
    private void serverError(String fmtString, Object...args)
    {
        serverPrint(System.err, fmtString, args);
    }
    
    /**
     * Prints a specified {@link String} to the specified {@link PrintStream}.
     * The string can have a number of {@link Object}s supplied to be used as if
     *  {@link String#format(java.lang.String, java.lang.Object...)} was being 
     * called. All strings will be prepended to a description of this server to 
     * make identifying server messages easier.
     * 
     * @param strm The {@link PrintStream} to write this string to.
     * @param fmtString The {@link String} to format and write out.
     * @param args A number of {@link Object}s to use when formatting the string
     * @since 1.1
     */
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
        
        serverMessage("Listening for connections...");
        while(runServer) {
            try {
                socket.receive(packet);
                MessageType t = NetMessage.getMessageType(buffer);
                switch (t) {
                    case DISCOVERY:
                        break;
                    case DRAW:
                        handleWhiteboardMessage(WhiteboardMessage.decodeMessage(buffer));
                        break;
                    case JOIN:
                        break;
                    case RESPONSE:
                        break;
                }
            } catch (IOException ioEx) {
                // Only print errors while the server is running.
                if (runServer) {
                    serverError(ioEx.getMessage());
                }
            }
        }
    }
}
