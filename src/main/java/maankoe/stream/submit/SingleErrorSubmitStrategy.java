package maankoe.stream.submit;

import maankoe.function.ErrorFunction;
import maankoe.loop.Event;
import maankoe.loop.EventLoop;
import maankoe.stream.base.EventStreamListener;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.utilities.IndexGenerator;
import maankoe.utilities.Optional;
import maankoe.utilities.Result;


public class SingleErrorSubmitStrategy<O> implements ErrorSubmitStrategy<O> {
    private final EventLoop loop;
    private final ErrorFunction<O> errorFunction;
    private final IndexGenerator indexGenerator;
    private final EventBlockingStrategy eventBlockingStrategy;

    public SingleErrorSubmitStrategy(
            EventLoop loop,
            ErrorFunction<O> errorFunction,
            IndexGenerator indexGenerator,
            EventBlockingStrategy eventBlockingStrategy
    ) {
        this.loop = loop;
        this.errorFunction = errorFunction;
        this.indexGenerator = indexGenerator;
        this.eventBlockingStrategy = eventBlockingStrategy;
    }

    @Override
    public void submit(Throwable error, EventStreamListener<O> listener) {
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
}
