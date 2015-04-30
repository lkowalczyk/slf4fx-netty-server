package io.github.lkowalczyk.slf4fx.message;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class PolicyFileRequest extends InboundMessage
{
    private static final byte[] REQUEST;
    static
    {
        try
        {
            REQUEST = "<policy-file-request/>\0".getBytes("US-ASCII");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new Error(e.getMessage(), e);
        }
    }
    
    @Override
    protected MessageType getType()
    {
        return MessageType.POLICY_FILE_REQUEST;
    }

    @Override
    protected void doDecode()
        throws BufferUnderrunException, MalformedMessageException
    {
        byte[] array = readBytes(REQUEST.length);
        if (!Arrays.equals(array, REQUEST))
            throw new MalformedMessageException("Expected <policy-file-request/>\\0");
    }
}
