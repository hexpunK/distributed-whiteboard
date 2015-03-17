package distributedwhiteboard;

/**
 * {@link DiscoveryMessage} is an abstract class that represents the base for a 
 * variety of messages that are used in the network discovery protocol for the 
 * Distributed Whiteboard application. The messages all contain the same data, 
 * the IP address/ host name of the source, and a port number to send UDP 
 * packets over.
 * 
 * @author 6266215
 * @version 1.2
 * @since 2015-03-07
 */
public abstract class DiscoveryMessage extends NetMessage
{
    /** The number of characters in an encoded IP address. */
    private static final int IP_SIZE = 12;
    /** The number of characters in an encoded port. */
    private static final int PORT_SIZE = 5;
    
    private static final int NAME_SIZE = 20;
    
    /** The character offset in the message for the start of the IP address. */
    private static final int IP_OFFSET = TYPE_OFFSET+1;
    /** The character offset in the message for the start of the port number. */
    private static final int PORT_OFFSET = IP_OFFSET+IP_SIZE;
    
    private static final int NAME_OFFSET = PORT_OFFSET+PORT_SIZE;
    
    public final String Name;
    /** The IP address for the source of this {@link DiscoveryMessage}. */
    public final String IP;
    /** The UDP port address for the source of this {@link DiscoveryMessage}. */
    public final int Port;

    /**
     * Creates a new {@link DiscoveryMessage} with the specified {@link 
     * MessageType}, IP address and port number stored inside.
     * 
     * @param type The {@link MessageType} of this message. This will normally 
     * be one of; {@link MessageType#DISCOVERY}, {@link MessageType#RESPONSE}, 
     * {@link MessageType#JOIN} or {@link MessageType#LEAVE}.
     * @param ip The IP address or host name to send as a String.
     * @param port The UDP port number to send as an int.
     * @since 1.0
     */
    protected DiscoveryMessage(MessageType type, String name, String ip, int port)
    {
        super(type);
        this.Name = name;
        this.IP = ip;
        this.Port = port;
    }
    
    /**
     * Works out the maximum number of bytes needed to store and version of a 
     * {@link DiscoveryMessage}.
     * 
     * @return The maximum size of a {@link DiscoveryMessage} as an int.
     * @since 1.0
     */
    public static int getLargestSize()
    {        
        return new DiscoveryRequest("12345678901234567890", 
                "000.000.000.000", 65535).encode().length;
    }
    
    /**
     * Converts this {@link DiscoveryMessage} into a byte array. This converts 
     * the {@link String} representation of this message into bytes.
     * 
     * @return A byte array containing this message.
     * @since 1.0
     */
    @Override
    public byte[] encode()
    {
        return this.toString().getBytes();
    }
    
    /**
     * Decodes a byte array into a implementation of {@link DiscoveryMessage}. 
     * If the byte array is not properly formed for the discovery protocol, this 
     *  will return rather than an instance of a sub class.
     * 
     * @param buffer The byte array to decode into a {@link DiscoveryMessage} 
     * implementation.
     * @return Returns a {@link DiscoveryMessage} containing the source IP and 
     * port if the message can be decoded. Null otherwise.
     * @since 1.0
     */
    public static DiscoveryMessage decode(byte[] buffer)
    {
        String messageStr = new String(buffer).trim();
        
        String uID = messageStr.substring(ID_OFFSET, RELY_OFFSET);
        String rID = messageStr.substring(RELY_OFFSET, TYPE_OFFSET);
        MessageType type = MessageType.parseChar(messageStr.charAt(TYPE_OFFSET));
        String ipStr = messageStr.substring(IP_OFFSET, PORT_OFFSET);
        String portStr = messageStr.substring(PORT_OFFSET, NAME_OFFSET);
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
        
        String nStr = messageStr.substring(NAME_OFFSET);
        nStr = nStr.trim();
        
        DiscoveryMessage msg = null;
        if (type == MessageType.DISCOVERY)
            msg = new DiscoveryRequest(nStr, ipStr, port);
        else if (type == MessageType.RESPONSE)
            msg = new DiscoveryResponse(nStr, ipStr, port);
        else if (type == MessageType.JOIN)
            msg = new JoinRequest(nStr, ipStr, port);
        else if (type == MessageType.LEAVE)
            msg = new LeaveRequest(nStr, ipStr, port);
        
        if (msg != null) {
            msg.setUniqueID(uID);
            msg.setRequiredID(rID);
            return msg;
        }
        
        System.err.println("Invalid message type.");
        return null;
    }
    
