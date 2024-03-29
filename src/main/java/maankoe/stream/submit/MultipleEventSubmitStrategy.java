package maankoe.stream.submit;

import maankoe.function.EventFunction;
import maankoe.loop.Event;
import maankoe.loop.EventLoop;
import maankoe.stream.base.EventStreamListener;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.utilities.IndexGenerator;
import maankoe.utilities.Optional;

public class MultipleEventSubmitStrategy<I, O> implements EventSubmitStrategy<I, O> {
    private final EventLoop loop;
    private final EventFunction<I, Iterable<O>> function;
    private final IndexGenerator indexGenerator;
    private final EventBlockingStrategy eventBlockingStrategy;

    public MultipleEventSubmitStrategy(
            EventLoop loop,
            EventFunction<I, Iterable<O>> function,
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
        Event<Optional<Iterable<O>>> event = loop.submit(() -> this.function.apply(item));
        long submitIndex = this.indexGenerator.next();
        listener.expect(submitIndex);
        event.onSuccess(ox ->
            ox.ifPresent(
                    xi -> xi.forEach(listener::submit)
            )
        );
        event.onError(listener::submitError);
        event.onComplete(ox -> listener.accept(submitIndex));
    }
}
