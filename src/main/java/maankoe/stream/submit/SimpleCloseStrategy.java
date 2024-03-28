package maankoe.stream.submit;

import maankoe.stream.base.EventStreamListener;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.utilities.IndexGenerator;

public class SimpleCloseStrategy implements CloseStrategy {
    private final IndexGenerator indexGenerator;
    private final ListenerBlockingStrategy listenerBlockingStrategy;
    private final EventBlockingStrategy eventBlockingStrategy;
    private final EventStreamListener<?> listener;

    public SimpleCloseStrategy(
            IndexGenerator indexGenerator,
            ListenerBlockingStrategy listenerBlockingStrategy,
            EventBlockingStrategy eventBlockingStrategy,
            EventStreamListener<?> listener
    ) {
        this.indexGenerator = indexGenerator;
        this.listenerBlockingStrategy = listenerBlockingStrategy;
        this.eventBlockingStrategy = eventBlockingStrategy;
        this.listener = listener;
    }

    @Override
    public void close(int index) {
        this.listenerBlockingStrategy.close(index);
        this.listenerBlockingStrategy.block();
        this.eventBlockingStrategy.block();
        this.listener.close(this.indexGenerator.current());
    }
}
