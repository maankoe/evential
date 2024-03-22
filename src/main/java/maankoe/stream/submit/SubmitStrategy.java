package maankoe.stream.submit;

import maankoe.function.ErrorFunction;
import maankoe.loop.EventLoop;
import maankoe.function.EventFunction;
import maankoe.stream.base.EventStreamListener;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface SubmitStrategy<I, O> {
    Logger LOGGER = LoggerFactory.getLogger(SubmitStrategy.class);

    void expect(long index);
    void submit(I item, EventStreamListener<O> listener);
    void submitError(Throwable error, EventStreamListener<O> listener);
    void accept(long index);
    void close(long index, EventStreamListener<O> listener);

    static <I, O> SubmitStrategy<I, O> single(
            EventLoop loop,
            EventFunction<I, O> function,
            ListenerBlockingStrategy listenerBlockingStrategy,
            EventBlockingStrategy eventBlockingStrategy
    ) {
        return new SingleSubmitStrategy<>(
                loop,
                function,
                new ErrorFunction.Identity<>(),
                listenerBlockingStrategy,
                eventBlockingStrategy
        );
    }

    static <I> SubmitStrategy<I, I> singleError(
            EventLoop loop,
            ErrorFunction<I> function,
            ListenerBlockingStrategy listenerBlockingStrategy,
            EventBlockingStrategy eventBlockingStrategy
    ) {
        return new SingleSubmitStrategy<>(
                loop,
                new EventFunction.Identity<>(),
                function,
                listenerBlockingStrategy,
                eventBlockingStrategy
        );
    }

    static <I, O> SubmitStrategy<I, O> multiple(
            EventLoop loop,
            EventFunction<I, Iterable<O>> function,
            ListenerBlockingStrategy listenerBlockingStrategy,
            EventBlockingStrategy eventBlockingStrategy
    ) {
        return new MultipleSubmitStrategy<>(
                loop,
                function,
                new ErrorFunction.Identity<>(),
                listenerBlockingStrategy,
                eventBlockingStrategy
        );
    }
}
