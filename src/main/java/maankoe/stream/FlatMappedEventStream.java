package maankoe.stream;

import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.stream.submit.SubmitStrategy;
import maankoe.loop.EventLoop;

import java.util.function.Function;

public class FlatMappedEventStream<I, O> extends GeneralEventStream<I, O> {

    public FlatMappedEventStream(EventLoop loop, Function<I, Iterable<O>> mapper) {
        super(
                loop,
                SubmitStrategy.multiple(
                        loop,
                        new EventFunction.Mapper<>(mapper),
                        new ListenerBlockingStrategy("FLATMAP"),
                        new EventBlockingStrategy("FLATMAP")
                ),
            "FLATMAP"
        );
    }
}
