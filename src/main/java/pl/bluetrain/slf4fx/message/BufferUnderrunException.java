package pl.bluetrain.slf4fx.message;

/**
 * Thrown when there is not enough data in a buffer to
 * construct a full message or a full part of it.
 * 
 * @author Łukasz Kowalczyk <lukasz@bluetrain.pl>
 */
public class BufferUnderrunException extends Exception
{
    public BufferUnderrunException()
    {
    }
}
