package distributedwhiteboard;

/**
 *
 * @author Jordan
 */
public abstract class DiscoveryMessage extends NetMessage
{
    private static final byte IP_SIZE = 12;
    private static final byte PORT_SIZE = 5;
    
    private static final byte IP_OFFSET = 1;
    private static final byte PORT_OFFSET = IP_OFFSET+IP_SIZE;
    
    public final String IP;
    public final int Port;

    protected DiscoveryMessage(MessageType type, String ip, int port)
    {
        super(type);
        this.IP = ip;
        this.Port = port;
    }
    
    public static int getLargestSize()
    {
        return new DiscoveryRequest("000.000.000.000", 65535).encode().length;
    }
    
    @Override
    public byte[] encode()
    {
        return this.toString().getBytes();
    }
    
    public static DiscoveryMessage decode(byte[] buffer)
    {
        String messageStr = new String(buffer).trim();
        
        MessageType type = MessageType.parseChar(messageStr.charAt(0));
        String ipStr = messageStr.substring(IP_OFFSET, PORT_OFFSET);
        String portStr = messageStr.substring(PORT_OFFSET);
        int port;
        
        ipStr = stringToIP(ipStr);
        if (ipStr == null) return null;
        
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException nfe) {
            port = -1;
        }
        
        if (port < 1 || port > 65535) {
            System.err.println("Port value is not a valid TCP port.");
            return null;
        }
        
        if (type == MessageType.DISCOVERY)
            return new DiscoveryRequest(ipStr, port);
        else if (type == MessageType.RESPONSE)
            return new DiscoveryResponse(ipStr, port);
        else if (type == MessageType.JOIN)
            return new JoinRequest(ipStr, port);
        else {
            System.err.println("Invalid message type.");
            return null;
        }
    }
    
    private static String stringToIP(String str)
    {
        String[] octectStrs = new String[4];
        
        try {
            octectStrs[0] = str.substring(0, 3);
            octectStrs[1] = str.substring(3, 6);
            octectStrs[2] = str.substring(6, 9);
            octectStrs[3] = str.substring(9, 12);
        } catch (IndexOutOfBoundsException indEx) {
            System.err.println("IP Address is not properly formed.");
            return null;
        }
        
        int[] octects = new int[octectStrs.length];
        try {
            for (int i = 0; i < octectStrs.length; i++)
                octects[i] = Integer.parseInt(octectStrs[i]);
        } catch (NumberFormatException nfe) {
            System.err.println("IP Address contained non-numeric character.");
        }
        
        return String.format("%d.%d.%d.%d", octects[0], octects[1], 
                octects[2], octects[3]);
    }
    
    private static String ipToString(String address)
    {
        String[] octectStrs = address.split("\\.");
        if (octectStrs.length != 4) return null;
        
        int[] octects = new int[octectStrs.length];
        
        for (int i = 0; i < octects.length; i++) {
            try {
                octects[i] = Integer.parseInt(octectStrs[i]);
            } catch (NumberFormatException nfe) {
                System.err.println("Provided IP address is incorrectly formed.");
                return null;
            }
        }
        
        return String.format("%03d%03d%03d%03d", 
                octects[0], octects[1], octects[2], octects[3]);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(type.value);
        
        String ipStr = DiscoveryMessage.ipToString(IP);
        String portStr = String.format("%05d", Port);
        
        if (ipStr == null || ipStr.isEmpty() || portStr.isEmpty())
            return null;
        
        sb.append(ipStr);
        sb.append(portStr);
        return sb.toString();
    }
    
    public static class DiscoveryRequest extends DiscoveryMessage
    {
        public DiscoveryRequest(String ipAddress, int portNumber)
        {
            super(MessageType.DISCOVERY, ipAddress, portNumber);
        }
    }
    
    public static class DiscoveryResponse extends DiscoveryMessage
    {
        public DiscoveryResponse(String ipAddress, int portNumber)
        {
            super(MessageType.RESPONSE, ipAddress, portNumber);
        }
    }
    
    public static class JoinRequest extends DiscoveryMessage
    {
        public JoinRequest(String ipAddress, int portNumber)
        {
            super(MessageType.JOIN, ipAddress, portNumber);
        }
    }
}
