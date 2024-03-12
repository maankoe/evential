package maankoe;


import java.util.function.Function;

public class MappedEventStream<I, O> extends GeneralEventStream<I, O> {

    public MappedEventStream(EventLoop loop, Function<I, O> mapper) {
        super(loop, new EventFunction.Mapper<>(mapper), "MAP");
    }
}
