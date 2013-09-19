package io.github.krankenwagen.slf4fx;

public enum MessageType
{
    UNKNOWN(0),
    ACCESS_REQUEST(1),
    ACCESS_RESPONSE(2),
    LOG_RECORD(3);
    
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
