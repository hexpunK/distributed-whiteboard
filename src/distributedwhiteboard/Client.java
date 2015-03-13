package distributedwhiteboard;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 *
 * @author 6266215
 */
public class Client
{
    private static Client INSTANCE;
    private final ArrayList<Pair<String, Integer>> knownHosts;
    private Pair<String, Integer> thisHost;
    
    private Client()
    {
        knownHosts = new ArrayList<>();
        knownHosts.add(new Pair("192.168.0.69", 55551));
        knownHosts.add(new Pair("192.168.0.69", 55552));
    }
    
    public static Client getInstance() 
    {
        if (Client.INSTANCE == null)
            Client.INSTANCE = new Client();
        
        return Client.INSTANCE;
    }
    
    public void setHost(String hostname, int port) 
    {
        thisHost = new Pair<>(hostname, port);
    }
    
    public void sendMessage(WhiteboardMessage message)
    {
        byte[] bytes = message.toString().getBytes();
        for (Pair<String, Integer> host : knownHosts) {
            if (host.equals(thisHost)) continue; // Don't message yourself.
            
            InetAddress address;
            DatagramSocket socket;
            DatagramPacket packet;
            try {
                address = InetAddress.getByName(host.Left);
            } catch (UnknownHostException hostEx) {
                System.err.printf("Could not find host %s:%d\n", host.Left, host.Right);
                continue;
            }
            
            try {
                socket = new DatagramSocket();
            } catch (SocketException sockEx) {
                System.err.println("Could not create DatagramSocket");
                continue;
            }
            
            try {
                packet = new DatagramPacket(bytes, bytes.length, address, host.Right);
                socket.send(packet);
            } catch (IOException ioEx) {
                System.err.printf("Error sending packet:\n%s\n", ioEx.getMessage());
            }
        }
    }
}
