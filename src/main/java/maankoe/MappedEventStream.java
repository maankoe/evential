package maankoe;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class MappedEventStream<I, O>
        extends BaseEventStream<O>
        implements EventStreamListener<I> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConsumedEventStream.class);

    private final Function<I, O> mapper;

    public MappedEventStream(
            EventLoop loop,
            Function<I, O> mapper
    ) {
        super(loop);
        this.mapper = mapper;
    }

    public void addInput(I item) {
        LOGGER.info("MAP {}", item);
        Event<O> event = loop.submit(() -> mapper.apply(item));
        event.onComplete(x -> listener.addInput(x));
    }
}
