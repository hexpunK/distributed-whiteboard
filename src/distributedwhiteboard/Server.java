package distributedwhiteboard;

import java.net.DatagramSocket;

/**
 *
 * @author Jordan
 */
public class Server implements Runnable
{
    private static final Server INSTANCE = new Server();
    private DatagramSocket socket;
    
    private Server()
    {
        this.socket = null;
    }
    
    public static Server getInstance()
    {
        return Server.INSTANCE;
    }
    
    @Override
    public void run()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
