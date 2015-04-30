package io.github.lkowalczyk.slf4fx;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.lkowalczyk.slf4fx.message.AccessRequest;
import io.github.lkowalczyk.slf4fx.message.BufferUnderrunException;
import io.github.lkowalczyk.slf4fx.message.InboundMessage;
import io.github.lkowalczyk.slf4fx.message.LogRecord;
import io.github.lkowalczyk.slf4fx.message.PolicyFileRequest;

import java.util.List;

/**
 * Decodes byte sequences to high-level messages.
 * 
 * @author Łukasz Kowalczyk &lt;lkowalczyk@gmail.com&gt;
 */
class InboundMessageDecoder extends ByteToMessageDecoder
{
    private final Logger log = LoggerFactory.getLogger(InboundMessageDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!in.isReadable()) {
            return;
        }
        try
        {
            InboundMessage message;
            if ((message = new LogRecord().tryDecode(in)) != null)
            {
                out.add(message);
            }
            else if ((message = new AccessRequest().tryDecode(in)) != null)
            {
                out.add(message);
            }
            else if ((message = new PolicyFileRequest().tryDecode(in)) != null)
            {
                out.add(message);
            }
            else {
                throw new IllegalArgumentException("Unknown message type: " + in.getByte(0));
            }

            log.debug("Decoded {}", message);
        }
        catch (BufferUnderrunException e)
        {
            log.debug(e.getMessage(), e);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            ctx.disconnect();
        }
    }
}
