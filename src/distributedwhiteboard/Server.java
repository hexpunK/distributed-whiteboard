package distributedwhiteboard;

import distributedwhiteboard.DiscoveryMessage.JoinRequest;
import distributedwhiteboard.gui.WhiteboardCanvas;
import distributedwhiteboard.gui.WhiteboardGUI;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import javax.imageio.ImageIO;

/**
 * Listens for UDP connections from other instances of the Distributed 
 * Whiteboard application.
 *
 * @author 6266215
 * @version 1.5
 * @since 2015-03-26
 */
public class Server implements Runnable
{
    /** The singleton instance of {@link Server}. */
    private static Server INSTANCE;
    /** The ratio for packet loss simulation. */
    private static int PACKET_LOSS;
    /** The maximum size of a {@link DatagramPacket} buffer. */
    private static final int BUFFER_SIZE 
            = new WhiteboardMessage().encode().length;
    /** A reserved port to listen for TCP packets on. */
    public static final int TCP_PORT = 55558;
    /** A timeout time for sending data over TCP. */
    public static final int TCP_TIMEOUT = 10000;
    /** A reserved port to listen for multicast packets on. */
    public static final int MULTICAST_PORT = 55559;
    /** A UDP {@link DatagramSocket} to listen for connections on. */
    private DatagramSocket udpServer;
    /** A TCP {@link Socket} to receive images and large data over. */
    private ServerSocket tcpServer;
    /** A port number to listen on. */
    private int port;
    /** The IP address of the host this server is running on. */
    private String hostName;
    /** The {@link Thread} to run this server in the background on. */
    private Thread serverThread;
    /** A {@link Thread} to handle the slow redrawing. */
    private Thread redrawThread;
    /** Lets the server continue to execute in the background. */
    private volatile boolean runServer;
    /** Contains a history of all received {@link NetMessage}s. */
    public static final HashMap<String, NetMessage> messages = 
            new HashMap<>();
    /** Holds a buffer of {@link NetMessage}s that need requesting. */
    private Queue<NetMessage> messageBuffer;
    /** 
     * A mapping of {@link BufferedImage}s that this {@link Server} knows to
     * their unique hash codes. 
     */
    public static final HashMap<Integer, BufferedImage> images = 
            new HashMap<>();
    
