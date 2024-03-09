package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class ConsumedEventStream<T> extends EventStream<T> {
    private final static Logger LOGGER = LoggerFactory.getLogger(ConsumedEventStream.class);

    private final EventStream<T> stream;
    private final Consumer<T> consumer;

    private EventStream<T> listener;

    public ConsumedEventStream(
            EventLoop loop,
            EventStream<T> stream,
            Consumer<T> consumer
    ) {
        super(loop);
        this.stream = stream;
        this.consumer = consumer;
    }

    public void addInput(T item) {
        LOGGER.info("CONSUME {}", item);
        Event<T> event = loop.submit(() -> {
            consumer.accept(item);
            return item;
        });
//        event.onComplete(x -> listener.addInput(event.result()));
    }
}
