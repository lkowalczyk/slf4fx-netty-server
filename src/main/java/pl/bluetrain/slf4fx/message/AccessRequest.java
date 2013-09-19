package pl.bluetrain.slf4fx.message;

import org.jboss.netty.buffer.ChannelBuffer;

import pl.bluetrain.slf4fx.BufferUnderflowException;
import pl.bluetrain.slf4fx.MessageType;

public class AccessRequest extends InboundMessage
{
    private final String applicationId;
    private final String secret;
    
    public AccessRequest(String applicationId, String secret)
    {
        this.applicationId = applicationId;
        this.secret = secret;
    }
    
    public String getApplicationId()
    {
        return applicationId;
    }
    
    public String getSecret()
    {
        return secret;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((applicationId == null) ? 0 : applicationId.hashCode());
        result = prime * result + ((secret == null) ? 0 : secret.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AccessRequest other = (AccessRequest) obj;
        if (applicationId == null)
        {
            if (other.applicationId != null)
                return false;
        }
        else if (!applicationId.equals(other.applicationId))
            return false;
        if (secret == null)
        {
            if (other.secret != null)
                return false;
        }
        else if (!secret.equals(other.secret))
            return false;
        return true;
    }
    
    @Override
    public String toString()
    {
        return "AccessRequest [applicationId=" + applicationId + ", secret=" + secret + "]";
    }
    
    static Decoder decoder()
    {
        return new Decoder()
        {
            @Override
            InboundMessage decode(ChannelBuffer buffer)
                throws BufferUnderflowException
            {
                buffer.markReaderIndex();
                try
                {
                    int tag = buffer.readByte() & 0xff;
                    if (tag != MessageType.ACCESS_REQUEST.getTag())
                    {
                        buffer.resetReaderIndex();
                        return null;
                    }
                    
                    String applicationId = readUTF(buffer);
                    String secret = readUTF(buffer);
                    return new AccessRequest(applicationId, secret);
                }
                catch (IndexOutOfBoundsException e)
                {
                    buffer.resetReaderIndex();
                    throw new BufferUnderflowException(e);
                }
            }
        };
    }
}
