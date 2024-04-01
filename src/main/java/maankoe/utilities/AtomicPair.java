package maankoe.utilities;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicPair<L, R> {
    private final AtomicBoolean leftEmitted;
    private final AtomicBoolean rightEmitted;

    private final AtomicReference<L> left;
    private final AtomicReference<R> right;

    public AtomicPair() {
        this.leftEmitted = new AtomicBoolean(false);
        this.rightEmitted = new AtomicBoolean(false);
        this.left = new AtomicReference<>(null);
        this.right = new AtomicReference<>(null);
    }

    public Optional<L> left() {
        if (leftEmitted.compareAndSet(false, true)) {
            return Optional.of(this.left.get());
        } else {
            return Optional.empty();
        }
    }

    public Optional<R> right() {
        if (rightEmitted.compareAndSet(false, true)) {
            return Optional.of(this.right.get());
        } else {
            return Optional.empty();
        }
    }

    public boolean setLeft(L item) {
        return this.left.compareAndSet(null, item);
    }

    public boolean setRight(R item) {
        return this.right.compareAndSet(null, item);
    }
}
