package pl.bluetrain.slf4fx.message;

/**
 * Thrown when a unknown message tag is encountered.
 * 
 * @author lkowalczyk
 */
@SuppressWarnings("serial")
public class UnknownMessageException extends Exception
{
    private final int value;
    
    UnknownMessageException(int value)
    {
        super("for value: " + value);
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
}
