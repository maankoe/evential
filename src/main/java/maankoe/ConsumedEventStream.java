package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class ConsumedEventStream<T> extends GeneralEventStream<T, T> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConsumedEventStream.class);

    public ConsumedEventStream(EventLoop loop, Consumer<T> consumer) {
        super(loop, new EventFunction.Consumer<>(consumer), "CONSUME");
    }
}
