package maankoe.stream.submit;

import maankoe.function.ErrorFunction;
import maankoe.loop.Event;
import maankoe.loop.EventLoop;
import maankoe.function.EventFunction;
import maankoe.stream.base.EventStreamListener;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.utilities.IndexGenerator;
import maankoe.utilities.Result;

import java.util.Optional;

public class SingleSubmitStrategy<I, O> implements SubmitStrategy<I, O> {
    private final EventLoop loop;
    private final EventFunction<I, O> function;
    private final ErrorFunction<O> errorFunction;
    private final IndexGenerator indexGenerator;
    private final ListenerBlockingStrategy listenerBlockingStrategy;
    private final EventBlockingStrategy eventBlockingStrategy;

    public SingleSubmitStrategy(
            EventLoop loop,
            EventFunction<I, O> function,
            ErrorFunction<O> errorFunction,
            ListenerBlockingStrategy listenerBlockingStrategy,
            EventBlockingStrategy eventBlockingStrategy
    ) {
        this.loop = loop;
        this.function = function;
        this.errorFunction = errorFunction;
        this.listenerBlockingStrategy = listenerBlockingStrategy;
        this.eventBlockingStrategy = eventBlockingStrategy;
        this.indexGenerator = new IndexGenerator();
    }

    @Override
    public void expect(long index) {
        this.listenerBlockingStrategy.expect(index);
    }

    public void submit(
            I item,
            EventStreamListener<O> listener
    ) {
        Event<Optional<O>> event = loop.submit(() -> this.function.apply(item));
        this.eventBlockingStrategy.submit(event);
        long submitIndex = this.indexGenerator.next();
        listener.expect(submitIndex);
        event.onSuccess(ox -> ox.ifPresent(listener::submit));
        event.onError(listener::submitError);
        event.onComplete(orx -> listener.accept(submitIndex));
    }

    @Override
    public void submitError(
            Throwable error,
            EventStreamListener<O> listener
    ) {
        Event<Optional<Result<O>>> event = loop.submit(() -> this.errorFunction.apply(error));
        this.eventBlockingStrategy.submit(event);
        long submitIndex = this.indexGenerator.next();
        listener.expect(submitIndex);
        event.onSuccess(orx -> orx.ifPresent(rx -> rx
                .ifSuccess(listener::submit)
                .ifError(listener::submitError)
        ));
        event.onError(listener::submitError);
        event.onComplete(orx -> listener.accept(submitIndex));
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
