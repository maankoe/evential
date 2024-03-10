package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class FlatMappedEventStream<I, O>
        extends BaseEventStream<O>
        implements EventStreamListener<I> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConsumedEventStream.class);

    private final Function<I, Iterable<O>> mapper;

    public FlatMappedEventStream(
            EventLoop loop,
            Function<I, Iterable<O>> mapper
    ) {
        super(loop);
        this.mapper = mapper;
    }

    public void addInput(I item) {
        LOGGER.info("FLATMAP {}", item);
        for (O output : mapper.apply(item)) {
            Event<O> event = loop.submit(() -> output);
            event.onComplete(x -> listener.addInput(x));
        }
    }
}
