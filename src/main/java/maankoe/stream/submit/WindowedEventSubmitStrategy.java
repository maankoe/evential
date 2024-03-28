package maankoe.stream.submit;

import maankoe.function.ErrorFunction;
import maankoe.loop.Event;
import maankoe.loop.EventLoop;
import maankoe.stream.base.BaseEventStream;
import maankoe.stream.base.EventStreamListener;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.stream.reduce.LimitedCollection;
import maankoe.stream.submit.ErrorSubmitStrategy;
import maankoe.stream.submit.SingleErrorSubmitStrategy;
import maankoe.utilities.IndexGenerator;
import maankoe.utilities.Optional;
import maankoe.utilities.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;


public class WindowedEventSubmitStrategy<O> implements EventSubmitStrategy<O, Iterable<O>> {

    private final EventLoop loop;
    private final IndexGenerator indexGenerator;
    private final EventBlockingStrategy eventBlockingStrategy;

    private final AtomicReference<LimitedCollection<O>> current;
    private final int windowSize;

    public WindowedEventSubmitStrategy(
            EventLoop loop,
            int windowSize,
            IndexGenerator indexGenerator,
            EventBlockingStrategy eventBlockingStrategy
    ) {
        this.loop = loop;
        this.windowSize = windowSize;
        this.eventBlockingStrategy = eventBlockingStrategy;
        this.indexGenerator = indexGenerator;
        this.current = new AtomicReference<>(new LimitedCollection<>(windowSize));
    }

    @Override
    public void submit(O item, EventStreamListener<Iterable<O>> listener) {
        LimitedCollection<O> attempt = this.current.get();
        while (!attempt.add(item)) {
            this.current.compareAndSet(attempt, new LimitedCollection<>(this.windowSize));
            attempt.get().ifPresent(x -> this.submit(x, listener));
            attempt = current.get();
        }
    }

    private void submit(Collection<O> x, EventStreamListener<Iterable<O>> listener) {
        Event<Iterable<O>> event = loop.submit(() -> x);
        this.eventBlockingStrategy.submit(event);
        long submitIndex = this.indexGenerator.next();
        listener.expect(submitIndex);
        event.onSuccess(listener::submit);
        event.onError(listener::submitError);
        event.onComplete(orx -> listener.accept(submitIndex));
    }
}
