package io.github.lkowalczyk.slf4fx.message;


public class AccessRequest extends InboundMessage
{
    private String applicationId;
    private String secret;

    @Override
    protected MessageType getType()
    {
        return MessageType.ACCESS_REQUEST;
    }

    @Override
    protected void doDecode()
        throws BufferUnderrunException
    {
        readByte();
        applicationId = readUTF();
        secret = readUTF();
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
}
