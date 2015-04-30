package io.github.lkowalczyk.slf4fx;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Netty-based server for receiving log messages generated by SLF4Fx's client.
 * <p/>
 * See the original SLF4Fx suite at https://code.google.com/p/slf4fx/.
 * <p/>
 * This is designed to be a drop-in replacement for the original SLF4FxServer
 * although with a few twists you should be aware of.
 * <ul>
 * <li>The {@link #setDefaultLocalAddress} method does not accept null argument
 * (throws NullPointerException).
 * <p/>
 * <li>The {@link #setCredentials(Map)} method does not accept null argument and
 * it will also reject a map which contains a null key or a null value.
 * <p/>
 * <li>{@link #stop()} throws InterruptedException.
 * <p/>
 * <li>{@link #setReaderBufferSize(int)} is unimplemented, calling it has no effect.
 * </ul>
 *
 * @author Łukasz Kowalczyk &lt;lkowalczyk@gmail.com&gt;
 */
public class SLF4FxServer {
    private final Logger log = LoggerFactory.getLogger(SLF4FxServer.class);

    private String flexPolicyResponse;
    private SocketAddress localAddress = new InetSocketAddress("localhost", 18888);
    private Map<String, String> credentials = new HashMap<>();
    private int sessionTimeout = 60;
    private int workersCount = 1;
    private String categoryPrefix = "slf4fx";

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture future;

    /**
     * This constructor works the same as calling:
     * <p/>
     * <pre>
     * SLF4FxServer(new InetSocketAddress("localhost", 18888), new NioServerSocketChannelFactory(),
     *              { "child.tcpNoDelay": true, "child.keepAlive", true }, {});
     * </pre>
     * <p/>
     * (maps in pseudocode).
     */

    public SLF4FxServer() {
    }

    public SLF4FxServer(SocketAddress localAddress, Map<String, Object> serverBootstrapOptions,
                        Map<String, String> credentials, int workersCount) {
        if (localAddress == null)
            throw new NullPointerException("localAddress");
        if (serverBootstrapOptions == null)
            throw new NullPointerException("serverBootstrapOptions");
        setCredentials(credentials);
    }

    public void setDefaultLocalAddress(final SocketAddress localAddress) {
        if (localAddress == null)
            throw new NullPointerException("localAddress");
        this.localAddress = localAddress;
    }

    public SocketAddress getDefaultLocalAddress() {
        return localAddress;
    }

    public void setWorkersCount(final int workersCount) {
        if (workersCount < 1)
            throw new IllegalArgumentException("workersCount must be larger than 0 (" + workersCount + ")");
        this.workersCount = workersCount;
    }

    public int getWorkersCount() {
        return workersCount;
    }

    /**
     * Sets accepted credentials consisting of applicationId-secret pairs
     * neither of which may be null. Default credentials are empty which means
     * all logging is accepted.
     *
     * @param credentials applicationId-secret pairs neither of which may be null.
     */
    public void setCredentials(Map<String, String> credentials) {
        if (credentials == null)
            throw new NullPointerException("credentials");
        for (Map.Entry<String, String> entry : credentials.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null)
                throw new IllegalArgumentException("Each credential must have a non-null applicationId and secret");
        }
        this.credentials = new HashMap<String, String>(credentials);
    }

    public Map<String, String> getCredentials() {
        return Collections.unmodifiableMap(credentials);
    }

    public void setSessionTimeout(final int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setFlexPolicyResponse(final File file)
            throws IOException {
        Reader reader = new FileReader(file);
        try {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[4096];
            int size;
            while ((size = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, size);
            }
            setFlexPolicyResponse(sb.toString());
        }
        finally {
            try {
                reader.close();
            }
            catch (IOException e) {
                // ignore
            }
        }
    }

    public void setFlexPolicyResponse(final String flexPolicyResponse) {
        this.flexPolicyResponse = flexPolicyResponse;
    }

    public String getFlexPolicyResponse() {
        return flexPolicyResponse;
    }

    public void setReaderBufferSize(final int readerBufferSize) {
    }

    public int getReaderBufferSize() {
        return 1024;
    }

    /**
     * Sets category prefix used for all Flash application logging. Default is
     * {@code slf4fx}. Do not include a dot at the end.
     * <p/>
     * If null, no prefix apart from the application name is used.
     *
     * @param prefix Logging category prefix without the following dot.
     */
    public void setCategoryPrefix(String prefix) {
        this.categoryPrefix = prefix;
    }

    public String getCategoryPrefix() {
        return categoryPrefix;
    }

    /**
     * Starts this SLF4FxServer. An instance cannot be started more than once.
     * Second and subsequent invocations of this method without an intervening
     * {@link #stop()} will have no effect.
     */
    public synchronized void start() {
        if (future != null) {
            log.warn("This server instance was already started before.");
            return;
        }

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        final Map<String, Object> parameters = new HashMap<>();
                        parameters.put("policy-file-response", flexPolicyResponse);
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new InboundMessageDecoder());
                        p.addLast(new OutboundMessageEncoder());

                        p.addLast(new MessageHandler(SLF4FxServer.this.categoryPrefix, SLF4FxServer.this.credentials,
                                Collections.unmodifiableMap(parameters)));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);

        log.trace("Binding to {}", localAddress);
        future = serverBootstrap.bind(localAddress);
        future.awaitUninterruptibly();
        assert future.isDone();
        if (future.isCancelled()) {
            log.error("Binding to {} cancelled", localAddress);
        }
        else if (!future.isSuccess()) {
            log.error("Binding to {} failed", localAddress);
        }
        else {
            log.trace("Bound to {}", localAddress);
        }
    }

    /**
     * Stops the SLF4Fx server. Calling this method before {@link #start()} has
     * no effect, as well as calling it again.
     */
    public synchronized void stop() throws InterruptedException {
        if (future != null) {
            log.trace("Closing");
            future.channel().close();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            future.channel().closeFuture().sync();
            log.trace("Closed");
            future = null;
        }
    }
}