    /**
     * Converts a {@link String} to a properly formed IPv4 IP address.
     * 
     * @param str The encoded String to convert.
     * @return The decoded IP address as a String. If the String cannot be 
     * decoded properly, this will return null instead.
     * @since 1.0
     */
    private static String stringToIP(String str)
    {
        String[] octetStrs = new String[4];
        
        try {
            octetStrs[0] = str.substring(0, 3);
            octetStrs[1] = str.substring(3, 6);
            octetStrs[2] = str.substring(6, 9);
            octetStrs[3] = str.substring(9, 12);
        } catch (IndexOutOfBoundsException indEx) {
            System.err.println("IP Address is not properly formed.");
            return null;
        }
        
        int[] octets = new int[octetStrs.length];
        try {
            for (int i = 0; i < octetStrs.length; i++)
                octets[i] = Integer.parseInt(octetStrs[i]);
        } catch (NumberFormatException nfe) {
            System.err.println("IP Address contained non-numeric character.");
            return null;
        }
        
        return String.format("%d.%d.%d.%d", octets[0], octets[1], 
                octets[2], octets[3]);
    }
    
    /**
     * Converts a standard representation of an IPv4 address to an encoded 
     * String to be sent over the network. This strips out the periods and pads 
     * each octet to 3 characters in length.
     * 
     * @param address The IPv4 formatted address to convert to an encoded form.
     * @return Returns the encoded String if it is encoded, otherwise returns 
     * null.
     * @since 1.0
     */
    private static String ipToString(String address)
    {
        String[] octetStrs = address.split("\\.");
        if (octetStrs.length != 4) return null;
        
        int[] octets = new int[octetStrs.length];
        
        for (int i = 0; i < octets.length; i++) {
            try {
                octets[i] = Integer.parseInt(octetStrs[i]);
            } catch (NumberFormatException nfe) {
                System.err.println("Provided IP address is incorrectly formed.");
                return null;
            }
        }
        
        return String.format("%03d%03d%03d%03d", 
                octets[0], octets[1], octets[2], octets[3]);
    }

    /**
     * Generates a {@link String} representation of this {@link 
     * DiscoveryMessage} and its contents. This String contains the encoded 
     * version of the message rather than a easily readable form.
     * 
     * @return The String representation of this message.
     * @since 1.0
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        
        String ipStr = DiscoveryMessage.ipToString(IP);
        String portStr = String.format("%05d", Port);
        
        if (ipStr == null || ipStr.isEmpty() || portStr.isEmpty())
            return null;
        
        int diff = 20 - Name.length();
        
        sb.append(ipStr);
        sb.append(portStr);
        sb.append(Name);
        for (int i = diff; i > 0; i--) {
            sb.append(" ");
        }
        return sb.toString();
    }
    
    /**
     * A {@link DiscoveryMessage} that will be sent to all hosts in a certain 
     * multicast group to see if any of them are running the client for this 
     * program. Any instance that picks up this message should reply with a 
     * {@link DiscoveryResponse} message.
     * 
     * @version 1.0
     * @since 1.0
     */
    public static class DiscoveryRequest extends DiscoveryMessage
    {
        public DiscoveryRequest(String name, String ipAddress, int portNumber)
        {
            super(MessageType.DISCOVERY, name, ipAddress, portNumber);
        }
    }
    
    /**
     * A {@link DiscoveryMessage} sent in reply to a {@link DiscoveryRequest} 
     * when the host looking for others is not already known. Tells the host 
     * searching for new connections to add the sender of this message.
     * 
     * @version 1.0
     * @version 1.1
     */
    public static class DiscoveryResponse extends DiscoveryMessage
    {
        public DiscoveryResponse(String name, String ipAddress, int portNumber)
        {
            super(MessageType.RESPONSE, name, ipAddress, portNumber);
        }
    }
    
    /**
     * Sent from a client that wishes to join the peer-to-peer network to inform
     *  an existing client that it needs a copy of the {@link WhiteboardCanvas}.
     * 
     * @version 1.0
     * @since 1.1
     */
    public static class JoinRequest extends DiscoveryMessage
    {
        public JoinRequest(String name, String ipAddress, int portNumber)
        {
            super(MessageType.JOIN, name, ipAddress, portNumber);
        }
    }
    
    /**
     * Sent by a client that is disconnecting from the network. This allows the 
     * other hosts in the network to avoid wasting time sending messages to the 
     * sender of this message.
     * 
     * @version 1.0
     * @since 1.2
     */
    public static class LeaveRequest extends DiscoveryMessage
    {
        public LeaveRequest(String name, String ipAddress, int portNumber)
        {
            super(MessageType.LEAVE, name, ipAddress, portNumber);
        }
    }
}
