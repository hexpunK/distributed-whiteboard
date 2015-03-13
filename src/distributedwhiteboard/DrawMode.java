package distributedwhiteboard;

/**
 * {@link DrawMode} holds all the possible drawing modes this application 
 * supports.
 * 
 * @author 6266215
 * @version 1.2
 * @since 2015-03-12
 */
public enum DrawMode
{
    /** 
     * Draws straight lines between two points.
     * @since 1.0
     */
    LINE('l'),
    /** 
     * Draws a {@link String} at a specified point.
     * @since 1.0
     */
    TEXT('t'),
    /** 
     * Draws straight lines between points, but joins the last and first.
     * @since 1.1
     */
    POLYGON('p'),
    /** 
     * Allows the user to click and drag a arbritrary shape.
     * @since 1.1
     */
    FREEFORM_LINE('f'),
    /** 
     * Draws a rectangle between two points.
     * @since 1.1
     */
    RECTANGLE('r');
    
    public final char value;
    private DrawMode(char val) { this.value = val; }
    
    /**
     * Parses a provided character and provides the correct {@link DrawMode} 
     * enumeration value for that character.
     * 
     * @param c The character to parse.
     * @return A {@link DrawMode} value if a matching one is found, null 
     * otherwise.
     * @since 1.2
     */
    public static DrawMode parseChar(char c)
    {
        switch(c) {
            case 'l':
                return LINE;
            case 't':
                return TEXT;
            case 'p':
                return POLYGON;
            case 'f':
                return FREEFORM_LINE;
            case 'r':
                return RECTANGLE;
            default:
                System.out.printf("Unknown DrawType '%c'\n", c);
                return null;
        }
    }
    
    /**
     * Format the names of each enum entry to make them nicer for users to read.
     * Names will have all underscores replaced with spaces, and the first 
     * character of each word capitalised.
     * 
     * @return Returns the formatted name of an enum value as a String.
     * @since 1.0
     */
    @Override
    public String toString()
    {
        String lowerCase = this.name().toLowerCase();
        lowerCase = lowerCase.replaceAll("_", " ");
        String[] words = lowerCase.split("\\s+");
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            name.append(words[i].substring(0, 1).toUpperCase());
            name.append(words[i].substring(1));
            if (i+1 < words.length)
                name.append(" ");
        }
        
        return name.toString();
    }
}
