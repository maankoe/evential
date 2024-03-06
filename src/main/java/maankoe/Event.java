package maankoe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Event<T> {
    private final Future<T> future;
    private final Collection<OnceConsumer<T>> onComplete;
    private final Collection<OnceConsumer<Exception>> onError;

    public Event(Future<T> future) {
        this.future = future;
        this.onComplete = new ConcurrentLinkedQueue<>();
        this.onError = new ConcurrentLinkedQueue<>();
    }

    public Event<T> onComplete(Consumer<T> consumer) {
        OnceConsumer<T> onceConsumer = new OnceConsumer<>(consumer);
        this.onComplete.add(onceConsumer);
        if (this.isDone()) {
            try {
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
        return this.future.isDone();
    }

    public void complete() {
        if (!future.isDone()) {
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
        for (Consumer<T> consumer : new ArrayList<>(this.onComplete)) {
            consumer.accept(result);
        }
    }

    private void error(Exception error) {
        for (Consumer<Exception> consumer : this.onError) {
            consumer.accept(error);
        }
    }
}
