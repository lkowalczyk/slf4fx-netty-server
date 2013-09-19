package io.github.krankenwagen.slf4fx.message;

import io.github.krankenwagen.slf4fx.BufferUnderflowException;
import io.github.krankenwagen.slf4fx.MessageType;

import java.lang.invoke.MethodHandles;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogRecord extends InboundMessage
{
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
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
                    if (tag != MessageType.LOG_RECORD.getTag())
                    {
                        buffer.resetReaderIndex();
                        return null;
                    }
                    
                    String category = readUTF(buffer);
                    Level level = readLevel(buffer);
                    String message = readUTF(buffer);
                    return new LogRecord(category, level, message);
                }
                catch (IndexOutOfBoundsException e)
                {
                    buffer.resetReaderIndex();
                    throw new BufferUnderflowException(e);
                }
            }
            
            private Level readLevel(ChannelBuffer buffer)
                throws BufferUnderflowException
            {
                try
                {
                    int levelValue = buffer.readInt();
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
                            log.warn("Unknown logging level: {}", levelValue);
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
}
