package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public interface SubmitStrategy<I, O> {
    Logger LOGGER = LoggerFactory.getLogger(SubmitStrategy.class);

    void expect(long index);
    void submit(I item, long index, EventStreamListener<O> listener);
    void accept(long index);
    void close(long index, EventStreamListener<O> listener);
    void block();

    class Single<I, O> implements SubmitStrategy<I, O> {
        private final EventLoop loop;
        private final EventFunction<I, O> function;
        private final IndexGenerator indexGenerator;
        private final BlockingStrategy blockingStrategy;
        private final String name;

        public Single(
                EventLoop loop,
                EventFunction<I, O> function,
                BlockingStrategy blockingStrategy,
                String name
        ) {
            this.loop = loop;
            this.function = function;
            this.name = name;
            this.blockingStrategy = blockingStrategy;
            this.indexGenerator = new IndexGenerator();
        }

        @Override
        public void expect(long index) {
            this.blockingStrategy.expect(index);
        }

        public void submit(
                I item,
                long index,
                EventStreamListener<O> listener
        ) {
            LOGGER.info("{} {}", this.name, item);
            Event<Optional<O>> event = loop.submit(() -> {
                Optional<O> ret = this.function.apply(item);
                this.blockingStrategy.accept(index);
                return ret;
            });
            long submitIndex = this.indexGenerator.next();
            listener.expect(submitIndex);
            event.onComplete(ox -> ox.ifPresentOrElse(
                    x -> listener.submit(x, submitIndex),
                    () -> listener.accept(submitIndex)
            ));
        }

        @Override
        public void accept(long index) {
            this.blockingStrategy.accept(index);
        }

        public void close(long index, EventStreamListener<O> listener) {
            listener.close(this.indexGenerator.next());
            this.blockingStrategy.close(index);
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
        private final String name;

        public Multiple(
                EventLoop loop,
                EventFunction<I, Iterable<O>> function,
                BlockingStrategy blockingStrategy,
                String name
        ) {
            this.loop = loop;
            this.function = function;
            this.name = name;
            this.blockingStrategy = blockingStrategy;
            this.indexGenerator = new IndexGenerator();
        }

        @Override
        public void expect(long index) {
            this.blockingStrategy.expect(index);
        }

        public void submit(
                I item,
                long index,
                EventStreamListener<O> listener
        ) {
            LOGGER.info("{} {}", this.name, item);
            Event<Optional<Iterable<O>>> event = loop.submit(() -> {
//                Thread.sleep(100);
                Optional<Iterable<O>> ret = this.function.apply(item);
                this.blockingStrategy.accept(index);
                return ret;
            });
            long submitIndex = this.indexGenerator.next();
            listener.expect(submitIndex);
            event.onComplete(ox -> ox.ifPresentOrElse(
                    xi -> xi.forEach(x -> listener.submit(x, submitIndex)),
                    () -> listener.accept(submitIndex)
            ));
        }

        @Override
        public void accept(long index) {
            this.blockingStrategy.accept(index);
        }

        public void close(long index, EventStreamListener<O> listener) {
            listener.close(this.indexGenerator.next());
            this.blockingStrategy.close(index);
        }

        @Override
        public void block() {
            this.blockingStrategy.block();
        }
    }
}
