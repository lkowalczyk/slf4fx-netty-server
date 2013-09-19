package io.github.krankenwagen.slf4fx.message;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class AccessResponse
{
    private final boolean accessGranted;
    
    public AccessResponse(boolean accessGranted)
    {
        this.accessGranted = accessGranted;
    }

    public boolean isAccessGranted()
    {
        return accessGranted;
    }

    public ChannelBuffer asChannelBuffer()
    {
        return ChannelBuffers.wrappedBuffer(
                new byte[] { (byte) 2 },
                new byte[] { accessGranted ? (byte) 1 : (byte) 0 });
    }
}
