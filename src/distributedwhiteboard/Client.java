package distributedwhiteboard;

import distributedwhiteboard.DiscoveryMessage.DiscoveryRequest;
import distributedwhiteboard.DiscoveryMessage.DiscoveryResponse;
import distributedwhiteboard.DiscoveryMessage.LeaveRequest;
import distributedwhiteboard.gui.WhiteboardCanvas;
import distributedwhiteboard.gui.WhiteboardGUI;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * Handles connection to and sending messages to other instances of this 
 * Distributed Whiteboard application. Works in tandem with the server to create
 *  a peer-to-peer network to communicate over.
 * 
 * @author 6266215
 * @version 1.6
 * @since 2015-03-27
 */
public class Client implements Runnable
{
    /** The singleton instance of this {@link Client}. */
    private static Client INSTANCE;
    /** The maximum size of the byte buffer for multicast packets. */
    private static final int BUFFER_SIZE = DiscoveryRequest.getLargestSize();
    /** A list of known hosts to send drawing updates to. */
    private final Set<Triple<String, String, Integer>> knownHosts;
    /** The client name for this instance of the program as a String. */
    private String thisName;
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
        this.thisName = "UNNAMED";
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
        discoveryThread.setName("Multicast listener");
        discoveryThread.start();
        
        WhiteboardGUI.getInstance().setTitle(
                String.format("Distributed Whiteboard - %s", this.thisName)
        );
        
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
        // Inform the other nodes that this client is disconnecting.
        if (thisHost != null) {
            String ip = thisHost.Left;
            int port = thisHost.Right;
            broadCastMessage(new LeaveRequest(thisName, ip, port));
        }
        isSending = false; // Stop sending out updates.
        try {
            if (multicast != null && receiver != null && !receiver.isClosed()) {
                System.out.println("Stopping network discovery.");
                receiver.leaveGroup(multicast.Left);
                receiver.close();
            }
            if (discoveryThread != null)
                discoveryThread.join();
        } catch (IOException ex) {
            System.err.println("Failed to leave multicast group");
            System.err.println(ex.getMessage());
            return;
        } catch (InterruptedException ex) { }
        knownHosts.clear();
        WhiteboardGUI.getInstance().updateClientList();
        System.out.println("Stopped network discovery.");
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
    public synchronized boolean addHost(Triple<String, String, Integer> host)
    {
        Pair<String, Integer> hostpair = new Pair<>(host.Two, host.Three);
        if (hostpair.equals(thisHost)) 
            return false;
        
        if (knownHosts.add(host)) {
            System.out.printf("Added new host %s:%d%n", host.Two, host.Three);
            WhiteboardGUI.getInstance().updateClientList();
            return true;
        }
        
        System.out.printf("Host %s:%d already known.%n", host.Two, host.Three);
        return false;
    }
    
    /**
     * Removes the specified host from the list of hosts known to this client 
     * component of the Distributed Whiteboard.
     * 
     * @param host The host as a {@link Pair} containing the IP address as a 
     * {@link String} and the port as an {@link Integer}.
     * @since 1.4
     */
    public synchronized void removeHost(Triple<String, String, Integer> host)
    {
        knownHosts.remove(host);
        WhiteboardGUI.getInstance().updateClientList();
    }
    
    /**
     * Gets a {@link Set} of all the hosts known to this client component of 
     * the Distributed Whiteboard.
     * 
     * @return A {@link Set} containing {@link Pair}s of a {@link String} and a
     * {@link Integer}.
     * @since 1.4
     */
    public Set<Triple<String, String, Integer>> getKnownHosts() 
    { 
        return knownHosts; 
    }
    
    /**
     * Gets the name the client has set for use with this instance of the 
     * program.
     * 
     * @return The client name as a String.
     * @since 1.4
     */
    public String getClientName() { return thisName; }
    
