package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseEventStream<O> {
    private final static Logger LOGGER = LoggerFactory.getLogger(EventStream.class);

    protected EventStreamListener<O> listener = new EventStreamListener.Dummy<>();
    protected EventLoop loop;

    public BaseEventStream(
            EventLoop loop
    ) {
         this.loop = loop;
    }

    public <OO> BaseEventStream<OO> map(Function<O, OO> mapper) {
        MappedEventStream<O, OO> ret = new MappedEventStream<>(this.loop, mapper);
        this.listener = ret;
        return ret;
    }

    public <OO> BaseEventStream<OO> flatMap(Function<O, Iterable<OO>> mapper) {
        FlatMappedEventStream<O, OO> ret = new FlatMappedEventStream<>(this.loop, mapper);
        this.listener = ret;
        return ret;
    }

    public BaseEventStream<O> consume(Consumer<O> consumer) {
        ConsumedEventStream<O> ret = new ConsumedEventStream<>(this.loop, consumer);
        this.listener = ret;
        return ret;
    }
}
