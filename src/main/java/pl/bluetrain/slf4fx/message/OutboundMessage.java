package pl.bluetrain.slf4fx.message;

import java.io.ByteArrayOutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Base class for all outbound messages. Provides encoding capabilities.
 * 
 * @author Łukasz Kowalczyk <lukasz@bluetrain.pl>
 */
public abstract class OutboundMessage
{
    private final ByteArrayOutputStream os = new ByteArrayOutputStream();

    public final ChannelBuffer asChannelBuffer()
    {
        doEncode();
        return ChannelBuffers.wrappedBuffer(os.toByteArray());
    }
    
    /**
     * When called, this method should in turn output encoded data
     * using the {@code write*} methods in this class.
     */
    protected abstract void doEncode();
    
    protected final void writeByte(int value)
    {
        os.write(value);
    }
    
    protected final void writeBytes(byte[] bytes)
    {
        os.write(bytes, 0, bytes.length);
    }
}
