package maankoe;

import maankoe.loop.EventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class Utilities {
    private final static Logger LOGGER = LoggerFactory.getLogger(TestEventLoop.class);

    public static <T> Consumer<T> sleepConsumer(int millis) {
        return x -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static void waitForCompletion(EventLoop loop) {
        while (loop.hasEvents()) {
//            LOGGER.info("{}", loop.events);
        }
    }
}
