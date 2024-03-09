package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

public class EventStream<T> {
    private final static Logger LOGGER = LoggerFactory.getLogger(EventStream.class);

    private EventStream<T> listener;
    protected final EventLoop loop;

    public EventStream(EventLoop loop) {
        this.loop = loop;
        this.listener = null;
    }

    public void addInput(T item) {
        LOGGER.info("SUBMIT {}", item);
        Event<T> event = loop.submit(() -> item);
        event.onComplete(x -> listener.addInput(x));
    }

//    public <O> EventStream<T> map(Function<T, O> mapper) {
//        this.listener = new MappedEventStream<>(this, mapper);
//        return this.listener;
//    }

    public EventStream<T> consume(Consumer<T> consumer) {
        this.listener = new ConsumedEventStream<>(loop, this, consumer);
        return this.listener;
    }
}
