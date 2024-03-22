package maankoe.stream;

import maankoe.loop.EventLoop;
import maankoe.stream.base.GeneralEventStream;
import maankoe.function.DistinctFilter;

public class DistinctEventStream<I, K> extends GeneralEventStream<I, I> {
    public DistinctEventStream(EventLoop loop, DistinctFilter<I, K> distinctFilter) {
        super(loop, distinctFilter, "DISTINCT");
    }
}
