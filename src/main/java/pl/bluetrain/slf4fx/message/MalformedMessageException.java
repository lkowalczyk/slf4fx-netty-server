package pl.bluetrain.slf4fx.message;

/**
 * Thrown when a unknown message tag is encountered.
 * 
 * @author lkowalczyk
 */
public class MalformedMessageException extends Exception
{
    public MalformedMessageException(String message)
    {
        super(message);
    }
}
