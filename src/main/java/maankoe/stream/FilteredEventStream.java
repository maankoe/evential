package maankoe.stream;

import maankoe.function.EventFunction;
import maankoe.loop.EventLoop;
import maankoe.stream.base.GeneralEventStream;

import java.util.function.Predicate;

public class FilteredEventStream<O> extends GeneralEventStream<O, O> {

    public FilteredEventStream(EventLoop loop, Predicate<O> predicate) {
        super(loop, new EventFunction.Filter<>(predicate), "FILTER");
    }
}
