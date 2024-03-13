package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public interface SubmitStrategy<I, O> {
    Logger LOGGER = LoggerFactory.getLogger(SubmitStrategy.class);

    void expect(long index);
    void submit(I item, EventStreamListener<O> listener);
    void accept(long index);
    void close(long index, EventStreamListener<O> listener);
    void block();

    class Single<I, O> implements SubmitStrategy<I, O> {
        private final EventLoop loop;
        private final EventFunction<I, O> function;
        private final IndexGenerator indexGenerator;
        private final BlockingStrategy blockingStrategy;

        public Single(
                EventLoop loop,
                EventFunction<I, O> function,
                BlockingStrategy blockingStrategy
        ) {
            this.loop = loop;
            this.function = function;
            this.blockingStrategy = blockingStrategy;
            this.indexGenerator = new IndexGenerator();
        }

        @Override
        public void expect(long index) {
            this.blockingStrategy.expect(index);
        }

        public void submit(
                I item,
                EventStreamListener<O> listener
        ) {
            Event<Optional<O>> event = loop.submit(() -> this.function.apply(item));
            long submitIndex = this.indexGenerator.next();
            listener.expect(submitIndex);
            event.onComplete(ox -> {
                ox.ifPresent(listener::submit);
                listener.accept(submitIndex);
            });
        }

        @Override
        public void accept(long index) {
            this.blockingStrategy.accept(index);
        }

        public void close(long index, EventStreamListener<O> listener) {
            this.blockingStrategy.close(index);
            this.block();
            listener.close(this.indexGenerator.current());
        }

        @Override
        public void block() {
            this.blockingStrategy.block();
        }
    }

    class Multiple<I, O> implements SubmitStrategy<I, O> {
        private final EventLoop loop;
        private final EventFunction<I, Iterable<O>> function;
        private final IndexGenerator indexGenerator;
        private final BlockingStrategy blockingStrategy;

        public Multiple(
                EventLoop loop,
                EventFunction<I, Iterable<O>> function,
                BlockingStrategy blockingStrategy
        ) {
            this.loop = loop;
            this.function = function;
            this.blockingStrategy = blockingStrategy;
            this.indexGenerator = new IndexGenerator();
        }

        @Override
        public void expect(long index) {
            this.blockingStrategy.expect(index);
        }

        public void submit(
                I item,
                EventStreamListener<O> listener
        ) {
            Event<Optional<Iterable<O>>> event = loop.submit(() -> this.function.apply(item));
            long submitIndex = this.indexGenerator.next();
            listener.expect(submitIndex);
            event.onComplete(ox -> {
                ox.ifPresent(
                        xi -> xi.forEach(listener::submit)
                );
                listener.accept(submitIndex);
            });
        }

        @Override
        public void accept(long index) {
            this.blockingStrategy.accept(index);
        }

        public void close(long index, EventStreamListener<O> listener) {
            this.blockingStrategy.close(index);
            this.block();
            listener.close(this.indexGenerator.current());
        }

        @Override
        public void block() {
            this.blockingStrategy.block();
        }
    }
}
