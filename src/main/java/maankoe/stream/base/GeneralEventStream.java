package maankoe.stream.base;

import maankoe.function.ErrorFunction;
import maankoe.function.EventFunction;
import maankoe.loop.EventLoop;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.stream.submit.*;
import maankoe.utilities.IndexGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneralEventStream<I, O>
        extends BaseEventStream<O>
        implements EventStreamListener<I>  {

    private final static Logger LOGGER = LoggerFactory.getLogger(GeneralEventStream.class);

    private final IndexGenerator indexGenerator;
    private final ListenerBlockingStrategy listenerBlockingStrategy;
    private final EventBlockingStrategy eventBlockingStrategy;
    private final EventSubmitStrategy<I, O> eventSubmitStrategy;
    private final ErrorSubmitStrategy<O> errorSubmitStrategy;
    private final CloseStrategy closeStrategy;
    private final String name;

    public GeneralEventStream(
            EventLoop loop,
            IndexGenerator indexGenerator,
            ListenerBlockingStrategy listenerBlockingStrategy,
            EventBlockingStrategy eventBlockingStrategy,
            EventSubmitStrategy<I, O> eventSubmitStrategy,
            ErrorSubmitStrategy<O> errorSubmitStrategy,
            CloseStrategy closeStrategy,
            String name
    ) {
        super(loop);
        this.indexGenerator = indexGenerator;
        this.listenerBlockingStrategy = listenerBlockingStrategy;
        this.eventBlockingStrategy = eventBlockingStrategy;
        this.eventSubmitStrategy = eventSubmitStrategy;
        this.errorSubmitStrategy = errorSubmitStrategy;
        this.closeStrategy = closeStrategy;
        this.name = name;
    }

    public static <I, O> GeneralEventStream<I, O> create(
            EventLoop loop,
            EventFunction<I, O> function,
            String name
    ) {
        IndexGenerator indexGenerator = new IndexGenerator();
        ListenerBlockingStrategy listenerBlockingStrategy = new ListenerBlockingStrategy(name);
        EventBlockingStrategy eventBlockingStrategy = new EventBlockingStrategy(name);
        return new GeneralEventStream<>(
                loop,
                indexGenerator,
                listenerBlockingStrategy,
                eventBlockingStrategy,
                new SingleEventSubmitStrategy<>(
                        loop, function, indexGenerator, eventBlockingStrategy
                ),
                new SingleErrorIdentitySubmitStrategy<>(indexGenerator),
                new SimpleCloseStrategy(
                        indexGenerator, listenerBlockingStrategy, eventBlockingStrategy
                ),
                name
        );
    }

    public static <O> GeneralEventStream<O, O> create(
            EventLoop loop,
            ErrorFunction<O> errorFunction,
            String name
    ) {
        IndexGenerator indexGenerator = new IndexGenerator();
        ListenerBlockingStrategy listenerBlockingStrategy = new ListenerBlockingStrategy(name);
        EventBlockingStrategy eventBlockingStrategy = new EventBlockingStrategy(name);
        return new GeneralEventStream<>(
                loop,
                indexGenerator,
                listenerBlockingStrategy,
                eventBlockingStrategy,
                new SingleIdentitySubmitStrategy<>(indexGenerator),
                new SingleErrorSubmitStrategy<>(
                        loop, errorFunction, indexGenerator, eventBlockingStrategy
                ),
                new SimpleCloseStrategy(
                        indexGenerator, listenerBlockingStrategy, eventBlockingStrategy
                ),
                name
        );
    }

    public static <I, O> GeneralEventStream<I, O> createMulti(
            EventLoop loop,
            EventFunction<I, Iterable<O>> function,
            String name
    ) {
        IndexGenerator indexGenerator = new IndexGenerator();
        ListenerBlockingStrategy listenerBlockingStrategy = new ListenerBlockingStrategy(name);
        EventBlockingStrategy eventBlockingStrategy = new EventBlockingStrategy(name);
        return new GeneralEventStream<>(
                loop,
                indexGenerator,
                listenerBlockingStrategy,
                eventBlockingStrategy,
                new MultipleEventSubmitStrategy<>(
                        loop, function, indexGenerator, eventBlockingStrategy
                ),
                new SingleErrorIdentitySubmitStrategy<>(indexGenerator),
                new SimpleCloseStrategy(
                        indexGenerator, listenerBlockingStrategy, eventBlockingStrategy
                ),
                name
        );
    }

    @Override
    public void expect(long index) {
        LOGGER.info("{} Expect: {}", this.name, index);
        this.listenerBlockingStrategy.expect(index);
    }

    @Override
    public void submit(I item) {
        LOGGER.info("{} Submit: {}", this.name, item);
        this.eventSubmitStrategy.submit(item, listener);
    }

    @Override
    public void submitError(Throwable error) {
        LOGGER.info("{} Error: {}", this.name, error.getMessage());
        this.errorSubmitStrategy.submit(error, this.listener);
    }

    @Override
    public void accept(long index) {
        LOGGER.info("{} Accept: {}", this.name, index);
        this.listenerBlockingStrategy.accept(index);
    }

    @Override
    public void close(long index) {
        LOGGER.info("{} Close: {}", this.name, index);
        this.closeStrategy.close(index, this.listener);
    }
}
