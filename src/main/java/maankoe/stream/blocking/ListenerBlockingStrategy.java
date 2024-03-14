package maankoe.stream.blocking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Math.max;

public class ListenerBlockingStrategy {
    private final static Logger LOGGER = LoggerFactory.getLogger(ListenerBlockingStrategy.class);

    private CompletableFuture<Boolean> isSatisfied = null;

    private final String name;
    private final AtomicLong ticker;
    private final AtomicLong closeIndex;
    private final AtomicLong maxExpecting;
    private final AtomicBoolean isBlocked;

    public ListenerBlockingStrategy(String name) {
        this.name = name;
        this.ticker = new AtomicLong(0);
        this.closeIndex = new AtomicLong(Long.MAX_VALUE);
        this.maxExpecting = new AtomicLong(Long.MIN_VALUE);
        this.isBlocked = new AtomicBoolean(false);
    }

    public void expect(long index) {
        if (index >= this.closeIndex.get()) {
            LOGGER.error("Cannot expect {} on stream closed at {}", index, this.closeIndex);
            throw new IllegalStateException(String.format(
                    "Stream is closed and cannot expect more input, closed at %d, received %d",
                    this.closeIndex.get(),
                    index
            ));
        }
        this.ticker.getAndIncrement();
        this.maxExpecting.getAndUpdate(x -> max(x, index));
    }

    public void accept(long index) {
        this.ticker.getAndDecrement();
        if (this.isBlocked.get() && this.ticker.get() == 0) {
            LOGGER.info(
                    "{}: Completed, accepted: {}/{}",
                    this.name, this.maxExpecting, this.closeIndex
            );
            this.isSatisfied.complete(true);
        }
    }

    public void close(long index) {
        this.closeIndex.getAndSet(index);
    }

    public void block() {
        LOGGER.info("{}: BLOCK closeIndex={}, maxExpecting={}", this.name, closeIndex, maxExpecting);
        this.isBlocked.compareAndSet(false, true);
        this.isSatisfied = new CompletableFuture<>();
        while (this.ticker.get() != 0 || this.maxExpecting.get() < this.closeIndex.get()) {
            LOGGER.info(
                    "{}: EXPECTING {}, inputs, {}/{}",
                    this.name,  this.ticker.get(),
                    this.maxExpecting, this.closeIndex
            );
            try {
                this.isSatisfied.get(1000, TimeUnit.MILLISECONDS);
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                //do nothing
            }
        }
    }
}
