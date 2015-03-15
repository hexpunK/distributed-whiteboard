package distributedwhiteboard;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Handles connection to and sending messages to other instances of this 
 * Distributed Whiteboard application. Works in tandem with the server to create
 *  a peer-to-peer network to communicate over.
 * 
 * @author 6266215
 * @version 1.2
 * @since 2015-03-15
 */
public class Client implements Runnable
{
    /** The singleton instance of this {@link Client}. */
    private static Client INSTANCE;
    private static final int BUFFER_SIZE = 64;
    /** A list of known hosts to send drawing updates to. */
    private final ArrayList<Pair<String, Integer>> knownHosts;
    /** The details of this host, to stop the client from talking to itself. */
    private Pair<String, Integer> thisHost;
    private final Pair<InetAddress, Integer> multicast;
    /** This will be true if this {@link Client} is allowed to send packets. */
    private boolean isSending;
    private Thread discoveryThread;
    private MulticastSocket receiver;
    
    /**
     * Creates a new {@link Client} with some hard coded nodes to connect to.
     */
    private Client()
    {
        this.isSending = false;
        this.knownHosts = new ArrayList<>();
        this.knownHosts.add(new Pair("localhost", 55551));
        this.knownHosts.add(new Pair("localhost", 55552));
        this.knownHosts.add(new Pair("192.168.0.69", 55552));
        
        InetAddress addr;
        try {
            addr = InetAddress.getByName("225.4.5.8");
        } catch (UnknownHostException hostEx) {
            System.err.println("Could not find multicast group.");
            System.err.println(hostEx.getMessage());
            addr = null;
        }
        this.multicast = new Pair<>(addr, 55559);
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
    
    public void startClient()
    {
        if (isSending) return; // Already started.
        discoveryThread = new Thread(this);
        discoveryThread.start();
        
        while (!isSending) {}
        discoverClients();
    }
    
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
     * Enables or disables this {@link Client}, preventing it from sending 
     * network messages when disabled.
     * 
     * @param enabled Set this to true to allow this Client to send network 
     * messages, false to prevent this.
     * @since 1.1
     * @deprecated Call {@link Client#startClient()} and {@link 
     * Client#stopClient()} instead to ensure network discovery works.
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
    
    public void discoverClients()
    {
        if (multicast.Left == null) return; // No multicast group.
        
        DatagramPacket packet;
        byte[] buffer = new byte[BUFFER_SIZE];
        
        for (byte i = 0; i < BUFFER_SIZE;)
            buffer[i] = i++;
        
        try (MulticastSocket sender = new MulticastSocket()) {
            packet = new DatagramPacket(buffer, buffer.length, 
                    multicast.Left, multicast.Right);
            sender.send(packet);
        } catch (IOException ex) {
            System.err.println("Failed to send discovery message.");
            System.err.println(ex.getMessage());
        }
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
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                receiver.receive(packet);
                System.out.println("Received a mutlicast packet.");
            } catch (IOException ex) {
                if (!isSending) return; // Socket closed.
                System.err.println("bfdgsvsf");
                System.err.println(ex.getMessage());
            }
        }
    }
}
