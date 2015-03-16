package distributedwhiteboard;

import distributedwhiteboard.DiscoveryMessage.DiscoveryResponse;
import distributedwhiteboard.gui.WhiteboardCanvas;
import distributedwhiteboard.gui.WhiteboardGUI;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.imageio.ImageIO;

/**
 * Listens for UDP connections from other instances of the Distributed 
 * Whiteboard application.
 *
 * @author 6266215
 * @version 1.3
 * @since 201503-15
 */
public class Server implements Runnable
{
    /** *  The singleton instance of {@link Server}. */
    private static Server INSTANCE;
    /** The maximum size of a {@link DatagramPacket} buffer. */
    private static final int BUFFER_SIZE 
            = new WhiteboardMessage().encode().length;
    /** A reserved port to listen for TCP packets on. */
    public static final int TCP_PORT = 55558;
    /** A reserved port to listen for multicast packets on. */
    public static final int MULTICAST_PORT = 55559;
    /** A UDP {@link DatagramSocket} to listen for connections on. */
    private DatagramSocket updServer;
    /** A TCP {@link Socket} to receive images and large data over. */
    private ServerSocket tcpServer;
    /** A port number to listen on. */
    private int port;
    /** The IP address of the host this server is running on. */
    private String hostName;
    /** The {@link Thread} to run this server in the background on. */
    private Thread serverThread;
    /** Lets the server continue to execute in the background. */
    private volatile boolean runServer;
    
