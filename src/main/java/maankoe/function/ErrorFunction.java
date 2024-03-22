package maankoe.function;


import maankoe.utilities.Optional;
import maankoe.utilities.Result;

import java.util.function.Function;


public interface ErrorFunction<O> extends EventFunction<Throwable, Result<O>> {
    class Blackhole<O> implements ErrorFunction<O> {
        public Blackhole() {}
        @Override
        public Optional<Result<O>> apply(Throwable error) {
            return Optional.empty();
        }
    }

    class Identity<O> implements ErrorFunction<O> {
        public Identity() {}
        @Override
        public Optional<Result<O>> apply(Throwable error) {
            return Optional.of(Result.Error(error));
        }
    }

    class ErrorConsumerBlackhole<O> implements ErrorFunction<O> {
        private final java.util.function.Consumer<Throwable> consumer;
        public ErrorConsumerBlackhole(java.util.function.Consumer<Throwable> consumer) {
            this.consumer = consumer;
        }
        @Override
        public Optional<Result<O>> apply(Throwable error) {
            this.consumer.accept(error);
            return Optional.empty();
        }
    }

    class ErrorConsumer<O> implements ErrorFunction<O> {
        private final java.util.function.Consumer<Throwable> consumer;
        public ErrorConsumer(java.util.function.Consumer<Throwable> consumer) {
            this.consumer = consumer;
        }
        @Override
        public Optional<Result<O>> apply(Throwable error) {
            this.consumer.accept(error);
            return Optional.of(Result.Error(error));
        }
    }

    class ErrorMapper<O> implements ErrorFunction<O> {
        private final Function<Throwable, O> mapper;
        public ErrorMapper(Function<Throwable, O> mapper) {
            this.mapper = mapper;
        }
        @Override
        public Optional<Result<O>> apply(Throwable error) {
            return Optional.of(Result.Success(this.mapper.apply(error)));
        }
    }
}
