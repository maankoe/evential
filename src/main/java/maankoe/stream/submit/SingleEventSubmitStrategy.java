package maankoe.stream.submit;

import maankoe.function.EventFunction;
import maankoe.loop.Event;
import maankoe.loop.EventLoop;
import maankoe.stream.base.EventStreamListener;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.utilities.IndexGenerator;
import maankoe.utilities.Optional;

public class SingleEventSubmitStrategy<I, O> implements EventSubmitStrategy<I, O> {
    private final EventLoop loop;
    private final EventFunction<I, O> function;
    private final IndexGenerator indexGenerator;
    private final EventBlockingStrategy eventBlockingStrategy;

    public SingleEventSubmitStrategy(
            EventLoop loop,
            EventFunction<I, O> function,
            IndexGenerator indexGenerator,
            EventBlockingStrategy eventBlockingStrategy
    ) {
        this.loop = loop;
        this.function = function;
        this.eventBlockingStrategy = eventBlockingStrategy;
        this.indexGenerator = indexGenerator;
    }

    @Override
    public void submit(I item, EventStreamListener<O> listener) {
        Event<Optional<O>> event = loop.submit(() -> this.function.apply(item));
        this.eventBlockingStrategy.submit(event);
        long submitIndex = this.indexGenerator.next();
        listener.expect(submitIndex);
        event.onSuccess(ox -> ox.ifPresent(listener::submit));
        event.onError(listener::submitError);
        event.onComplete(orx -> listener.accept(submitIndex));
    }
}
