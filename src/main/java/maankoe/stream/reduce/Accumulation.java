package maankoe.stream.reduce;

import maankoe.loop.Event;
import maankoe.loop.EventLoop;
import maankoe.stream.base.BaseEventStream;
import maankoe.stream.base.EventStreamListener;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.stream.submit.CloseStrategy;
import maankoe.stream.submit.ErrorSubmitStrategy;
import maankoe.stream.submit.SimpleCloseStrategy;
import maankoe.stream.submit.SingleErrorIdentitySubmitStrategy;
import maankoe.utilities.IndexGenerator;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class Accumulation<I, O>
        extends BaseEventStream<I>
        implements EventStreamListener<I> {

    private final IndexGenerator indexGenerator;
    private final BiFunction<I, O, O> function;

    private final ListenerBlockingStrategy listenerBlockingStrategy;
    private final EventBlockingStrategy eventBlockingStrategy;
    private final ErrorSubmitStrategy<I> errorSubmitStrategy;
    private final CloseStrategy closeStrategy;

    private final EventLoop reducerLoop;
    private final AtomicReference<O> current;


    public Accumulation(
            EventLoop loop,
            IndexGenerator indexGenerator,
            ListenerBlockingStrategy listenerBlockingStrategy,
            EventBlockingStrategy eventBlockingStrategy,
            BiFunction<I, O, O> function,
            Supplier<O> identitySupplier,
            ErrorSubmitStrategy<I> errorSubmitStrategy,
            CloseStrategy closeStrategy
    ) {
        super(loop);
        this.indexGenerator = indexGenerator;
        this.listenerBlockingStrategy = listenerBlockingStrategy;
        this.eventBlockingStrategy = eventBlockingStrategy;
        this.function = function;
        this.errorSubmitStrategy = errorSubmitStrategy;
        this.closeStrategy = closeStrategy;

        this.reducerLoop = new EventLoop(1);
        this.reducerLoop.start();
        this.current = new AtomicReference<>(identitySupplier.get());
    }

    public static <I, O> Accumulation<I, O> create(
            EventLoop loop,
            BiFunction<I, O, O> function,
            Supplier<O> identitySupplier,
            String name
    ) {
        IndexGenerator indexGenerator = new IndexGenerator();
        ListenerBlockingStrategy listenerBlockingStrategy = new ListenerBlockingStrategy(name);
        EventBlockingStrategy eventBlockingStrategy = new EventBlockingStrategy(name);
        return new Accumulation<>(
                loop,
                indexGenerator,
                listenerBlockingStrategy,
                eventBlockingStrategy,
                function,
                identitySupplier,
                new SingleErrorIdentitySubmitStrategy<>(indexGenerator),
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
        Event<O> event = reducerLoop.submit(
                () -> {
                    O value = this.function.apply(item, this.current.get());
                    this.current.set(value);
                    return value;
                }
        );
        this.eventBlockingStrategy.submit(event);
        long submitIndex = this.indexGenerator.next();
        this.listener.expect(submitIndex);
        event.onSuccess(x -> listener.submit(item));
        event.onError(listener::submitError);
        event.onComplete(orx -> listener.accept(submitIndex));
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

    public O get() {
        return this.current.get();
    }
}
