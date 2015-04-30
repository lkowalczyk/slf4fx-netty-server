package io.github.lkowalczyk.slf4fx.message;

/**
 * Thrown when a unknown message tag is encountered.
 * 
 * @author Łukasz Kowalczyk &lt;lkowalczyk@gmail.com&gt;
 */
public class MalformedMessageException extends Exception
{
    public MalformedMessageException(String message)
    {
        super(message);
    }
}
