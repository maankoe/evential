package maankoe.utilities;

import java.util.Objects;
import java.util.function.Consumer;

public class Optional<T> {
    private final T value;
    private final boolean isPresent;

    private Optional(T value, boolean isPresent) {
        this.value = value;
        this.isPresent = isPresent;
    }

    public static <T> Optional<T> empty() {
        return new Optional<>(null, false);
    }

    public static <T> Optional<T> of(T value) {
        return new Optional<>(value, true);
    }

    public static <T> Optional<T> emptyIfNull(T value) {
        return new Optional<>(value, Objects.isNull(value));
    }

    public void ifPresent(Consumer<? super T> action) {
        if (this.isPresent) {
            action.accept(value);
        }
    }
}
