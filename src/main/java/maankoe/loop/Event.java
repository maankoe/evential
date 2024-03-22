package maankoe.loop;

import maankoe.utilities.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Event<T> {
    private final static Logger LOGGER = LoggerFactory.getLogger(Event.class);

    private final Future<T> future;
    private final AtomicBoolean isDone;
    private final Collection<OnceConsumer<T>> onSuccess;
    private final Collection<OnceConsumer<Throwable>> onError;
    private final Collection<OnceConsumer<Result<T>>> onComplete;

    public Event(Future<T> future) {
        this.future = future;
        this.isDone = new AtomicBoolean(false);
        this.onSuccess = new ConcurrentLinkedQueue<>();
        this.onError = new ConcurrentLinkedQueue<>();
        this.onComplete = new ConcurrentLinkedQueue<>();
    }

    public Event<T> onSuccess(Consumer<T> consumer) {
        LOGGER.debug("ON_SUCCESS {}", consumer);
        OnceConsumer<T> onceConsumer = new OnceConsumer<>(consumer);
        this.onSuccess.add(onceConsumer);
        if (this.isDone()) {
            LOGGER.debug("ALREADY_COMPLETED::SUCCESS {}", this.future);
            this.result().ifSuccess(onceConsumer);
        }
        return this;
    }

    public Event<T> onError(Consumer<Throwable> consumer) {
        LOGGER.debug("ON_ERROR {}", consumer);
        OnceConsumer<Throwable> onceConsumer = new OnceConsumer<>(consumer);
        this.onError.add(onceConsumer);
        if (this.isDone()) {
            LOGGER.debug("ALREADY_COMPLETED::ERROR {}", this.future);
            this.result().ifError(onceConsumer);
        }
        return this;
    }

    public Event<T> onComplete(Consumer<Result<T>> consumer) {
        LOGGER.debug("ON_COMPLETE {}", consumer);
        OnceConsumer<Result<T>> onceConsumer = new OnceConsumer<>(consumer);
        this.onComplete.add(onceConsumer);
        if (this.isDone()) {
            LOGGER.debug("ALREADY_COMPLETED::COMPLETE {}", this.future);
            onceConsumer.accept(this.result());
        }
        return this;
    }

    public boolean isDone() {
        this.isDone.compareAndSet(false, future.isDone());
        return this.isDone.get();
    }

    private Result<T> result() {
        try {
            return Result.Success(this.future.get());
        } catch (ExecutionException e) {
            return Result.Error(e.getCause());
        } catch (CancellationException | InterruptedException e) {
            return Result.Error(e);
        }
    }

    public void complete() {
        if (!this.isDone()) {
            this.completeError(new IllegalStateException("Future emitted but not done."));
        } else {
            Result<T> result = this.result();
            if (result.isSuccess()) {
                this.completeSuccess(result.success());
            } else {
                this.completeError(result.error());
            }
            this.completeResult(result);
        }
    }

    private void completeResult(Result<T> result) {
        LOGGER.debug("DONE {}", this);
        for (Consumer<Result<T>> consumer : this.onComplete) {
            consumer.accept(result);
        }
    }

    private void completeSuccess(T result) {
        LOGGER.debug("COMPLETE {}", result);
        for (Consumer<T> consumer : this.onSuccess) {
            consumer.accept(result);
        }
    }

    private void completeError(Throwable error) {
        for (Consumer<Throwable> consumer : this.onError) {
            consumer.accept(error);
        }
    }
}
