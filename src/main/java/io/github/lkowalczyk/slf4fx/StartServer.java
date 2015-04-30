package io.github.lkowalczyk.slf4fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Test.
 *
 * @author Łukasz Kowalczyk &lt;lkowalczyk@gmail.com&gt;
 */
public class StartServer {
    private static final Logger log = LoggerFactory.getLogger(SLF4FxServer.class);

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().getLogger("").setLevel(Level.ALL);
        for (Handler handler : LogManager.getLogManager().getLogger("").getHandlers()) {
            handler.setLevel(Level.ALL);
        }

        SLF4FxServer server = new SLF4FxServer();
        log.debug("Start");
        System.out.println("Start server");
        server.start();
        final long timeout = System.currentTimeMillis() + 10 * 1000L;
        synchronized (StartServer.class) {
            while (System.currentTimeMillis() < timeout) {
                StartServer.class.wait(timeout - System.currentTimeMillis());
            }
        }
        log.debug("Stop");
        server.stop();
    }
}
