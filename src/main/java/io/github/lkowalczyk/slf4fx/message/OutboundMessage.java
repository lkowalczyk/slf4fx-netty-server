package io.github.lkowalczyk.slf4fx.message;

import java.io.ByteArrayOutputStream;

/**
 * Base class for all outbound messages. Provides encoding capabilities.
 * 
 * @author Łukasz Kowalczyk &lt;lkowalczyk@gmail.com&gt;
 */
public abstract class OutboundMessage {
    private ByteArrayOutputStream os = new ByteArrayOutputStream();

    public byte[] getBytes() {
        if (os.size() == 0) {
            doEncode();
        }
        return os.toByteArray();
    }

    /**
     * When called, this method should in turn output encoded data
     * using the {@code write*} methods in this class.
     */
    protected abstract void doEncode();
    
    protected final void writeByte(int value) {
        os.write(value);
    }
    
    protected final void writeBytes(byte[] bytes)
    {
        os.write(bytes, 0, bytes.length);
    }
}
