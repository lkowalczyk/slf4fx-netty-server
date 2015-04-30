package io.github.lkowalczyk.slf4fx;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.lkowalczyk.slf4fx.message.OutboundMessage;

/**
 * Encodes high-level messages as byte sequences.
 * 
 * @author Łukasz Kowalczyk &lt;lkowalczyk@gmail.com&gt;
 */
public class OutboundMessageEncoder extends MessageToByteEncoder<OutboundMessage> {
    private final Logger log = LoggerFactory.getLogger(MessageHandler.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, OutboundMessage msg, ByteBuf out) throws Exception {
        log.trace("Encoding {}", msg);
        out.writeBytes(msg.getBytes());
    }
}
