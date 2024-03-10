package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Predicate;

public class FilteredEventStream<O>
        extends BaseEventStream<O>
        implements EventStreamListener<O> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConsumedEventStream.class);

    private final Predicate<O> predicate;

    public FilteredEventStream(
            EventLoop loop,
            Predicate<O> predicate
    ) {
        super(loop);
        this.predicate = predicate;
    }

    @Override
    public void addInput(O item) {
        LOGGER.info("FILTER {}", item);
        Event<Optional<O>> event = loop.submit(
                () -> predicate.test(item) ? Optional.of(item) : Optional.empty()
        );
        event.onComplete(x -> x.ifPresent(listener::addInput));
    }
}
