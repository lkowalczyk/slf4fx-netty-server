package pl.bluetrain.slf4fx;

import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.bluetrain.slf4fx.message.AccessRequest;
import pl.bluetrain.slf4fx.message.AccessResponse;
import pl.bluetrain.slf4fx.message.LogRecord;
import pl.bluetrain.slf4fx.message.OutboundMessage;
import pl.bluetrain.slf4fx.message.PolicyFileRequest;
import pl.bluetrain.slf4fx.message.PolicyFileResponse;

/**
 * Handles incoming messages. An instance is NOT shareable among pipelines.
 * 
 * @author Łukasz Kowalczyk <lukasz@bluetrain.pl>
 */
class MessageHandler extends SimpleChannelHandler
{
    private final Logger log = LoggerFactory.getLogger(MessageHandler.class);
    private final String categoryPrefix;
    private final Map<String, String> credentials;
    private String applicationId;
    private final String policyFileResponse;
    
    public MessageHandler(String categoryPrefix, Map<String, String> credentials, Map<String, Object> parameters)
    {
        this.categoryPrefix = categoryPrefix;
        this.credentials = credentials;
        this.policyFileResponse = (String) parameters.get("policy-file-response");
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent ev)
        throws Exception
    {
        if (ev.getMessage() instanceof LogRecord)
        {
            LogRecord message = (LogRecord) ev.getMessage();
            handleLogRecord(message);
        }

        if (ev.getMessage() instanceof AccessRequest)
        {
            AccessRequest message = (AccessRequest) ev.getMessage();
            OutboundMessage response = handleAccessRequest(message);
            Channels.write(ev.getChannel(), response);
        }
        
        if (ev.getMessage() instanceof PolicyFileRequest)
        {
            PolicyFileRequest message = (PolicyFileRequest) ev.getMessage();
            OutboundMessage response = handlePolicyFileRequest(message, policyFileResponse);
            Channels.write(ev.getChannel(), response);
        }
    }
    
    private AccessResponse handleAccessRequest(AccessRequest message)
    {
        
        boolean grant = credentials.isEmpty()
                || (credentials.containsKey(message.getApplicationId()) && credentials.get(
                        message.getApplicationId()).equals(message.getSecret()));
        
        if (grant)
        {
            this.applicationId = message.getApplicationId();
        }
        else
        {
            log.info("Access request ({}:{}): rejected", message.getApplicationId(), message.getSecret());
        }
        return new AccessResponse(grant);
    }
    
    private PolicyFileResponse handlePolicyFileRequest(PolicyFileRequest message, String policyFileResponse)
    {
        PolicyFileResponse response = new PolicyFileResponse(policyFileResponse);
        return response;
    }
    
    private void handleLogRecord(LogRecord message)
    {
        if (applicationId == null)
        {
            return;
        }
        StringBuilder category = new StringBuilder();
        if (categoryPrefix != null)
            category.append(categoryPrefix);
        category.append('.');
        category.append(applicationId).append('.').append(message.getCategory());
        Logger logger = LoggerFactory.getLogger(category.toString());
        switch (message.getLevel())
        {
            case DEBUG:
                logger.debug(message.getMessage());
                break;
            
            case WARN:
                logger.warn(message.getMessage());
                break;
            
            case ERROR:
                logger.error(message.getMessage());
                break;
            
            case INFO:
            default:
                logger.info(message.getMessage());
                break;
        }
    }
}
