package io.github.krankenwagen.slf4fx.message;

import io.github.krankenwagen.slf4fx.BufferUnderflowException;

import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class InboundMessage
{
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final InboundMessage.Decoder[] decoders = { AccessRequest.decoder(), LogRecord.decoder() };
    
    public static InboundMessage decode(ChannelBuffer buffer) throws BufferUnderflowException, UnknownMessageException
    {
        for (InboundMessage.Decoder decoder : decoders)
        {
            InboundMessage im = decoder.decode(buffer);
            if (im != null)
                return im;
        }
        buffer.markReaderIndex();
        int tag = buffer.readByte();
        buffer.resetReaderIndex();
        throw new UnknownMessageException(tag);
    }
    
    static abstract class Decoder
    {
        /**
         * Returns an InboundMessage instance or null, if the message is of
         * different type.
         * 
         * @return InboundMessage or null.
         * @throws BufferUnderflowException
         *             If there is not enough data in the buffer to construct a
         *             complete message. The buffer marker is left intact in
         *             this case.
         */
        abstract InboundMessage decode(ChannelBuffer buffer)
            throws BufferUnderflowException;
        
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
        protected static String readUTF(ChannelBuffer buffer)
            throws BufferUnderflowException
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
