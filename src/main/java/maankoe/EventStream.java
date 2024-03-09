package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class EventStream<O>
        extends BaseEventStream<O>
        implements EventStreamListener<O> {

    private final static Logger LOGGER = LoggerFactory.getLogger(EventStream.class);

    public EventStream(EventLoop loop) {
        super(loop);
    }

    public void addInput(O item) {
        LOGGER.info("SUBMIT {}", item);
        Event<O> event = loop.submit(() -> item);
        event.onComplete(x -> listener.addInput(x));
    }
}
