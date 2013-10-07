package pl.bluetrain.slf4fx.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.bluetrain.slf4fx.MessageType;

public class LogRecord extends InboundMessage
{
    private static final Logger log = LoggerFactory.getLogger(LogRecord.class);
    
    static Decoder decoder()
    {
        return new Decoder(MessageType.LOG_RECORD)
        {
            @Override
            InboundMessage doDecode()
                throws BufferUnderflowException
            {
                return new LogRecord(readUTF(), readLevel(), readUTF());
            }
            
            private Level readLevel()
                throws BufferUnderflowException
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
                    throw new BufferUnderflowException(e);
                }
            }
            
        };
    }

    public enum Level
    {
        ERROR, WARN, INFO, DEBUG
    }
    
    private final String category;
    private final Level level;
    private final String message;
    
    private LogRecord(String category, Level level, String message)
    {
        this.category = category;
        this.level = level;
        this.message = message;
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
        return "LogRecord [log=" + log + ", category=" + category + ", level=" + level + ", message=" + message + "]";
    }
}
