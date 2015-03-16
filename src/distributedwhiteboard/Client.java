package distributedwhiteboard;

import distributedwhiteboard.DiscoveryMessage.DiscoveryRequest;
import distributedwhiteboard.DiscoveryMessage.DiscoveryResponse;
import distributedwhiteboard.DiscoveryMessage.JoinRequest;
import distributedwhiteboard.gui.WhiteboardCanvas;
import distributedwhiteboard.gui.WhiteboardGUI;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * Handles connection to and sending messages to other instances of this 
 * Distributed Whiteboard application. Works in tandem with the server to create
 *  a peer-to-peer network to communicate over.
 * 
 * @author 6266215
 * @version 1.3
 * @since 2015-03-15
 */
public class Client implements Runnable
{
    /** The singleton instance of this {@link Client}. */
    private static Client INSTANCE;
    /** The maximum size of the byte buffer for multicast packets. */
    private static final int BUFFER_SIZE = DiscoveryRequest.getLargestSize();
    /** A list of known hosts to send drawing updates to. */
    private final Set<Pair<String, Integer>> knownHosts;
    /** The details of this host, to stop the client from talking to itself. */
    private Pair<String, Integer> thisHost;
    /** The details for the multicast group this {@link Client} uses. */
    private final Pair<InetAddress, Integer> multicast;
    /** This will be true if this {@link Client} is allowed to send packets. */
    private volatile boolean isSending;
    /** A thread to listen for multicast connections in the background. */
    private Thread discoveryThread;
    /** The {@link MulticastSocket} to receive discovery requests on. */
    private MulticastSocket receiver;
    
    /**
     * Creates a new {@link Client} with some hard coded nodes to connect to.
     */
    private Client()
    {
        this.isSending = false;
        this.knownHosts = new HashSet<>();
        
        InetAddress addr;
        try {
            addr = InetAddress.getByName("225.4.5.8");
        } catch (UnknownHostException hostEx) {
            System.err.println("Could not find multicast group.");
            System.err.println(hostEx.getMessage());
            addr = null;
        }
        this.multicast = new Pair<>(addr, Server.MULTICAST_PORT);
    }
    
    /**
     * Gets the singleton instance for the {@link Client} if one exists. If no 
     * such instance exists, one will be created.
     * 
     * @return Returns the singleton instance of {@link Client}.
     * @since 1.0
     */
    public static Client getInstance() 
    {
        if (Client.INSTANCE == null)
            Client.INSTANCE = new Client();
        
        return Client.INSTANCE;
    }
    
    /**
     * Initialises this {@link Client} and the background thread for listening 
     * for multicast communication.
     * 
     * @since 1.3
     */
    public void startClient()
    {
        if (isEnabled()) return; // Already started.
        discoveryThread = new Thread(this);
        discoveryThread.start();
        
        while (!isSending) {}
        discoverClients();
    }
    
    /**
     * Stops the background multicast listener and disables the ability to send 
     * packets for this {@link Client}.
     * 
     * @since 1.3
     */
    public void stopClient()
    {
        isSending = false;
        try {
            System.out.println("Stopping network discovery.");
            if (multicast != null && receiver != null && !receiver.isClosed()) {
                receiver.leaveGroup(multicast.Left);
                receiver.close();
            }
            if (discoveryThread != null)
                discoveryThread.join();
        } catch (IOException ex) {
            System.err.println("Failed to leave multicast group");
            System.err.println(ex.getMessage());
        } catch (InterruptedException ex) {
            
        }
    }
    
    /**
     * Sets the details of the host this {@link Client} is running on to prevent
     *  it from talking to itself.
     * 
     * @param hostname The host name/ IP address of the host.
     * @param port The port the host is running on.
     * @since 1.0
     */
    public void setHost(String hostname, int port) 
    {
        thisHost = new Pair<>(hostname, port);
    }
    
    /**
     * Adds a new host to this {@link Client} for broadcasting the {@link 
     * WhiteboardCanvas} changes to.
     * 
     * @param host The host name/ port number {@link Pair} for the new host.
     * @return Returns true if the host was added. Returns false if the host 
     * was already known or if the host is this host.
     */
    public synchronized boolean addKnownHost(Pair<String, Integer> host)
    {
        if (host.equals(thisHost)) 
            return false;
        
        if (knownHosts.add(host)) {
            System.out.printf("Added new host %s:%d%n", host.Left, host.Right);
            return true;
        }
        
        System.out.printf("Host %s:%d already known.%n", host.Left, host.Right);
        return false;
    }
    
