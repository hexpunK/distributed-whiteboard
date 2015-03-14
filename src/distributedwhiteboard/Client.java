package distributedwhiteboard;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Handles connection to and sending messages to other instances of this 
 * Distributed Whiteboard application. Works in tandem with the server to create
 *  a peer-to-peer network to communicate over.
 * 
 * @author 6266215
 * @version 1.1
 * @since 2015-03-13
 */
public class Client
{
    /** The singleton instance of this {@link Client}. */
    private static Client INSTANCE;
    /** A list of known hosts to send drawing updates to. */
    private final ArrayList<Pair<String, Integer>> knownHosts;
    /** The details of this host, to stop the client from talking to itself. */
    private Pair<String, Integer> thisHost;
    /** This will be true if this {@link Client} is allowed to send packets. */
    private boolean isSending;
    
    /**
     * Creates a new {@link Client} with some hard coded nodes to connect to.
     */
    private Client()
    {
        this.isSending = false;
        this.knownHosts = new ArrayList<>();
        this.knownHosts.add(new Pair("192.168.0.69", 55551));
        this.knownHosts.add(new Pair("192.168.0.69", 55552));
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
     * Enables or disables this {@link Client}, preventing it from sending 
     * network messages when disabled.
     * 
     * @param enabled Set this to true to allow this Client to send network 
     * messages, false to prevent this.
     * @since 1.1
     */
    public void setEnabled(boolean enabled) { isSending = enabled; }
    
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
     * Sends a UDP message out to all known clients. Messages are contained in 
     * the {@link WhiteboardMessage} class, which encodes them into a byte 
     * array to be sent in a {@link DatagramPacket}.
     * 
     * @param message The {@link WhiteboardMessage} to transmit to other 
     * instances of this program.
     * @since 1.0
     */
    public void sendMessage(WhiteboardMessage message)
    {
        if (!isSending) return;
        
        byte[] bytes = message.toString().getBytes();
        for (Pair<String, Integer> host : knownHosts) {
            if (host.equals(thisHost)) continue; // Don't message yourself.
            
            InetAddress address;
            DatagramSocket socket;
            DatagramPacket packet;
            
            try {
                address = InetAddress.getByName(host.Left);
            } catch (UnknownHostException hostEx) {
                System.err.printf("Could not find host %s:%d\n", 
                        host.Left, host.Right);
                continue;
            }
            
            try {
                socket = new DatagramSocket();
            } catch (SocketException sockEx) {
                System.err.println("Could not create DatagramSocket");
                continue;
            }
            
            try {
                packet = new DatagramPacket(bytes, bytes.length, 
                        address, host.Right);
                socket.send(packet);
            } catch (IOException ioEx) {
                System.err.printf("Error sending packet:\n%s\n", 
                        ioEx.getMessage());
            } finally {
                socket.close();
            }
        }
    }
}
