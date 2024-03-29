package maankoe.utilities;

import maankoe.utilities.Optional;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LimitedCollection<T> {
    private final int limit;
    private final AtomicInteger size;
    private final Collection<T> base;
    private final AtomicBoolean emitted;

    public LimitedCollection(int limit) {
        this.limit = limit;
        this.base = new ConcurrentLinkedQueue<>();
        this.size = new AtomicInteger(0);
        this.emitted = new AtomicBoolean(false);
    }

    public Optional<Collection<T>> get() {
        if (emitted.compareAndSet(false, true)) {
            return Optional.of(this.base);
        } else {
            return Optional.empty();
        }
    }

    public boolean add(T item) {
        if (this.size.getAndIncrement() < limit) {
            this.base.add(item);
            return true;
        } else {
            return false;
        }
    }
}
