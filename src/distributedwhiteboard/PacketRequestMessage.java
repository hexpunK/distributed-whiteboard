package distributedwhiteboard;

import static distributedwhiteboard.NetMessage.TYPE_OFFSET;

/**
 *
 * @author 6266215
 */
public class PacketRequestMessage extends NetMessage
{
    /** The number of characters in an encoded SourceIP address. */
    private static final int IP_SIZE = 12;
    /** The number of characters in an encoded port. */
    private static final int PORT_SIZE = 5;
    
    /** The character offset in the message for the start of the SourceIP address. */
    private static final int IP_OFFSET = TYPE_OFFSET+1;
    /** The character offset in the message for the start of the port number. */
    private static final int PORT_OFFSET = IP_OFFSET+IP_SIZE;
    
    /** The SourceIP address for the source of this {@link PacketRequestMessage}. */
    public final String SourceIP;
    /** The UDP port address for the source of this message. */
    public final int SourcePort;
    
    public PacketRequestMessage(String ip, int port, String uuid)
    {
        super(MessageType.MISSING_PACKET);
        this.SourceIP = ip;
        this.SourcePort = port;
        this.setRequiredID(uuid);
    }

    @Override
    public byte[] encode()
    {
        return this.toString().getBytes();
    }
    
    public static PacketRequestMessage decode(byte[] msg)
    {
        String messageStr = new String(msg).trim();
        String ipStr = messageStr.substring(IP_OFFSET, PORT_OFFSET);
        String ipAddress = Conversions.stringToIP(ipStr);
        
        String portStr = messageStr.substring(PORT_OFFSET);
        int portNum;
        try {
            portNum = Integer.parseInt(portStr);
        } catch (NumberFormatException nfe) {
            return null;
        }
        
        String uuidStr = messageStr.substring(RELY_OFFSET, TYPE_OFFSET);
        
        return new PacketRequestMessage(ipAddress, portNum, uuidStr);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        
        sb.append(Conversions.ipToString(SourceIP));
        sb.append(String.valueOf(SourcePort));
        
        return sb.toString();
    }
}
