package pl.bluetrain.slf4fx.message;

/**
 * Thrown when there is not enough data in a buffer to
 * construct a full message or a full part of it.
 * 
 * @author lkowalczyk
 */
@SuppressWarnings("serial")
public class BufferUnderflowException extends Exception
{
    public BufferUnderflowException(IndexOutOfBoundsException e)
    {
        super(e.getMessage(), e);
    }
}
