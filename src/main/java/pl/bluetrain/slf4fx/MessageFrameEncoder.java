package pl.bluetrain.slf4fx;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import pl.bluetrain.slf4fx.message.AccessResponse;

/**
 * Encodes high-level messages as byte sequences.
 * 
 * @author lkowalczyk
 */
public class MessageFrameEncoder extends OneToOneEncoder
{
    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg)
        throws Exception
    {
        if (msg instanceof AccessResponse)
        {
            AccessResponse response = (AccessResponse) msg;
            return response.asChannelBuffer();
        }
        return msg;
    }
}
