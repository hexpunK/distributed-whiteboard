package distributedwhiteboard;

import java.util.UUID;

/**
 * Represents a message to be sent across a network. Messages will be encoded in
 * to a byte array, the contents of the array will be dependant on the sub-class
 * implementation.
 * 
 * @author 6266215
 * @version 1.1
 * @since 2015-03-17
 */
public abstract class NetMessage
{
    /** The number of bytes required to store a {@link UUID}. */
    protected static final int UUID_SZ = UUID.randomUUID().toString().length();
    /** The byte offset for the unique ID for this {@link NetMessage}. */
    protected static final int ID_OFFSET = 0;
    /** The byte offset for the unique ID this {@link NetMessage} relies on. */
    protected static final int RELY_OFFSET = ID_OFFSET+UUID_SZ;
    /** The byte offset for the {@link MessageType} of this message. */
    public static final int TYPE_OFFSET = RELY_OFFSET+UUID_SZ;
    
    /** The {@link MessageType} of this {@link WhiteboardMessage}. */
    public final MessageType type;
    /** The last unique ID used as an {@link UUID}. */
    private static UUID lastID;
    /** The unique ID for this packet as a {@link UUID}. */
    private UUID uniqueID;
    /** Any unique ID this packet relies on as a {@link UUID}. */
    private UUID requiredID;
    
    /**
     * Creates a new {@link NetMessage} with the specified type.
     * 
     * @param type The {@link MessageType} of this message.
     * @since 1.0
     */
    public NetMessage(MessageType type)
    {
        this.type = type;
        // Generate and set a new unique ID for this packet.
        synchronized (NetMessage.class) {
            if (lastID != null)
                this.requiredID = lastID;
            this.uniqueID = UUID.randomUUID();
            NetMessage.lastID = uniqueID;
        }
    }
    
    /**
     * Encodes this {@link NetMessage} into a byte array. Implementations of 
     * this abstract class need to implement this as needed for their contents.
     * 
     * @return Returns a byte array containing an econded copy of this {@link 
     * NetMessage}.
     * @since 1.0
     */
    public abstract byte[] encode();
    
    /**
     * Gets the unique ID for this {@link NetMessage}. This is a {@link String} 
     * representation of a Java {@link UUID}.
     * 
     * @return The unique ID for this message as a String.
     * @since 1.1
     */
    public String getUniqueID() { return uniqueID.toString(); }
    
    /**
     * Sets the unique ID for this {@link NetMessage} to the provided value. The
     *  {@link String} provided must be in the Java {@link UUID} format as 
     * specified in the documentation for {@link UUID#toString()}.
     * 
     * @param id The String to use as the unique ID for this message.
     * @since 1.1
     */
    public void setUniqueID(String id) { uniqueID = UUID.fromString(id); }
    
    /**
     * Gets the unique ID of a message that this {@link NetMessage} relies on 
     * to be ordered correctly. This is a {@link String} representation of a 
     * Java {@link UUID}.
     * 
     * @return The unique ID of a required message as a String. If no required 
     * ID exists, this will return null;
     * @since 1.1
     */
    public String getRequiredID() { return requiredID.toString(); }
    
    /**
     * Sets the unique ID for a required message for this {@link NetMessage}. 
     * The {@link String} provided must be in the Java {@link UUID} format as 
     * specified in the documentation for {@link UUID#toString()}. If an null 
     * or empty String is provided, the required ID will be null.
     * 
     * @param id 
     */
    public void setRequiredID(String id) { 
        if (id == null || id.isEmpty())
            requiredID = null;
        else
            requiredID = UUID.fromString(id); 
    }
    
    /**
     * Works out the {@link MessageType} of this {@link NetMessage}.
     * 
     * @param buffer The byte buffer containing this {@link NetMessage}.
     * @return The {@link MessageType} of this {@link NetMessage}, or null if it
     * cannot be determined.
     * @since 1.0
     */
    public static MessageType getMessageType(byte[] buffer)
    {
        String messageStr = new String(buffer);
        return MessageType.parseChar(messageStr.charAt(TYPE_OFFSET));
    }

    /**
     * Converts the unique ID, required ID and {@link MessageType} of this 
     * {@link NetMessage} to a {@link String} representation.
     * 
     * @return The unique ID, required ID and type of this message as a String.
     * @since 1.1
     */
    @Override
    public String toString() 
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append(uniqueID.toString());
        if (requiredID == null) {
            for (int i = 0; i < UUID_SZ; i++)
                sb.append(" ");
        } else
            sb.append(requiredID.toString());
        sb.append(type.type);
        
        return sb.toString();
    }
}
