package maankoe.stream.submit;

import maankoe.loop.Event;
import maankoe.loop.EventLoop;
import maankoe.stream.EventFunction;
import maankoe.stream.EventStreamListener;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.utilities.IndexGenerator;

import java.util.Optional;

class MultipleSubmitStrategy<I, O> implements SubmitStrategy<I, O> {
    private final EventLoop loop;
    private final EventFunction<I, Iterable<O>> function;
    private final IndexGenerator indexGenerator;
    private final ListenerBlockingStrategy listenerBlockingStrategy;
    private final EventBlockingStrategy eventBlockingStrategy;

    public MultipleSubmitStrategy(
            EventLoop loop,
            EventFunction<I, Iterable<O>> function,
            ListenerBlockingStrategy listenerBlockingStrategy,
            EventBlockingStrategy eventBlockingStrategy
    ) {
        this.loop = loop;
        this.function = function;
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
        Event<Optional<Iterable<O>>> event = loop.submit(() -> this.function.apply(item));
        long submitIndex = this.indexGenerator.next();
        listener.expect(submitIndex);
        event.onSuccess(ox ->
            ox.ifPresent(
                    xi -> xi.forEach(listener::submit)
            )
        );
        event.onComplete(ox -> listener.accept(submitIndex));
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
