package pl.bluetrain.slf4fx.message;


/**
 * Informs about accepting or rejecting the access request.
 * 
 * @author lkowalczyk
 */
public class AccessResponse extends OutboundMessage
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
    
    @Override
    protected void doEncode()
    {
        writeByte(MessageType.ACCESS_RESPONSE.getTag());
        writeByte(accessGranted ? 1 : 0);
    }
}
