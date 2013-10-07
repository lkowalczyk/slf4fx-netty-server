package pl.bluetrain.slf4fx.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogRecord extends InboundMessage
{
    private static final Logger log = LoggerFactory.getLogger(LogRecord.class);
    
    public enum Level
    {
        ERROR, WARN, INFO, DEBUG
    }
    
    private String category;
    private Level level;
    private String message;
    
    @Override
    protected MessageType getType()
    {
        return MessageType.LOG_RECORD;
    }
    
    @Override
    protected void doDecode()
        throws BufferUnderrunException
    {
        readByte();
        category = readUTF();
        level = readLevel();
        message = readUTF();
    }
    
    private Level readLevel()
        throws BufferUnderrunException
    {
        try
        {
            int levelValue = readInt();
            switch (levelValue)
            {
                case 0:
                    return Level.ERROR;
                case 1:
                    return Level.WARN;
                case 2:
                    return Level.INFO;
                case 3:
                    return Level.DEBUG;
                default:
                    log.debug("Unknown logging level: {}", levelValue);
                    return Level.INFO;
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new BufferUnderrunException();
        }
    }
    
    public String getCategory()
    {
        return category;
    }
    
    public Level getLevel()
    {
        return level;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((level == null) ? 0 : level.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LogRecord other = (LogRecord) obj;
        if (category == null)
        {
            if (other.category != null)
                return false;
        }
        else if (!category.equals(other.category))
            return false;
        if (level != other.level)
            return false;
        if (message == null)
        {
            if (other.message != null)
                return false;
        }
        else if (!message.equals(other.message))
            return false;
        return true;
    }
    
    @Override
    public String toString()
    {
        return "LogRecord [category=" + category + ", level=" + level + ", message=" + message + "]";
    }
}
