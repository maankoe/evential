package maankoe.utilities;

import java.util.Objects;
import java.util.function.Consumer;

public class Result<T> {
    public static <T> Result<T> Success(T success) {
        return new Result<>(success);
    }

    public static <T> Result<T> Error(Throwable error) {
        return new Result<>(error);
    }

    private final T success;
    private final Throwable error;

    private Result(T success) {
        this.success = success;
        this.error = null;
    }

    private Result(Throwable error) {
        this.success = null;
        this.error = error;
    }

    public boolean isSuccess() {
        return Objects.nonNull(success);
    }

    public boolean isError() {
        return !this.isSuccess();
    }

    public T success() {
        return this.success;
    }

    public Throwable error() {
        return this.error;
    }

    public Result<T> ifSuccess(Consumer<T> consumer) {
        if (this.isSuccess()) {
            consumer.accept(this.success);
        }
        return this;
    }

    public Result<T> ifError(Consumer<Throwable> consumer) {
        if (this.isError()) {
            consumer.accept(this.error);
        }
        return this;
    }
}
