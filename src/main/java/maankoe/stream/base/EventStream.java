package maankoe.stream.base;


import maankoe.function.ErrorFunction;
import maankoe.function.EventFunction;
import maankoe.loop.EventLoop;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.stream.submit.*;
import maankoe.utilities.IndexGenerator;

public class EventStream<O> extends GeneralEventStream<O, O> {

    public EventStream(
            EventLoop loop,
            IndexGenerator indexGenerator,
            ListenerBlockingStrategy listenerBlockingStrategy,
            EventBlockingStrategy eventBlockingStrategy,
            EventSubmitStrategy<O, O> eventSubmitStrategy,
            ErrorSubmitStrategy<O> errorSubmitStrategy,
            CloseStrategy closeStrategy
    ) {
        super(
                loop,
                indexGenerator,
                listenerBlockingStrategy,
                eventBlockingStrategy,
                eventSubmitStrategy,
                errorSubmitStrategy,
                closeStrategy
        );
    }

    public static <O> EventStream<O> create(EventLoop loop) {
        return EventStream.create(loop, "BASE");
    }

    public static <O> EventStream<O> create(EventLoop loop, String name) {
        IndexGenerator indexGenerator = new IndexGenerator();
        ListenerBlockingStrategy listenerBlockingStrategy = new ListenerBlockingStrategy(name);
        EventBlockingStrategy eventBlockingStrategy = new EventBlockingStrategy(name);
        return new EventStream<>(
                loop,
                indexGenerator,
                listenerBlockingStrategy,
                eventBlockingStrategy,
                new SingleEventSubmitStrategy<>(
                        loop, new EventFunction.Identity<>(), indexGenerator, eventBlockingStrategy
                ),
                new SingleErrorSubmitStrategy<>(
                        loop, new ErrorFunction.Identity<>(), indexGenerator, eventBlockingStrategy
                ),
                new SimpleCloseStrategy(
                        indexGenerator, listenerBlockingStrategy, eventBlockingStrategy
                )
        );
    }
}
