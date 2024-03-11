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

    public void submit(O item) {
        LOGGER.info("SUBMIT {}", item);
        Event<O> event = this.loop.submit(() -> item);
        // this guy needs proper submit strategy or something (needs to tell lsitener to expect)
        event.onComplete(x -> this.listener.submit(x));
    }

    public void block() {
        this.listener.block();
    }
}
