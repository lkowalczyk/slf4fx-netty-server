package pl.bluetrain.slf4fx;

/**
 * Exception thrown when the incoming ChannelBuffer does not
 * have enough data to construct a complete incoming message.
 */
@SuppressWarnings("serial")
public class BufferUnderflowException extends Exception
{
    public BufferUnderflowException(Throwable cause)
    {
        super(cause.getMessage(), cause);
    }
}