    /**
     * Creates a new instance of {@link Server}. This is private to force usage 
     * of the singleton instance. There should never be more than one server 
     * running per instance of the application.
     * 
     * @since 1.0
     */
    private Server()
    {
        this.udpServer = null;
        this.serverThread = null;
        this.runServer = false;
        this.port = 55551;
        try {
            this.hostName = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            this.hostName = "UNKNOWN";
        }
        this.messageBuffer = new LinkedBlockingQueue<>();
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
        this.udpServer = null;
        this.serverThread = null;
        this.runServer = false;
        this.port = port;
        try {
            this.hostName = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            this.hostName = "UNKNOWN";
        }
        this.messageBuffer = new LinkedBlockingQueue<>();
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
     * Sets the packet loss ratio for the {@link Server}. This will only affect 
     * {@link WhiteboardMessage} messages rather than all types. The value is 
     * clamped between 0 and 100.
     * 
     * @param ratio The ratio of packets to drop as a int.
     * @since 1.5
     */
    public static void setPacketLossRatio(int ratio)
    {
        ratio = Math.max(0, ratio);
        ratio = Math.min(100, ratio);
        PACKET_LOSS = ratio;
        System.out.printf("Set server packet loss to %d%%%n", PACKET_LOSS);
    }
    
    /**
     * Gets the ratio used for simulating packet loss across all {@link Server} 
     * instances.
     * 
     * @return The ratio for packet loss as an int between 0 to 100 (inclusive).
     * @since 1.5
     */
    public static int getPacketLossRatio() { return PACKET_LOSS; }
    
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
            udpServer = new DatagramSocket(port);
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
            if (udpServer != null && !udpServer.isClosed())
                udpServer.close();
            if (tcpServer != null && !tcpServer.isClosed())
                tcpServer.close();
            if (serverThread != null)
                serverThread.join();
            if (redrawThread != null) {
                redrawThread.interrupt();
                redrawThread.join(500);
            }
        } catch (InterruptedException iEx) {
            serverError("Server interrupted during shutdown!%n%s", 
                    iEx.getMessage());
        } catch (IOException ioEx) {
            serverError("Failed to close server.%n%s", ioEx.getMessage());
        }
        serverMessage("Server stopped", hostName, port);
    }
    
    /**
     * Processes a received packet and handles the contained message in the 
     * correct way for its {@link MessageType}.
     * 
     * @param buffer The byte buffer of the packet to handle.
     * @return Returns true if the packet could be processed, false if the 
     * packet was invalid of if the packet was dropped due to simulated packet 
     * loss.
     * @since 1.2
     */
    private boolean processPacket(byte[] buffer)
    {
        NetMessage msg;
        MessageType t = NetMessage.getMessageType(buffer);
        if (t == null) {
            serverMessage("Received an unknown message type. Ignored.");
            return false;
        }
        switch (t) {
            case DRAW:
                msg = WhiteboardMessage.decode(buffer);
                int ranVal = new Random().nextInt(100);
                if (ranVal <= PACKET_LOSS) {
                    serverMessage("Dropped a packet.");
                    return false;
                }
                messages.put(msg.getUniqueID(), msg);
                if (msg.getRequiredID() != null 
                        && !messages.containsKey(msg.getRequiredID())) {
                    if (!messageBuffer.contains(msg))
                        messageBuffer.add(msg);
                    System.out.println("Missing a required packet.");
                    return false;
                }
                handleWhiteboardMessage((WhiteboardMessage)msg);
                break;
            case JOIN:
                msg = DiscoveryMessage.decode(buffer);
                handeJoinRequest((DiscoveryMessage)msg);
                break;
            case RESPONSE:
                msg = DiscoveryMessage.decode(buffer);
                handleDiscoveryResponse((DiscoveryMessage)msg);
                break;
            case LEAVE:
                msg = DiscoveryMessage.decode(buffer);
                handleLeaveRequest((DiscoveryMessage)msg);
                break;
        }
        
        return true;
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
            serverError("Could not decode WhiteboardMessage");
            return;
        }
        WhiteboardCanvas canvas = WhiteboardGUI.getInstance().getCanvas();
        if (canvas == null) {
            serverError("No Whiteboard canvas could be found.");
            return;
        }
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
                BufferedImage i = images.get(msg.imageHash);
                if (i == null) {
                    serverMessage("Preparing to receive image.");
                    Client.getInstance().requestImage(msg.imageHash);
                    i = receiveImage();
                    if (i == null) return;
                    images.put(msg.imageHash, i);
                    serverMessage("Cached new image (%d)", msg.imageHash);
                }
                canvas.drawImage(msg.startPoint, i, msg.imageScale);
                break;
            default:
                serverError("Unknown drawmode.");
        }
    }
    
    /**
     * Receives data from a TCP connection. This will be used when sending large
     *  amounts of data between two instances of the whiteboard due to the 
     * limitations of UDP communication.
     * 
     * @return Returns the {@link InputStream} for this {@link Socket} to read 
     * data from.
     * @since 1.3
     */
    private BufferedImage receiveImage()
    {   
        try {
            tcpServer = new ServerSocket(TCP_PORT);
            tcpServer.setSoTimeout(TCP_TIMEOUT);
        } catch (IOException ex) {
            serverError("Couldn't set up TCP server.%n%s", ex.getMessage());
            return null;
        }
        
        try (Socket sock = tcpServer.accept()) {
            serverMessage("Received socket connection.");
            return ImageIO.read(sock.getInputStream());
        } catch (SocketTimeoutException sEx) {
            serverError("Socket timed out receiving image.");
        } catch (IOException ex) {
            serverError("Error receiving TCP data.%n%s", ex.getMessage());
        } finally {
            try {
                if (tcpServer != null && !tcpServer.isClosed())
                    tcpServer.close();
            } catch (IOException closeEx) {
                serverError("Couldm't close TCP server.%n%s", 
                        closeEx.getMessage());
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
        WhiteboardCanvas canvas = WhiteboardGUI.getInstance().getCanvas();
        if (msg == null) {
            serverError("Could not decode DiscoveryMessage");
            return;
        }
        if (client == null) {
            serverError("Could not get clientside component.");
            return;
        }
        if (canvas == null) {
            serverError("Could not get whiteboard canvas.");
            return;
        }
        
        String newName = msg.Name;
        String newIP = msg.IP;
        int newPort = msg.Port;
        Triple<String, String, Integer> host = new Triple<>(newName, newIP, 
                newPort);
        
        client.addHost(host);
        serverMessage("Requesting canvas from host %s:%d.", msg.IP, msg.Port);
        client.sendMessage(new JoinRequest(client.getClientName(), hostName, 
                TCP_PORT), host.Two, host.Three);
            
        BufferedImage image = receiveImage();
        serverMessage("Received canvas from host %s:%d.", msg.IP, msg.Port);
        canvas.drawImage(new Point(), image, 1.0f);
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
    private void handeJoinRequest(DiscoveryMessage msg)
    {
        if (msg == null) {
            serverError("JoinRequest was incorrectly formed.");
            return;
        }
        serverMessage("Sending canvas to host %s:%d.", msg.IP, msg.Port);
        BufferedImage canvas = 
                WhiteboardGUI.getInstance().getCanvas().getBufferedImage();
        Client.sendImage(canvas, new Pair<>(msg.IP, msg.Port));
    }
    
    /**
     * Handles {@link LeaveRequest} messages from clients disconnecting from 
     * the distributed network.
     * 
     * @param msg The {@link DiscoveryMessage} to read the IP address and port 
     * of the disconnecting client from.
     * @since 1.4
     */
    private void handleLeaveRequest(DiscoveryMessage msg)
    {
        Client client = Client.getInstance();
        if (client == null) {
            serverError("Could not get client instance.");
            return;
        }
        client.removeHost(new Triple<>(msg.Name, msg.IP, msg.Port));
    }
    
    /**
     * Slowly redraws all the previous messages this {@link Server} has stored.
     * 
     * @param ms The delay between drawing operations in milliseconds.
     * @since 1.5
     */
    public void slowRedraw(final int ms)
    {
        redrawThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String lastID = null;
                System.out.println("Redrawing...");
                WhiteboardGUI.getInstance().getCanvas().clearCanvas();
                HashSet<String> handledIDS = new HashSet<>();
                boolean missingMessage = false;
                boolean complete = false;
                while (!complete) {
                    complete = true;
                    for (NetMessage message : messages.values()) {
                        if (!handledIDS.contains(message.getUniqueID())) {
                            complete = false;
                        }
                        missingMessage = true;
                        if (!handledIDS.contains(message.getUniqueID())
                                && message.getRequiredID() == null
                                || (lastID != null 
                                    && message.getRequiredID() != null
                                    && message.getRequiredID().equals(lastID)
                                    && message instanceof WhiteboardMessage)) {
                            System.out.printf("Last Message: %s - Current Message: %s - Required Message: %s%n", 
                                    lastID == null ? "null" : lastID, 
                                    message.getUniqueID() == null ? "null" : message.getUniqueID(), 
                                    message.getRequiredID() == null ? "null" : message.getUniqueID());
                            handleWhiteboardMessage((WhiteboardMessage)message);
                            lastID = message.getUniqueID();
                            missingMessage = false;
                            handledIDS.add(message.getUniqueID());
                            break;
                        }
                    }
                    if (missingMessage && lastID != null) {
                        Client.getInstance().requestPacket(lastID);
                    }
                    try {
                        Thread.sleep(ms);
                    } catch (InterruptedException ex) { return; }
                }
                System.out.printf("Messages: %d\tHandled: %d%n", messages.size(), handledIDS.size());
                System.out.println("Finished redrawing.");
            }
        });
        redrawThread.start();
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
        ArrayList<NetMessage> toRemove = new ArrayList<>();
        
        serverMessage("Listening for connections...");
        while(runServer) {
            for (NetMessage head : messageBuffer) {
                if (!processPacket(head.encode())) 
                    Client.getInstance().requestPacket(head.getRequiredID());
                else {
                    toRemove.add(head);
                }
            }
            messageBuffer.removeAll(toRemove);
            toRemove.clear();
            try {
                buffer = new byte[BUFFER_SIZE];
                packet = new DatagramPacket(buffer, BUFFER_SIZE);
                udpServer.receive(packet);
                processPacket(buffer);
            } catch (IOException ioEx) {
                // Only print errors while the server is running.
                if (runServer) {
                    serverError(ioEx.getMessage());
                }
            }
        }
    }
}
