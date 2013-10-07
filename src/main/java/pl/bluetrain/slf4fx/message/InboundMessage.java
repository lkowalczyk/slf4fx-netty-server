package pl.bluetrain.slf4fx.message;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for incoming messages.
 * 
 * @author lkowalczyk
 */
public abstract class InboundMessage
{
    private static final Logger log = LoggerFactory.getLogger(InboundMessage.class);
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private ChannelBuffer buffer;

    /**
     * Attempts parsing a message.
     * @return An concrete InboundMessage descendant or null if the message is
     *  of different type than the instantiated class.
     * @throws BufferUnderrunException if there is not enough data in the ChannelBuffer.
     * @throws MalformedMessageException if the message is of correct type but cannot be parsed.
     */
    public InboundMessage tryDecode(ChannelBuffer buffer) throws BufferUnderrunException, MalformedMessageException
    {
        if (buffer == null)
            throw new NullPointerException("buffer");
        if (false == buffer.readable())
            throw new BufferUnderrunException();
        if (getType().getTag() != buffer.getUnsignedByte(0))
            return null;
        this.buffer = buffer;
        buffer.markReaderIndex();
        try
        {
            doDecode();
        }
        catch (BufferUnderrunException e)
        {
            buffer.resetReaderIndex();
            throw e;
        }
        catch (MalformedMessageException e)
        {
            buffer.resetReaderIndex();
            throw e;
        }
        return this;
    }
    
    protected abstract MessageType getType();

    /**
     * Attempts parsing a message.
     * @return true if the message was successfully parsed, or false if the message
     *  is of different type than the instantiated class.
     * @throws BufferUnderrunException if there is not enough data in the ChannelBuffer.
     */
    protected abstract void doDecode() throws BufferUnderrunException, MalformedMessageException;

    /**
     * Reads and returns an unsigned byte.
     * @throws BufferUnderrunException
     */
    protected int readByte() throws BufferUnderrunException
    {
        try
        {
            return buffer.readByte() & 0xff; // remove sign
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new BufferUnderrunException();
        }
    }
    
    protected byte[] readBytes(int length) throws BufferUnderrunException
    {
        if (buffer.readableBytes() < length)
            throw new BufferUnderrunException();
        byte[] result = new byte[length];
        buffer.readBytes(result);
        return result;
    }

    /**
     * Reads and returns a 32-bit signed integer.
     * @throws BufferUnderrunException
     */
    protected int readInt() throws BufferUnderrunException
    {
        try
        {
            return buffer.readInt();
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new BufferUnderrunException();
        }
    }
    
    /**
     * Reads data written to the socket using ActionScript's Socket.writeUTF
     * method. First comes length as a 16-bit unsigned integers, followed by
     * UTF-8 payload of the specified length.
     * 
     * If an invalid UTF-8 sequence is found, it is replaced with a space
     * and a warning is logged.
     * 
     * @param buffer
     * @return
     * @throws BufferUnderrunException
     */
    protected String readUTF()
        throws BufferUnderrunException
    {
        try
        {
            int length = buffer.readShort() & 0xffff;
            ByteBuffer bb = ByteBuffer.allocate(length);
            buffer.readBytes(bb);
            bb.flip();
            
            CharBuffer cb;
            try
            {
                CharsetDecoder decoder = UTF_8.newDecoder();
                decoder.onMalformedInput(CodingErrorAction.REPORT);
                cb = decoder.decode(bb);
            }
            catch (CharacterCodingException e)
            {
                // We wanted this exception by setting CodingErrorAction.REPORT,
                // now we can log a warning and decode the invalid
                // data while replacing invalid sequences with spaces.
                log.warn("{}", e.toString());
                CharsetDecoder decoder = UTF_8.newDecoder();
                decoder.onMalformedInput(CodingErrorAction.REPLACE);
                decoder.replaceWith(" ");
                try
                {
                    cb = decoder.decode(bb);
                }
                catch (CharacterCodingException cce)
                {
                    // should never happen (CodingErrorAction.REPLACE)
                    log.error(cce.toString(), cce);
                    cb = CharBuffer.allocate(0);
                }
            }
            
            String result = cb.toString();
            
            return result;
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new BufferUnderrunException();
        }
    }
}
