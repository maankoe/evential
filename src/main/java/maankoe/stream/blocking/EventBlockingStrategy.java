package maankoe.stream.blocking;

import maankoe.loop.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class EventBlockingStrategy {
    private final static Logger LOGGER = LoggerFactory.getLogger(EventBlockingStrategy.class);

    private final String name;
    private final AtomicLong ticker;
    private final AtomicBoolean isBlocked;
    private CompletableFuture<Boolean> isComplete = null;

    public EventBlockingStrategy(String name) {
        this.name = name;
        this.ticker = new AtomicLong(0);
        this.isBlocked = new AtomicBoolean(false);
    }

    public void submit(Event<?> event) {
        this.expect();
        event.onComplete(x -> this.accept());
    }

    private void expect() {
        this.ticker.getAndIncrement();
    }

    private void accept() {
        this.ticker.getAndDecrement();
        if (this.isBlocked.get() && this.ticker.get() == 0) {
            LOGGER.info("{}: Completed", this.name);
            this.isComplete.complete(true);
        }
    }

    public void block() {
        LOGGER.info("{}: BLOCK", this.name);
        this.isBlocked.compareAndSet(false, true);
        this.isComplete = new CompletableFuture<>();
        while (this.ticker.get() != 0) {
            LOGGER.info("{}: AWAITING {}", this.name, this.ticker.get());
            try {
                this.isComplete.get(1000, TimeUnit.MILLISECONDS);
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                //do nothing
            }
        }
    }
}
