package maankoe.utilities;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LimitedCollection<T> {
    private final static Logger LOGGER = LoggerFactory.getLogger(LimitedCollection.class);

    private static final Object OBJECT = new Object();

    private final int limit;
    private final AtomicInteger size;
    private final Collection<T> base;
    private final AtomicBoolean emitted;
    private final CompletableFuture<Object> collected;

    public LimitedCollection(int limit) {
        this.limit = limit;
        this.base = new ConcurrentLinkedQueue<>();
        this.size = new AtomicInteger(0);
        this.emitted = new AtomicBoolean(false);
        this.collected = new CompletableFuture<>();
    }

    public Optional<Collection<T>> get() {
        try {
            this.collected.get();
        } catch (InterruptedException | ExecutionException e) {
            // do nothing
        }
        if (emitted.compareAndSet(false, true)) {
            return Optional.of(this.base);
        } else {
            return Optional.empty();
        }
    }

    public boolean add(T item) {
        if (this.size.getAndIncrement() < limit) {
            LOGGER.info("ADD: {}, {} + {}", this.size.get(), this, item);
            this.base.add(item);
            if (this.base.size() == limit) {
                this.collected.complete(OBJECT);
            }
            return true;
        } else {
            LOGGER.info("FULL: {}, {} / {}", this.size.get(), this, item);
            return false;
        }
    }

    @Override
    public String toString() {
        return this.base.toString();
    }
}
