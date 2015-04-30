package io.github.lkowalczyk.slf4fx.message;

public enum MessageType
{
    UNKNOWN(0),
    ACCESS_REQUEST(1),
    ACCESS_RESPONSE(2),
    LOG_RECORD(3),
    POLICY_FILE_REQUEST('<');
    
    private final int tag;

    private MessageType(int tag)
    {
        this.tag = tag;
    }

    public int getTag()
    {
        return tag;
    }
}
