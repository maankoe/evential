package maankoe.utilities;

import java.util.Objects;

public class Result<T> {
    public static <T> Result<T> Success(T success) {
        return new Result<>(success);
    }

    public static <T> Result<T> Error(Exception error) {
        return new Result<>(error);
    }

    private final T success;
    private final Exception error;

    private Result(T success) {
        this.success = success;
        this.error = null;
    }

    private Result(Exception error) {
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

    public Exception error() {
        return this.error;
    }
}
