package maankoe.stream.reduce;

import maankoe.function.ErrorFunction;
import maankoe.loop.Event;
import maankoe.loop.EventLoop;
import maankoe.stream.base.BaseEventStream;
import maankoe.stream.base.EventStreamListener;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.stream.submit.ErrorSubmitStrategy;
import maankoe.stream.submit.SingleErrorSubmitStrategy;
import maankoe.utilities.IndexGenerator;
import maankoe.utilities.Optional;
import maankoe.utilities.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;


public class WindowedEventStream<O>
        extends BaseEventStream<Iterable<O>>
        implements EventStreamListener<O> {

    private final static Logger LOGGER = LoggerFactory.getLogger(WindowedEventStream.class);

    private final EventLoop loop;
    private final IndexGenerator indexGenerator;
    private final ListenerBlockingStrategy listenerBlockingStrategy;
    private final EventBlockingStrategy eventBlockingStrategy;
    private final String name = "WINDOW";
    private final ErrorSubmitStrategy<Iterable<O>> errorSubmitStrategy;


    private final AtomicReference<LimitedCollection<O>> current;
    private final int windowSize;

    public WindowedEventStream(
            EventLoop loop,
            int windowSize,
            ErrorFunction<Iterable<O>> errorFunction,
            ListenerBlockingStrategy listenerBlockingStrategy,
            EventBlockingStrategy eventBlockingStrategy
    ) {
        super(loop);
        this.loop = loop;
        this.windowSize = windowSize;
        this.listenerBlockingStrategy = listenerBlockingStrategy;
        this.eventBlockingStrategy = eventBlockingStrategy;
        this.indexGenerator = new IndexGenerator();
        this.current = new AtomicReference<>(new LimitedCollection<>(windowSize));
        this.errorSubmitStrategy = new SingleErrorSubmitStrategy<>(
                loop, errorFunction, indexGenerator, eventBlockingStrategy
        );
    }

    @Override
    public void expect(long index) {
        this.listenerBlockingStrategy.expect(index);
    }

    @Override
    public void submit(O item) {
        LimitedCollection<O> attempt = this.current.get();
        while (!attempt.add(item)) {
            this.current.compareAndSet(attempt, new LimitedCollection<>(this.windowSize));
            attempt.get().ifPresent(this::submit);
            attempt = current.get();
        }
    }

    private void submit(Collection<O> x) {
        LOGGER.debug("{}: Submit {}", this.name, x);
        Event<Iterable<O>> event = loop.submit(() -> x);
        this.eventBlockingStrategy.submit(event);
        long submitIndex = this.indexGenerator.next();
        listener.expect(submitIndex);
        event.onSuccess(listener::submit);
        event.onError(listener::submitError);
        event.onComplete(orx -> listener.accept(submitIndex));
    }

    @Override
    public void submitError(Throwable error) {
        LOGGER.error("{}: Error {}", this.name, error);
        this.errorSubmitStrategy.submit(error, this.listener);
    }

    @Override
    public void accept(long index) {
        this.listenerBlockingStrategy.accept(index);
    }

    @Override
    public void close(long index) {
        LOGGER.info("{}: Closing stream at index {}", this.name, index);
        this.listenerBlockingStrategy.close(index);
        this.listenerBlockingStrategy.block();
        this.current.get().get()
                .filter(x -> !x.isEmpty())
                .ifPresent(this::submit);
        this.eventBlockingStrategy.block();
        listener.close(this.indexGenerator.current());
    }
}
