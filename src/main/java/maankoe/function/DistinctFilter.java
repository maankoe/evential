package maankoe.function;

import maankoe.utilities.Optional;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DistinctFilter<I, K> implements EventFunction<I, I> {

    private final Function<I, K> key;
    private final ConcurrentHashMap<K, Object> observed;

    public DistinctFilter(Function<I, K> key) {
        this.key = key;
        this.observed = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<I> apply(I item) {
        if (Objects.isNull(this.observed.putIfAbsent(this.key.apply(item), new Object()))) {
            return Optional.of(item);
        } else {
            return Optional.empty();
        }
    }
}
