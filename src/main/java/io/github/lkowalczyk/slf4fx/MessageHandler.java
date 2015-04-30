package io.github.lkowalczyk.slf4fx;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.lkowalczyk.slf4fx.message.AccessRequest;
import io.github.lkowalczyk.slf4fx.message.AccessResponse;
import io.github.lkowalczyk.slf4fx.message.LogRecord;
import io.github.lkowalczyk.slf4fx.message.OutboundMessage;
import io.github.lkowalczyk.slf4fx.message.PolicyFileRequest;
import io.github.lkowalczyk.slf4fx.message.PolicyFileResponse;

import java.util.Map;

/**
 * Handles incoming messages. An instance is NOT shareable among pipelines.
 * 
 * @author Łukasz Kowalczyk &lt;lkowalczyk@gmail.com&gt;
 */
class MessageHandler extends ChannelInboundHandlerAdapter
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.trace("Handling {}", msg);

        if (msg instanceof LogRecord)
        {
            LogRecord message = (LogRecord) msg;
            handleLogRecord(message);
            return;
        }

        OutboundMessage response;

        if (msg instanceof AccessRequest)
        {
            AccessRequest message = (AccessRequest) msg;
            response = handleAccessRequest(message);
        }
        else if (msg instanceof PolicyFileRequest)
        {
            PolicyFileRequest message = (PolicyFileRequest) msg;
            response = handlePolicyFileRequest(message, policyFileResponse);
        }
        else {
            throw new IllegalArgumentException("Nieznany komunikat: " + msg);
        }

        log.trace("Response for {}: {}", msg, response);
        ctx.writeAndFlush(response);
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
        log.debug("Logging {} to {}", message.getMessage(), category);
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
