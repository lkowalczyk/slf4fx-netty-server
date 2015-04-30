package io.github.lkowalczyk.slf4fx.message;

import java.io.UnsupportedEncodingException;

/**
 * Preset response to {@code &lt;policy-file-request/>}.
 * 
 * @author Łukasz Kowalczyk &lt;lkowalczyk@gmail.com&gt;
 */
public class PolicyFileResponse extends OutboundMessage
{
    private final String responseXml;
    
    public PolicyFileResponse(String responseXml)
    {
        this.responseXml = responseXml;
    }

    @Override
    protected void doEncode()
    {
        if (responseXml != null)
        {
            try
            {
                writeBytes(responseXml.getBytes("UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                // UTF-8 is guaranteed to by available
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        writeByte(0);
    }
    
}
