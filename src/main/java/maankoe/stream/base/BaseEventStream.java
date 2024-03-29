package maankoe.stream.base;

import maankoe.function.DistinctFilter;
import maankoe.function.ErrorFunction;
import maankoe.function.EventFunction;
import maankoe.loop.EventLoop;
import maankoe.stream.*;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.stream.reduce.WindowedEventStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


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

    public BaseEventStream<O> filter(Predicate<O> predicate) {
        FilteredEventStream<O> ret = new FilteredEventStream<>(this.loop, predicate);
        this.listener = ret;
        return ret;
    }

    public BaseEventStream<O> consume(Consumer<O> consumer) {
        ConsumedEventStream<O> ret = new ConsumedEventStream<>(this.loop, consumer);
        this.listener = ret;
        return ret;
    }

    public BaseEventStream<O> consumeError(Consumer<Throwable> consumer) {
        ErrorEventStream<O> ret = new ErrorEventStream<>(
                this.loop,
                new ErrorFunction.ErrorConsumerBlackhole<>(consumer),
                "ERROR_CONSUME"
        );
        this.listener = ret;
        return ret;
    }

    public BaseEventStream<O> mapError(Function<Throwable, O> mapping) {
        ErrorEventStream<O> ret = new ErrorEventStream<>(
                this.loop,
                new ErrorFunction.ErrorMapper<>(mapping),
                "ERROR_MAP"
        );
        this.listener = ret;
        return ret;
    }

    public <K> BaseEventStream<O> distinct(Function<O, K> key) {
        DistinctEventStream<O, K> ret = new DistinctEventStream<O, K>(
                this.loop, new DistinctFilter<>(key)
        );
        this.listener = ret;
        return ret;
    }

//    public <I> Reduction<I, O> reduce(BiFunction<I, O, O> reduction, O identity) {
//        Reduction<I, O> ret = new Reduction<>(
//                this.loop, reduction, identity
//        );
//        this.listener = ret;
//        return ret;
//    }

    public BaseEventStream<Iterable<O>> window(int windowSize) {
        WindowedEventStream<O> ret = new WindowedEventStream<O>(
                this.loop,
                windowSize,
                new ErrorFunction.Identity<>(),
                new ListenerBlockingStrategy("WINDOW"),
                new EventBlockingStrategy("WINDOW")
        );
        this.listener = ret;
        return ret;
    }
}
