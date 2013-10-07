package pl.bluetrain.slf4fx;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.bluetrain.slf4fx.message.InboundMessage;
import pl.bluetrain.slf4fx.message.UnknownMessageException;

/**
 * Decodes byte sequences to high-level messages.
 * 
 * @author lkowalczyk
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
            return InboundMessage.decode(buffer);
        }
        catch (UnknownMessageException e)
        {
            // After event one unexpected bytes we cannot trust any more data
            // coming from this source, thus breaking the connection.
            log.warn("Closing connection after unknown message: {}", e.toString());
            Channels.close(ctx, channel.getCloseFuture());
            return null;
        }
    }
}
