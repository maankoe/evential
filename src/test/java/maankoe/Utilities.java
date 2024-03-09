package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utilities {
    private final static Logger LOGGER = LoggerFactory.getLogger(TestEventLoop.class);

    public static void waitForCompletion(EventLoop loop) {
        while (loop.hasEvents()) {
//            LOGGER.info("{}", loop.events);
        }
    }
}
