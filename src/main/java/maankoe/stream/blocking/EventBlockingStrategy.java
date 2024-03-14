package maankoe.stream.blocking;

import maankoe.loop.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventBlockingStrategy {
    private final static Logger LOGGER = LoggerFactory.getLogger(EventBlockingStrategy.class);

    private final AtomicBoolean isBlocked = new AtomicBoolean(false);
    private CompletableFuture<Boolean> isComplete = null;

    private final String name;
    private final Collection<Event<?>> activeEvents;

    public EventBlockingStrategy(String name) {
        this.name = name;
        this.activeEvents = ConcurrentHashMap.newKeySet();
    }

    public void active(Event<?> event) {
        this.activeEvents.add(event);
        event.onComplete(x -> {
            this.activeEvents.remove(x);
            if (this.isBlocked.get() && this.activeEvents.isEmpty()) {
                LOGGER.info("{}: Completed", this.name);
                this.isComplete.complete(true);
            }
        });
    }

    public void block() {
        LOGGER.info("{}: BLOCK", this.name);
        this.isBlocked.compareAndSet(false, true);
        this.isComplete = new CompletableFuture<>();
        while (!this.activeEvents.isEmpty()) {
            LOGGER.info("{}: AWAITING {}", this.name, this.activeEvents.size());
            try {
                this.isComplete.get(1000, TimeUnit.MILLISECONDS);
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                //do nothing
            }
        }
    }
}
