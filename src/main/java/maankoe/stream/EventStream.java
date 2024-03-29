package maankoe.stream;


import maankoe.function.EventFunction;
import maankoe.loop.EventLoop;
import maankoe.stream.base.GeneralEventStream;

public class EventStream<O> extends GeneralEventStream<O, O> {

    public EventStream(EventLoop loop) {
        super(loop, new EventFunction.Identity<>(), "BASE");
    }
}
