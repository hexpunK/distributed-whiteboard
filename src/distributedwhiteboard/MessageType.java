package distributedwhiteboard;

/**
 * Message types to help the server understand what a {@link WhiteboardMessage} 
 * is trying to communicate.
 * 
 * @version 1.0
 * @since 2015-03-14
 */
public enum MessageType
{
    /** This message contains data for a drawing object. */
    DRAW('d'),
    /** The message is requesting to join the network. */
    JOIN('j'),
    /** A client is looking for other clients to communicate with. */
    DISCOVERY('f'),
    /** A client is responding to a {@link MessageType#DISCOVERY} message. */
    RESPONSE('r');
        
    public final char value;
    private MessageType(char value) { this.value = value; }
        
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
        for (MessageType type : MessageType.values()) {
            if (type.value == c)
                return type;
        }
        System.out.printf("Unknown MessageType '%c'%n", c);
        return null;
    }
    
    @Override        
    public String toString()
    {
        return String.format("%s(%c)", this.name(), this.value);
    }
}
