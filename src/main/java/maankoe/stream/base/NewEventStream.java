package maankoe.stream.base;

import maankoe.function.ErrorFunction;
import maankoe.function.EventFunction;
import maankoe.loop.EventLoop;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.stream.reduce.WindowedEventStream;
import maankoe.stream.submit.*;
import maankoe.utilities.IndexGenerator;

public class NewEventStream<I, O>
        extends BaseEventStream<O>
        implements EventStreamListener<I>  {

    private final IndexGenerator indexGenerator;
    private final ListenerBlockingStrategy listenerBlockingStrategy;
    private final EventBlockingStrategy eventBlockingStrategy;
    private final EventSubmitStrategy<I, O> eventSubmitStrategy;
    private final ErrorSubmitStrategy<O> errorSubmitStrategy;
    private final CloseStrategy closeStrategy;

    public NewEventStream(
            EventLoop loop,
            IndexGenerator indexGenerator,
            ListenerBlockingStrategy listenerBlockingStrategy,
            EventBlockingStrategy eventBlockingStrategy,
            EventSubmitStrategy<I, O> eventSubmitStrategy,
            ErrorSubmitStrategy<O> errorSubmitStrategy,
            CloseStrategy closeStrategy
    ) {
        super(loop);
        this.indexGenerator = indexGenerator;
        this.listenerBlockingStrategy = listenerBlockingStrategy;
        this.eventBlockingStrategy = eventBlockingStrategy;
        this.eventSubmitStrategy = eventSubmitStrategy;
        this.errorSubmitStrategy = errorSubmitStrategy;
        this.closeStrategy = closeStrategy;
    }

    public static <I, O> NewEventStream<I, O> create(
            EventLoop loop,
            EventFunction<I, O> function,
            String name
    ) {
        IndexGenerator indexGenerator = new IndexGenerator();
        ListenerBlockingStrategy listenerBlockingStrategy = new ListenerBlockingStrategy(name);
        EventBlockingStrategy eventBlockingStrategy = new EventBlockingStrategy(name);
        return new NewEventStream<>(
                loop,
                indexGenerator,
                listenerBlockingStrategy,
                eventBlockingStrategy,
                new SingleEventSubmitStrategy<>(
                        loop, function, indexGenerator, eventBlockingStrategy
                ),
                new SingleErrorSubmitStrategy<>(
                        loop, new ErrorFunction.Identity<>(), indexGenerator, eventBlockingStrategy
                ),
                new SimpleCloseStrategy(
                        indexGenerator, listenerBlockingStrategy, eventBlockingStrategy
                )
        );
    }

    public static <O> NewEventStream<O, O> create(
            EventLoop loop,
            ErrorFunction<O> errorFunction,
            String name
    ) {
        IndexGenerator indexGenerator = new IndexGenerator();
        ListenerBlockingStrategy listenerBlockingStrategy = new ListenerBlockingStrategy(name);
        EventBlockingStrategy eventBlockingStrategy = new EventBlockingStrategy(name);
        return new NewEventStream<>(
                loop,
                indexGenerator,
                listenerBlockingStrategy,
                eventBlockingStrategy,
                new SingleEventSubmitStrategy<>(
                        loop, new EventFunction.Identity<>(), indexGenerator, eventBlockingStrategy
                ),
                new SingleErrorSubmitStrategy<>(
                        loop, errorFunction, indexGenerator, eventBlockingStrategy
                ),
                new SimpleCloseStrategy(
                        indexGenerator, listenerBlockingStrategy, eventBlockingStrategy
                )
        );
    }

    public static <I, O> NewEventStream<I, O> createMulti(
            EventLoop loop,
            EventFunction<I, Iterable<O>> function,
            String name
    ) {
        IndexGenerator indexGenerator = new IndexGenerator();
        ListenerBlockingStrategy listenerBlockingStrategy = new ListenerBlockingStrategy(name);
        EventBlockingStrategy eventBlockingStrategy = new EventBlockingStrategy(name);
        return new NewEventStream<>(
                loop,
                indexGenerator,
                listenerBlockingStrategy,
                eventBlockingStrategy,
                new MultipleEventSubmitStrategy<>(
                        loop, function, indexGenerator, eventBlockingStrategy
                ),
                new MultipleErrorSubmitStrategy<>(
                        loop, new ErrorFunction.Identity<>(), indexGenerator, eventBlockingStrategy
                ),
                new SimpleCloseStrategy(
                        indexGenerator, listenerBlockingStrategy, eventBlockingStrategy
                )
        );
    }

    @Override
    public void expect(long index) {
        this.listenerBlockingStrategy.expect(index);
    }

    @Override
    public void submit(I item) {
        this.eventSubmitStrategy.submit(item, listener);
    }

    @Override
    public void submitError(Throwable error) {
        this.errorSubmitStrategy.submit(error, this.listener);
    }

    @Override
    public void accept(long index) {
        this.listenerBlockingStrategy.accept(index);
    }

    @Override
    public void close(long index) {
        this.closeStrategy.close(index, this.listener);
    }
}
