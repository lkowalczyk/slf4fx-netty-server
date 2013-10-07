package pl.bluetrain.slf4fx;

import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.bluetrain.slf4fx.message.AccessRequest;
import pl.bluetrain.slf4fx.message.AccessResponse;
import pl.bluetrain.slf4fx.message.LogRecord;

class MessageHandler extends SimpleChannelHandler
{
    private final Logger log = LoggerFactory.getLogger(MessageHandler.class);
    private final String categoryPrefix;
    private final Map<String, String> credentials;
    private String applicationId;
    
    public MessageHandler(String categoryPrefix, Map<String, String> credentials)
    {
        this.categoryPrefix = categoryPrefix;
        this.credentials = credentials;
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
        throws Exception
    {
        if (e.getMessage() instanceof LogRecord)
        {
            if (applicationId == null)
            {
                log.debug("Message of type 3 (log record) not preceded by a message of type 1 (access request) or invalid credentials, ignoring message.");
                return;
            }
            LogRecord logRecord = (LogRecord) e.getMessage();
            StringBuilder category = new StringBuilder();
            if (categoryPrefix != null)
                category.append(categoryPrefix);
            category.append('.');
            category.append(applicationId).append('.').append(logRecord.getCategory());
            Logger logger = LoggerFactory.getLogger(category.toString());
            switch (logRecord.getLevel())
            {
                case DEBUG:
                    logger.debug(logRecord.getMessage());
                    break;
                
                case WARN:
                    logger.warn(logRecord.getMessage());
                    break;
                
                case ERROR:
                    logger.error(logRecord.getMessage());
                    break;
                
                case INFO:
                default:
                    logger.info(logRecord.getMessage());
                    break;
            }
        }

        if (e.getMessage() instanceof AccessRequest)
        {
            AccessRequest request = (AccessRequest) e.getMessage();
            
            boolean grant = credentials.isEmpty()
                    || (credentials.containsKey(request.getApplicationId()) && credentials.get(
                            request.getApplicationId()).equals(request.getSecret()));
            
            Channels.write(e.getChannel(), new AccessResponse(grant));
            if (grant)
            {
                log.debug("Access request ({}:{}): granted", request.getApplicationId(), request.getSecret());
                this.applicationId = request.getApplicationId();
            }
            else
            {
                log.info("Access request ({}:{}): rejected", request.getApplicationId(), request.getSecret());
            }
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
        throws Exception
    {
        // information only, we let Netty handle the exception by closing the connection
        log.error("exceptionCaught: " + e.getCause().toString(), e.getCause());
    }
}
