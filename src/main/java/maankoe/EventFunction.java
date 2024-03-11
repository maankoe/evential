package maankoe;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public interface EventFunction<I, O> {
    Optional<O> apply(I item);

    class Consumer<I> implements EventFunction<I, I> {
        private final java.util.function.Consumer<I> consumer;
        public Consumer(java.util.function.Consumer<I> consumer) {
            this.consumer = consumer;
        }
        @Override
        public Optional<I> apply(I item) {
            this.consumer.accept(item);
            return Optional.of(item);
        }
    }

    class Mapper<I, O> implements EventFunction<I, O> {
        private final Function<I, O> mapper;
        public Mapper(Function<I, O> mapper) {
            this.mapper = mapper;
        }
        @Override
        public Optional<O> apply(I item) {
            return Optional.of(this.mapper.apply(item));
        }
    }

    class Filter<I> implements EventFunction<I, I> {
        private final Predicate<I> predicate;
        public Filter(Predicate<I> predicate) {
            this.predicate = predicate;
        }
        @Override
        public Optional<I> apply(I item) {
            return this.predicate.test(item) ? Optional.of(item) : Optional.empty();
        }
    }
}
