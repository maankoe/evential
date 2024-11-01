package maankoe.utilities;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Optional<T> {
    private final T value;
    private final boolean isPresent;

    /** This Optional class isPresent independently of whether the value is null or not */
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

    public Optional<T> ifPresent(Consumer<T> action) {
        if (this.isPresent) {
            action.accept(value);
        }
        return this;
    }

    public Optional<T> filter(Predicate<T> predicate) {
        if (this.isPresent && predicate.test(value)) {
            return this;
        } else {
            return Optional.empty();
        }
    }
}
