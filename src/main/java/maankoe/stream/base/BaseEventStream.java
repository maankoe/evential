package maankoe.stream.base;

import maankoe.function.DistinctFilter;
import maankoe.function.ErrorFunction;
import maankoe.function.EventFunction;
import maankoe.loop.EventLoop;
import maankoe.stream.reduce.Accumulation;
import maankoe.stream.reduce.Reduction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.*;


public abstract class BaseEventStream<O> {
    private final static Logger LOGGER = LoggerFactory.getLogger(EventStream.class);

    protected EventStreamListener<O> listener = new EventStreamListener.Dummy<>();
    protected EventLoop loop;

    public BaseEventStream(EventLoop loop) {
         this.loop = loop;
    }

    public <OO> BaseEventStream<OO> map(Function<O, OO> mapper) {
        GeneralEventStream<O, OO> ret = GeneralEventStream.create(
                this.loop,
                new EventFunction.Mapper<>(mapper),
                "MAP"
        );
        this.listener = ret;
        return ret;
    }

    public <OO> BaseEventStream<OO> flatMap(Function<O, Iterable<OO>> mapper) {
        GeneralEventStream<O, OO> ret = GeneralEventStream.createMulti(
                this.loop,
                new EventFunction.Mapper<>(mapper),
                "FLATMAP"
        );
        this.listener = ret;
        return ret;
    }

    public BaseEventStream<O> filter(Predicate<O> predicate) {
        GeneralEventStream<O, O> ret = GeneralEventStream.create(
                this.loop,
                new EventFunction.Filter<>(predicate),
                "FILTER"
        );
        this.listener = ret;
        return ret;
    }

    public BaseEventStream<O> consume(Consumer<O> consumer) {
        GeneralEventStream<O, O> ret = GeneralEventStream.create(
                this.loop,
                new EventFunction.Consumer<>(consumer),
                "CONSUME"
        );
        this.listener = ret;
        return ret;
    }

    public BaseEventStream<O> consumeError(Consumer<Throwable> consumer) {
        GeneralEventStream<O, O> ret = GeneralEventStream.create(
                this.loop,
                new ErrorFunction.ErrorConsumerBlackhole<>(consumer),
                "CONSUME_ERROR"
        );
        this.listener = ret;
        return ret;
    }

    public BaseEventStream<O> mapError(Function<Throwable, O> mapping) {
        GeneralEventStream<O, O> ret = GeneralEventStream.create(
                this.loop,
                new ErrorFunction.ErrorMapper<>(mapping),
                "MAP_ERROR"
        );
        this.listener = ret;
        return ret;
    }

    public <K> BaseEventStream<O> distinct(Function<O, K> key) {
        GeneralEventStream<O, O> ret = GeneralEventStream.create(
                this.loop,
                new DistinctFilter<>(key),
                "DISTINCT"
        );
        this.listener = ret;
        return ret;
    }

    public BaseEventStream<Iterable<O>> window(int windowSize) {
        WindowedEventStream<O> ret = WindowedEventStream.create(
                this.loop,
                windowSize,
                "WINDOWED"
        );
        this.listener = ret;
        return ret;
    }

    public <OO> Accumulation<O, OO> reduce(BiFunction<O, OO, OO> reduction, Supplier<OO> identity) {
        Accumulation<O, OO> ret = Accumulation.create(
                this.loop, reduction, identity, "REDUCE"
        );
        this.listener = ret;
        return ret;
    }

    public Reduction<O> reduce(BinaryOperator<O> reduce) {
        Reduction<O> ret = Reduction.create(
                this.loop, reduce, "REDUCE"
        );
        this.listener = ret;
        return ret;
    }
}
