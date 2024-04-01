package maankoe.stream.base;


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
            CloseStrategy closeStrategy,
            String name
    ) {
        super(
                loop,
                indexGenerator,
                listenerBlockingStrategy,
                eventBlockingStrategy,
                eventSubmitStrategy,
                errorSubmitStrategy,
                closeStrategy,
                name
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
                new SingleIdentitySubmitStrategy<>(indexGenerator),
                new SingleErrorIdentitySubmitStrategy<>(indexGenerator),
                new SimpleCloseStrategy(
                        indexGenerator, listenerBlockingStrategy, eventBlockingStrategy
                ),
                name
        );
    }
}
