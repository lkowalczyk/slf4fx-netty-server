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

import pl.bluetrain.slf4fx.MessageType;

/**
 * Base class for incoming messages.
 * 
 * @author lkowalczyk
 */
public abstract class InboundMessage
{
    private static final Logger log = LoggerFactory.getLogger(InboundMessage.class);
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final Decoder[] decoders = { AccessRequest.decoder(), LogRecord.decoder() };
    
    /**
     * This method attempts to decode the message using all its
     * registered decoders in turn. Null is returned if the buffer
     * does not have enough data to fully construct a message.
     * 
     * @return InboundMessage or null.
     * @throws UnknownMessageException If no suitable decoder could be found.
     */
    public static InboundMessage decode(ChannelBuffer buffer) throws UnknownMessageException
    {
        if (!buffer.readable())
            return null;
        
        for (InboundMessage.Decoder decoder : decoders)
        {
            if (decoder.isSuitable(buffer))
            {
                return decoder.decode(buffer);
            }
        }
        throw new UnknownMessageException(buffer.getUnsignedByte(0));
    }
    
    static abstract class Decoder
    {
        private final MessageType tag;
        private static ThreadLocal<ChannelBuffer> buffer = new ThreadLocal<ChannelBuffer>();
        
        Decoder(MessageType tag)
        {
            if (tag == null)
                throw new NullPointerException("tag");
            this.tag = tag;
        }

        abstract InboundMessage doDecode() throws BufferUnderflowException;
        
        /**
         * Returns {@code true} if the first byte in the provided buffer
         * is identical with this decoder's tag value.
         * 
         * @throws IndexOutOfBoundException if the buffer has no readable bytes.
         * @return true if the message in buffer can be decoded with this decoder.
         */
        final boolean isSuitable(ChannelBuffer buffer)
        {
            return buffer.getUnsignedByte(0) == tag.getTag();
        }
        
        /**
         * Returns an InboundMessage instance or null, if the buffer does
         * not have enough data to construct a complete message.
         * 
         * @return InboundMessage or null.
         */
        InboundMessage decode(ChannelBuffer buf)
        {
            if (buf == null)
                throw new NullPointerException("null");
            try
            {
                buffer.set(buf);
                buffer.get().markReaderIndex();
                buffer.get().readByte(); // tag
                return doDecode();
            }
            catch (BufferUnderflowException e)
            {
                buffer.get().resetReaderIndex();
            }
            buffer.set(null);
            return null;
        }
        
        protected int readInt() throws BufferUnderflowException
        {
            try
            {
                return buffer.get().readInt();
            }
            catch (IndexOutOfBoundsException e)
            {
                throw new BufferUnderflowException(e);
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
         * @throws BufferUnderflowException
         */
        protected String readUTF()
            throws BufferUnderflowException
        {
            try
            {
                int length = buffer.get().readShort() & 0xffff;
                ByteBuffer bb = ByteBuffer.allocate(length);
                buffer.get().readBytes(bb);
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
                    // We wanted this by setting CodingErrorAction.REPORT,
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
                throw new BufferUnderflowException(e);
            }
        }
    }
}