    /**
     * Creates a new instance of {@link Server}. This is private to force usage 
     * of the singleton instance. There should never be more than one server 
     * running per instance of the application.
     * 
     * @since 1.0
     */
    private Server()
    {
        this.updServer = null;
        this.serverThread = null;
        this.runServer = false;
        this.port = 55551;
        try {
            this.hostName = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            this.hostName = "UNKNOWN";
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
        this.updServer = null;
        this.serverThread = null;
        this.runServer = false;
        this.port = port;
        try {
            this.hostName = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            this.hostName = "UNKNOWN";
        }
    }
    
    /**
     * Gets the singleton instance of {@link Server} which will run on
     * port 55551 unless a previous singleton has been created already.
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
     * Gets the singleton instance of {@link Server} which will run on
     * the specified port number unless a previous singleton has been created 
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
     * Sets the port this {@link Server} will use when listening for 
     * incoming connections.
     * 
     * @param port The port number as an int.
     * @throws IllegalArgumentException Thrown if the port number is not a valid
     *  TCP/IP port.
     * @since 1.2
     */
    public void setPort(int port) throws IllegalArgumentException
    {
        if (port < 0 || port > 65536
                || port == TCP_PORT || port == MULTICAST_PORT) {
            throw new IllegalArgumentException(
                    String.format("Port number must be between 0 and 65536 but "
                            + "cannot be reserved ports %d or %d", 
                            TCP_PORT, MULTICAST_PORT)
            );
        }
        this.port = port;
    }
    
    /**
     * Opens the {@link DatagramSocket} and starts executing this {@link 
     * Server} in its background thread.
     * 
     * @return Returns true if the server can start, false if it fails for any 
     * reason.
     * @since 1.0
     */
    public boolean startServer()
    {
        try {
            updServer = new DatagramSocket(port);
        } catch (SocketException ex) {
            serverError("Couldn't create DatagramSocket.%n%s", 
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
        
        serverMessage("Started server", hostName, port);
        Client.getInstance().setHost(hostName, port);
        return true;
    }
    
    /**
     * Checks to see whether this {@link Server} is still running or 
     * not.
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
            if (updServer != null && !updServer.isClosed())
                updServer.close();
            if (tcpServer != null && !tcpServer.isClosed())
                tcpServer.close();
            if (serverThread != null)
                serverThread.join();
        } catch (InterruptedException iEx) {
            serverError("Server interrupted during shutdown!%n%s", 
                    iEx.getMessage());
        } catch (IOException ioEx) {
            serverError("Failed to close server.%n%s", ioEx.getMessage());
        }
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
        if (msg == null) {
            System.err.println("Could not decode WhiteboardMessage");
            return;
        }
        WhiteboardCanvas canvas = WhiteboardGUI.getInstance().getCanvas();
        
        switch (msg.mode) {
            case LINE:
            case POLYGON:
            case FREEFORM_LINE:
                canvas.drawLine(msg.startPoint, msg.endPoint, 
                        msg.drawColour, msg.lineWeight);
                break;
            case RECTANGLE:
                Dimension d = new Dimension(
                        msg.endPoint.x - msg.startPoint.x, 
                        msg.endPoint.y - msg.startPoint.y
                );
                Point p = msg.startPoint;
                if (d.width < 0 && d.height > 0) { 
                    p = new Point(msg.endPoint.x, msg.startPoint.y);
                } else if (d.width > 0 && d.height < 0) { 
                    p = new Point(msg.startPoint.x, msg.endPoint.y);
                } else if (d.width < 0 && d.height < 0) { 
                    p = new Point(msg.endPoint.x, msg.endPoint.y);
                }
                canvas.drawRectangle(p, d, msg.drawColour, msg.fillShape, 
                        msg.hasBorder, msg.borderWeight, msg.borderCol);
                break;
            case TEXT:
                canvas.drawText(msg.textChar, msg.startPoint, msg.font, 
                        msg.drawColour);
                break;
            case IMAGE:
                BufferedImage i = receiveImage();
                canvas.drawImage(new Point(), i, msg.imageScale);
                break;
            default:
                serverError("Unknown drawmode.");
        }
    }
    
    /**
     * Sends a specified {@link BufferedImage} over TCP to the specified host.
     * The host is a {@link Pair} containing the IP address/ host name in the 
     * left value, and the port number in the right value.
     * 
     * @param image
     * @param host 
     */
    private void sendImage(BufferedImage image, Pair<String, Integer> host)
    {
        try (Socket sender = new Socket(host.Left, host.Right)) {
            ImageIO.write(image, "PNG", sender.getOutputStream());
        } catch (IOException ex) {
            serverError("Error sending image to host %s:%d%n%s%n", 
                    host.Left, host.Right, ex.getMessage());
        }
    }
    
    /**
     * Receives an image sent to this instance of the Distributed Whiteboard by 
     * setting up a TCP connection. This connection uses the port specified in
     * TCP_PORT, so no other instances of the application should use this port.
     * 
     * @return Returns a {@link BufferedImage} containing the image to render. 
     * Returns null if there are any problems with receiving the networked 
     * image.
     * @since 1.3
     */
    private BufferedImage receiveImage()
    {   
        try {
            tcpServer = new ServerSocket(TCP_PORT);
            tcpServer.setSoTimeout(3000);
        } catch (IOException ex) {
            serverError("Couldn't set up TCP server.%n%s", ex.getMessage());
            return null;
        }
        
        try (Socket sock = tcpServer.accept()) {
            sock.setSoTimeout(3000);
            BufferedImage img = ImageIO.read(sock.getInputStream());
            return img;
        } catch (SocketException sEx) {
            serverError("Socket timed out receiving image.");
        } catch (IOException ex) {
            serverError("Error receiving image.%n%s", ex.getMessage());
        } finally {
            try {
                if (tcpServer != null && !tcpServer.isClosed())
                    tcpServer.close();
            } catch (IOException closeEx) {
                serverError(closeEx.getMessage());
            }
        }
        
        return null;
    }
    
    /**
     * Handle a response from a {@link DiscoveryRequest} by trying to add the 
     * found host to the list of known hosts in the {@link Client} component, 
     * and by getting a copy of the {@link WhiteboardCanvas} from the new hosts.
     * 
     * @param msg The {@link DiscoveryMessage} send as a reply to this instance 
     * of the application.
     * @since 1.3
     */
    private void handleDiscoveryResponse(DiscoveryMessage msg)
    {
        Client client = Client.getInstance();
        if (msg == null || client == null) {
            System.err.println("Could not decode DiscoveryMessage");
            return;
        }
        
        String newIP = msg.IP;
        int newPort = msg.Port;
        Pair<String, Integer> host = new Pair<>(newIP, newPort);
        
        client.addKnownHost(host);
        serverMessage("Receiving canvas from host %s:%d.", msg.IP, msg.Port);
        DiscoveryMessage.JoinRequest join = new DiscoveryMessage.JoinRequest(hostName, TCP_PORT);
        client.sendMessage(join, host.Left, host.Right);
            
        BufferedImage image = receiveImage();
        WhiteboardGUI.getInstance().getCanvas().drawImage(new Point(), image, 1.0f);
    }
    
    /**
     * Responds to a {@link Client} that has requested to join the Distributed 
     * Whiteboard network by sending a copy of the current {@link 
     * WhiteboardCanvas} over TCP to update that client.
     * 
     * @param msg The {@link DiscoveryMessage} from the {@link Client} trying to
     *  join this distributed network.
     * @since 1.3
     */
    private void handeJoinRequest(final DiscoveryMessage msg)
    {
        if (msg == null) {
            serverError("JoinRequest was incorrectly formed.");
            return;
        }
        serverMessage("Sending canvas to host %s:%d.", msg.IP, msg.Port);
        final BufferedImage canvas = 
                WhiteboardGUI.getInstance().getCanvas().getBufferedImage();
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {}
                sendImage(canvas,  new Pair<>(msg.IP, msg.Port));
            }
        }).start();
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
        strm.printf("(%s:%d) - %s%n", this.hostName, this.port, fmtString);
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
        byte[] buffer;
        DatagramPacket packet;
        MessageType t;
        
        serverMessage("Listening for connections...");
        while(runServer) {
            try {
                buffer = new byte[BUFFER_SIZE];
                packet = new DatagramPacket(buffer, BUFFER_SIZE);
                updServer.receive(packet);
                t = NetMessage.getMessageType(buffer);
                if (t == null) {
                    serverMessage("Received an unknown message type. Ignored.");
                    continue;
                }
                switch (t) {
                    case DRAW:
                        handleWhiteboardMessage(WhiteboardMessage.decodeMessage(buffer));
                        break;
                    case JOIN:
                        handeJoinRequest(DiscoveryResponse.decode(buffer));
                        break;
                    case RESPONSE:
                        handleDiscoveryResponse(DiscoveryResponse.decode(buffer));
                        break;
                }
            } catch (IOException ioEx) {
                // Only print errors while the server is running.
                if (runServer) {
                    serverError(ioEx.getMessage());
                }
            }
        }
        
        serverMessage("Server stopped", hostName, port);
    }
}
