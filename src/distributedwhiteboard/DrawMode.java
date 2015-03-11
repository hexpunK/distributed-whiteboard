package distributedwhiteboard;

/**
 *
 * @author Jordan
 */
public enum DrawMode
{
    LINE,
    TEXT,
    POLYGON,
    FREEFORM_LINE,
    RECTANGLE;
    
    /**
     * Format the names of each enum entry to make them nicer for users to read.
     * Names will have all underscores replaced with spaces, and the first 
     * character of each word capitalised.
     * 
     * @return Returns the formatted name of an enum value as a String.
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
