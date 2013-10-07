package pl.bluetrain.slf4fx;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.bluetrain.slf4fx.message.AccessRequest;
import pl.bluetrain.slf4fx.message.BufferUnderrunException;
import pl.bluetrain.slf4fx.message.InboundMessage;
import pl.bluetrain.slf4fx.message.LogRecord;
import pl.bluetrain.slf4fx.message.MalformedMessageException;
import pl.bluetrain.slf4fx.message.PolicyFileRequest;

/**
 * Decodes byte sequences to high-level messages.
 * 
 * @author Łukasz Kowalczyk <lukasz@bluetrain.pl>
 */
class MessageFrameDecoder extends FrameDecoder
{
    private final Logger log = LoggerFactory.getLogger(MessageFrameDecoder.class);
    
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer)
        throws Exception
    {
        try
        {
            InboundMessage message;
            if ((message = new LogRecord().tryDecode(buffer)) != null)
            {
                return message;
            }
            else if ((message = new AccessRequest().tryDecode(buffer)) != null)
            {
                return message;
            }
            else if ((message = new PolicyFileRequest().tryDecode(buffer)) != null)
            {
                return message;
            }
            log.warn("Closing connection after unknown message: {} and {} following bytes",
                    String.format("0x%x", buffer.getByte(0)), buffer.readableBytes() - 1);
            Channels.close(ctx, channel.getCloseFuture());
            return null;
        }
        catch (BufferUnderrunException e)
        {
            return null;
        }
        catch (MalformedMessageException e)
        {
            log.warn("Closing connection after unknown message: {}", e.toString());
            Channels.close(ctx, channel.getCloseFuture());
            return null;
        }
    }
}