    /**
     * Checks to see if this {@link Client} is able to send network messages or 
     * not.
     * 
     * @return Returns true if this Client can send messages over the network, 
     * false otherwise.
     * @since 1.1
     */
    public boolean isEnabled() { return this.isSending; }
    
    /**
     * Sends a UDP message out to the specified IP address and port number. The 
     * message will be an encoded {@link NetMessage} implementation, allowing 
     * for easy understanding of the contents of the message by the recipient.
     * 
     * @param message The {@link NetMessage} to transmit to other instances of 
     * this program.
     * @param targetIp The IP address to send to as a String.
     * @param targetPort The port number to send to as an int.
     * @return Returns true if the message was sent, false otherwise. This does 
     * not indicate the message was received however.
     * @since 1.3
     */
    public boolean sendMessage(NetMessage message, String targetIp, 
            int targetPort)
    {
        if (!isSending) return false;
        
        byte[] bytes = message.encode();
            
        InetAddress address;
        DatagramSocket socket;
        DatagramPacket packet;
            
        try {
            address = InetAddress.getByName(targetIp);
        } catch (UnknownHostException hostEx) {
            System.err.printf("Could not find host %s:%d%n", 
                    targetIp, targetPort);
            return false;
        }
           
        try {
            socket = new DatagramSocket();
        } catch (SocketException sockEx) {
            System.err.println("Could not create DatagramSocket");
            return false;
        }
            
        try {
            packet = new DatagramPacket(bytes, bytes.length, 
                    address, targetPort);
            socket.send(packet);
        } catch (IOException ioEx) {
            System.err.printf("Error sending packet:%n%s%n", ioEx.getMessage());
            return false;
        } finally {
            socket.close();
        }
        
        return true;
    }
    
    /**
     * Sends a UDP message out to all known clients. Messages are contained in 
     * the {@link NetMessage} class, which encodes them into a byte array to be 
     * sent in a {@link DatagramPacket}.
     * 
     * @param message The {@link NetMessage} to transmit to other instances of 
     * this program.
     * @since 1.3
     */
    public synchronized void broadCastMessage(NetMessage message)
    {
        for (Pair<String, Integer> host : knownHosts) {
            if (host.equals(thisHost)) continue; // Don't message yourself.            
            sendMessage(message, host.Left, host.Right);
        }
    }
    
    /**
     * Sends out a multicast message to try and find other clients on the 
     * network to communicate drawings to.
     * 
     */
    public void discoverClients()
    {
        if (multicast.Left == null) return; // No multicast group.
        
        DatagramPacket packet;
        
        DiscoveryRequest request = new DiscoveryRequest(thisHost.Left, 
                thisHost.Right);
        
        byte[] buffer = request.encode();
        try (MulticastSocket sender = new MulticastSocket()) {
            packet = new DatagramPacket(buffer, buffer.length, 
                    multicast.Left, multicast.Right);
            sender.send(packet);
        } catch (IOException ex) {
            System.err.println("Failed to send discovery message.");
            System.err.println(ex.getMessage());
        }
    }
    
    private void sendDiscoveryResponse(DiscoveryMessage request)
    {
        Pair<String, Integer> source = new Pair<>(request.IP, request.Port);
        if (source.equals(thisHost)) return;
        
        addKnownHost(source);
        
        DiscoveryResponse response = new DiscoveryResponse(thisHost.Left, thisHost.Right);
        sendMessage(response, request.IP, request.Port);
    }

    @Override
    public void run()
    {
        if (multicast.Left == null) return; // No multicast group.
        
        try {
            receiver = new MulticastSocket(multicast.Right); 
            receiver.setReuseAddress(true);
            receiver.joinGroup(multicast.Left);
        } catch (SocketException sockEx) {
            System.err.println("Could not create multicast receiver.");
            System.err.println(sockEx.getMessage());
            return;
        } catch (UnknownHostException hostEx) {
            System.err.println("Could not find multicast group for receiver");
            return;
        } catch (IOException ex) {
            System.err.println("Could not start multicast listener.");
            System.err.println(ex.getMessage());
            return;
        }
        
        isSending = true;
        System.out.println("Started network discovery.");
        while(isSending) {
            try {
                byte[] buff = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buff, buff.length);
                receiver.receive(packet);
                System.out.println("Received a mutlicast packet.");
                DiscoveryMessage msg = DiscoveryMessage.decode(buff);
                if (msg.type == MessageType.DISCOVERY) {
                    sendDiscoveryResponse(msg);
                }
            } catch (IOException ex) {
                if (!isSending) return; // Socket closed.
                System.err.println("bfdgsvsf");
                System.err.println(ex.getMessage());
            }
        }
    }
}
