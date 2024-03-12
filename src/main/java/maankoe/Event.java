package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Event<T> {
    private final static Logger LOGGER = LoggerFactory.getLogger(Event.class);

    private final Future<T> future;
    private final AtomicBoolean isDone;
    private final Collection<OnceConsumer<T>> onComplete;
    private final Collection<OnceConsumer<Exception>> onError;

    public Event(Future<T> future) {
        this.future = future;
        this.isDone = new AtomicBoolean(false);
        this.onComplete = new ConcurrentLinkedQueue<>();
        this.onError = new ConcurrentLinkedQueue<>();
    }

    public Event<T> onComplete(Consumer<T> consumer) {
        LOGGER.debug("ON_COMPLETE {}", consumer);
        OnceConsumer<T> onceConsumer = new OnceConsumer<>(consumer);
        this.onComplete.add(onceConsumer);
        if (this.isDone()) {
            try {
                LOGGER.debug("ALREADY_COMPLETED {}", this.future.get());
                onceConsumer.accept(this.future.get());
            } catch (CancellationException | ExecutionException | InterruptedException e) {
                // pass
            }
        }
        return this;
    }

    public Event<T> onError(Consumer<Exception> consumer) {
        OnceConsumer<Exception> onceConsumer = new OnceConsumer<>(consumer);
        this.onError.add(new OnceConsumer<>(consumer));
        if (this.isDone()) {
            try {
                this.future.get();
            } catch (CancellationException | ExecutionException | InterruptedException e) {
                onceConsumer.accept(e);
            }
        }
        return this;
    }

    public boolean isDone() {
        this.isDone.compareAndSet(false, future.isDone());
        return this.isDone.get();
    }

    public void complete() {
        if (!this.isDone()) {
            this.error(new IllegalStateException("Future emitted but not done."));
        } else {
            try {
                this.complete(this.future.get());
            } catch (CancellationException | ExecutionException | InterruptedException e) {
                this.error(e);
            }
        }
    }

    private void complete(T result) {
        LOGGER.debug("COMPLETE {}", result);
        for (Consumer<T> consumer : this.onComplete) {
            consumer.accept(result);
        }
    }

    private void error(Exception error) {
        for (Consumer<Exception> consumer : this.onError) {
            consumer.accept(error);
        }
    }
}
