package distributedwhiteboard;

/**
 * Message types to help the server understand what a {@link WhiteboardMessage} 
 * is trying to communicate.
 * 
 * @version 1.1
 * @since 2015-03-17
 */
public enum MessageType
{
    /** This message contains data for a drawing object. */
    DRAW('d'),
    /** The message is requesting to join the network. */
    JOIN('j'),
    /** A client is disconnecting from the network. */
    LEAVE('l'),
    /** A client is looking for other clients to communicate with. */
    DISCOVERY('f'),
    /** A client is responding to a {@link MessageType#DISCOVERY} message. */
    RESPONSE('r'),
    /** Sent over multicast to request a missing packet. */
    MISSING_PACKET('m'),
    /** Requests an image from all hosts over multicast. */
    IMAGE_REQUEST('i');
        
    public final char type;
    private MessageType(char value) { this.type = value; }
        
    /**
     * Works out the {@link MessageType} of a provided character.
     * 
     * @param c The character to convert to a {@link MessageType}.
     * @return Returns a {@link MessageType} if a matching one is found, null 
     * otherwise.
     * @since 1.0
     */
    public static MessageType parseChar(char c)
    {
        for (MessageType t : MessageType.values()) {
            if (t.type == c)
                return t;
        }
        System.out.printf("Unknown MessageType '%c'%n", c);
        return null;
    }
    
    @Override        
    public String toString()
    {
        return String.format("%s(%c)", this.name(), this.type);
    }
}
