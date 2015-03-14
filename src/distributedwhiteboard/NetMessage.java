package distributedwhiteboard;

/**
 * Represents a message to be sent across a network. Messages will be encoded in
 * to a byte array, the contents of the array will be dependant on the sub-class
 * implementation.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-03-14
 */
public abstract class NetMessage
{
    /** The {@link MessageType} of this {@link WhiteboardMessage}. */
    public final MessageType type;
    
    /**
     * Creates a new {@link NetMessage} with the specified type.
     * 
     * @param type The {@link MessageType} of this message.
     * @since 1.0
     */
    public NetMessage(MessageType type)
    {
        this.type = type;
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
        return MessageType.parseChar(messageStr.charAt(0));
    }
}