    /**
     * Sets the name this client will identify itself to others with.
     * 
     * @param name The name to use as a String.
     * @since 1.4
     */
    public void setClientName(String name) { thisName = name; }
    
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
        for (Triple<String, String, Integer> host : knownHosts) {
            Pair<String, Integer> hostpair = new Pair<>(host.Two, host.Three);
            if (hostpair.equals(thisHost)) continue; // Don't message yourself.            
            sendMessage(message, host.Two, host.Three);
        }
    }
    
    /**
     * Sends a specified {@link BufferedImage} over TCP to the specified host.
     * The host is a {@link Pair} containing the IP address/ host name in the 
     * left type, and the port number in the right type.
     * 
     * @param image The {@link BufferedImage} to send to the specified host.
     * @param host The host to send to as a {@link Pair} containing the IP 
     * address as a {@link String} and the port as an {@link Integer}.
     * @since 1.4
     */
    public static void sendImage(final BufferedImage image, 
            final Pair<String, Integer> host)
    {
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {}
                try (Socket sender = new Socket(host.Left, Server.TCP_PORT)) {
                    ImageIO.write(image, "PNG", sender.getOutputStream());
                } catch (IOException ex) {
                    System.err.printf("Error sending image to host %s:%d%n%s%n", 
                            host.Left, host.Right, ex.getMessage());
                }
            }
        }).start();
    }
    
    /**
     * Sends out a multicast message to try and find other clients on the 
     * network to communicate drawings to.
     * 
     * @since 1.0
     */
    public void discoverClients()
    {
        if (multicast.Left == null) return; // No multicast group.
        
        DatagramPacket packet;
        
        DiscoveryRequest request = new DiscoveryRequest(thisName, thisHost.Left, 
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
    
    /**
     * Sends a request for a certain packet out over multicast. If any clients 
     * know of the packet they will send it to this host over UDP.
     * 
     * @param uuid The unique ID of the missing packet as a {@link String}.
     * @since 1.5
     */
    public void requestPacket(String uuid)
    {
        if (multicast.Left == null) return; // No multicast group.
        
        DatagramPacket packet;
        
        PacketRequestMessage request = new PacketRequestMessage(thisHost.Left, 
                thisHost.Right, uuid);
        
        byte[] buffer = request.encode();
        try (MulticastSocket sender = new MulticastSocket()) {
            packet = new DatagramPacket(buffer, buffer.length, 
                    multicast.Left, multicast.Right);
            sender.send(packet);
            System.out.println("Requesting packet.");
        } catch (IOException ex) {
            System.err.println("Failed to send packet request message.");
            System.err.println(ex.getMessage());
        }
    }
    
    /**
     * Requests an image from all the hosts on the multicast network. Only the 
     * host that knows the image should respond.
     * 
     * @param hash The hash code generated by {@link Object#hashCode()} for the 
     * {@link BufferedImage} to request.
     * @since 1.6
     */
    public void requestImage(int hash)
    {
        if (multicast.Left == null) return; // No multicast group.
        
        DatagramPacket packet;
        
        ImageRequestMessage request = 
                new ImageRequestMessage(hash, thisHost.Left);
        
        byte[] buffer = request.encode();
        try (MulticastSocket sender = new MulticastSocket()) {
            packet = new DatagramPacket(buffer, buffer.length, 
                    multicast.Left, multicast.Right);
            sender.send(packet);
            System.out.println("Requesting packet.");
        } catch (IOException ex) {
            System.err.println("Failed to send image request message.");
            System.err.println(ex.getMessage());
        }
    }
    
    /**
     * Responds to another instance of this program that is discovering new 
     * hosts to connect to by sending a {@link DiscoveryResponse} containing the
     *  IP address and UDP port of this host.
     * 
     * @param request The {@link DiscoveryMessage} sent by the host looking for 
     * new clients.
     * @since 1.4
     */
    private void sendDiscoveryResponse(DiscoveryMessage request)
    {
        Triple<String, String, Integer> source = 
                new Triple<>(request.Name, request.IP, request.Port);
        Pair<String, Integer> sourcePair = new Pair<>(request.IP, request.Port);
        if (sourcePair.equals(thisHost)) return;
        
        addHost(source);
        
        sendMessage(new DiscoveryResponse(thisName, thisHost.Left, 
                thisHost.Right), request.IP, request.Port);
    }
    
    /**
     * Sends a missing packet to the host that requested it. This relies on the 
     * client knowing the packet with the unique ID specified within the 
     * message.
     * 
     * @param message A {@link PacketRequestMessage} that contains the unique 
     * ID to search for and the host details to send it to.
     * @since 1.5
     */
    private void sendMissingPacket(PacketRequestMessage message)
    {
        Pair<String, Integer> sourcePair = 
                new Pair<>(message.SourceIP, message.SourcePort);
        if (sourcePair.equals(thisHost)) return;
        
        String uuid = message.getRequiredID();
        if (uuid != null && !uuid.isEmpty()) {
            NetMessage foundMsg = Server.messages.get(uuid);
            if (foundMsg != null) {
                System.out.println("Sending missing packet.");
                sendMessage(foundMsg, message.SourceIP, message.SourcePort);
            }
        }
    }

    /**
     * Runs a multicast listener in the background to let this instance of the 
     * Distributed Whiteboard application respond to other instances looking for
     *  others.
     * 
     * @since 1.1
     */
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
        NetMessage msg;
        while(isSending) {
            try {
                byte[] buff = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buff, buff.length);
                receiver.receive(packet);
                System.out.println("Received a mutlicast packet.");
                MessageType type = NetMessage.getMessageType(buff);
                switch (type) {
                    case DISCOVERY:
                        msg = DiscoveryMessage.decode(buff);
                        if (msg != null)
                            sendDiscoveryResponse((DiscoveryMessage)msg);
                        break;
                    case MISSING_PACKET:
                        msg = PacketRequestMessage.decode(buff);
                        if (msg != null)
                            sendMissingPacket((PacketRequestMessage)msg);
                        break;
                    case IMAGE_REQUEST:
                        msg = ImageRequestMessage.decode(buff);
                        if (msg != null) {
                            BufferedImage img = Server.images.get(((ImageRequestMessage)msg).ImageHash);
                            if (img != null) {
                                Pair<String, Integer> host = new Pair<>(((ImageRequestMessage)msg).SourceAddress, Server.TCP_PORT);
                                sendImage(img, host);
                            }
                        }
                        break;
                }
            } catch (IOException ex) {
                if (!isSending) return; // Socket closed.
                System.err.printf("Client multicast reciever error:%n%s", 
                        ex.getMessage());
            }
        }
    }
}
