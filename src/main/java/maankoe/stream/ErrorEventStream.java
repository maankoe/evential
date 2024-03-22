package maankoe.stream;

import maankoe.function.ErrorFunction;
import maankoe.loop.EventLoop;
import maankoe.stream.base.GeneralEventStream;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.stream.submit.SubmitStrategy;

public class ErrorEventStream<O> extends GeneralEventStream<O, O> {
    public ErrorEventStream(
            EventLoop loop,
            ErrorFunction<O> function,
            String name
    ) {
        super(
                loop,
                SubmitStrategy.singleError(
                    loop,
                    function,
                    new ListenerBlockingStrategy(name),
                    new EventBlockingStrategy(name)
                ),
                name
        );
    }
}
