package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

public class ConsumedEventStream<T>
        extends BaseEventStream<T>
        implements EventStreamListener<T> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConsumedEventStream.class);

    private final Consumer<T> consumer;

    public ConsumedEventStream(
            EventLoop loop,
            Consumer<T> consumer
    ) {
        super(loop);
        this.consumer = consumer;
    }

    public void addInput(T item) {
        LOGGER.info("CONSUME {}", item);
        Event<T> event = loop.submit(() -> {
            consumer.accept(item);
            return item;
        });
        event.onComplete(x -> listener.addInput(x));
    }
}
