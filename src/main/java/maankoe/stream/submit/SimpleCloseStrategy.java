package maankoe.stream.submit;

import maankoe.stream.base.EventStreamListener;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.utilities.IndexGenerator;

public class SimpleCloseStrategy implements CloseStrategy {
    private final IndexGenerator indexGenerator;
    private final ListenerBlockingStrategy listenerBlockingStrategy;
    private final EventBlockingStrategy eventBlockingStrategy;

    public SimpleCloseStrategy(
            IndexGenerator indexGenerator,
            ListenerBlockingStrategy listenerBlockingStrategy,
            EventBlockingStrategy eventBlockingStrategy
    ) {
        this.indexGenerator = indexGenerator;
        this.listenerBlockingStrategy = listenerBlockingStrategy;
        this.eventBlockingStrategy = eventBlockingStrategy;
    }

    @Override
    public void close(long index, EventStreamListener<?> listener) {
        this.listenerBlockingStrategy.close(index);
        this.listenerBlockingStrategy.block();
        this.eventBlockingStrategy.block();
        listener.close(this.indexGenerator.current());
    }
}
