package maankoe.stream.submit;

import maankoe.function.ErrorFunction;
import maankoe.loop.EventLoop;
import maankoe.function.EventFunction;
import maankoe.stream.base.EventStreamListener;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.utilities.IndexGenerator;


public class SingleSubmitStrategy<I, O> implements SubmitStrategy<I, O> {
    private final IndexGenerator indexGenerator;
    private final ListenerBlockingStrategy listenerBlockingStrategy;
    private final EventBlockingStrategy eventBlockingStrategy;
    private final SingleEventSubmitStrategy<I, O> eventSubmitStrategy;
    private final ErrorSubmitStrategy<O> errorSubmitStrategy;

    public SingleSubmitStrategy(
            EventLoop loop,
            EventFunction<I, O> function,
            ErrorFunction<O> errorFunction,
            ListenerBlockingStrategy listenerBlockingStrategy,
            EventBlockingStrategy eventBlockingStrategy
    ) {
        this.listenerBlockingStrategy = listenerBlockingStrategy;
        this.eventBlockingStrategy = eventBlockingStrategy;
        this.indexGenerator = new IndexGenerator();
        this.eventSubmitStrategy = new SingleEventSubmitStrategy<>(
                loop, function, indexGenerator, eventBlockingStrategy
        );
        this.errorSubmitStrategy = new SingleErrorSubmitStrategy<>(
                loop, errorFunction, indexGenerator, eventBlockingStrategy
        );
    }

    @Override
    public void expect(long index) {
        this.listenerBlockingStrategy.expect(index);
    }

    public void submit(
            I item,
            EventStreamListener<O> listener
    ) {
        this.eventSubmitStrategy.submit(item, listener);
    }

    @Override
    public void submitError(
            Throwable error,
            EventStreamListener<O> listener
    ) {
        this.errorSubmitStrategy.submit(error, listener);
    }

    @Override
    public void accept(long index) {
        this.listenerBlockingStrategy.accept(index);
    }

    public void close(long index, EventStreamListener<O> listener) {
        this.listenerBlockingStrategy.close(index);
        this.listenerBlockingStrategy.block();
        this.eventBlockingStrategy.block();
        listener.close(this.indexGenerator.current());
    }
}
