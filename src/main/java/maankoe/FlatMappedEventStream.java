package maankoe;

import java.util.function.Function;

public class FlatMappedEventStream<I, O> extends GeneralEventStream<I, O> {

    public FlatMappedEventStream(EventLoop loop, Function<I, Iterable<O>> mapper) {
        super(
                loop,
                new SubmitStrategy.Multiple<>(
                        loop,
                        new EventFunction.Mapper<>(mapper),
                        new BlockingStrategy.Expecting("FLATMAP")
                ),
            "FLATMAP"
        );
    }
}
