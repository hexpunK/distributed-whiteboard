package distributedwhiteboard;

/**
 *
 * @author 6266215
 */
public class ImageRequestMessage extends NetMessage
{
    /** The number of characters in an encoded SourceIP address. */
    private static final int IP_SIZE = 12;    
    /** The character offset in the message for the start of the SourceIP address. */
    private static final int IP_OFFSET = TYPE_OFFSET+1;
    /** The character offset in the message for the start of the port number. */
    private static final int HASH_OFFSET = IP_OFFSET+IP_SIZE;
    
    public final String SourceAddress;
    public final int ImageHash;
    
    public ImageRequestMessage(int hash, String ip)
    {
        super(MessageType.IMAGE_REQUEST);
        this.SourceAddress = ip;
        this.ImageHash = hash;
    }
    
    public static int getLargestSize() 
    { 
        return new ImageRequestMessage(0, "000.000.000.000").encode().length; 
    }

    @Override
    public byte[] encode() { return this.toString().getBytes(); }

    public static ImageRequestMessage decode(byte[] buffer)
    {
        String msgStr = new String(buffer).trim();
        
        String sourceStr = msgStr.substring(IP_OFFSET, HASH_OFFSET);
        String hashStr = msgStr.substring(HASH_OFFSET);
        
        String ipStr = Conversions.stringToIP(sourceStr);
        if (ipStr == null) return null;
        
        int hashNum;
        try {
            hashNum = Integer.parseInt(hashStr);
        } catch (NumberFormatException nfe) {
            return null;
        }
        
        return new ImageRequestMessage(hashNum, ipStr);
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append(super.toString());
        sb.append(Conversions.ipToString(SourceAddress));
        sb.append(String.format("%010d", ImageHash));
        
        return sb.toString();
    }
}
