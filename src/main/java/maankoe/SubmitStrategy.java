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

    class Single<I, O> implements SubmitStrategy<I, O> {
        private final EventLoop loop;
        private final EventFunction<I, O> function;
        private final IndexGenerator indexGenerator;
        private final ListenerBlockingStrategy listenerBlockingStrategy;
        private final EventBlockingStrategy eventBlockingStrategy;

        public Single(
                EventLoop loop,
                EventFunction<I, O> function,
                ListenerBlockingStrategy listenerBlockingStrategy,
                EventBlockingStrategy eventBlockingStrategy
        ) {
            this.loop = loop;
            this.function = function;
            this.listenerBlockingStrategy = listenerBlockingStrategy;
            this.eventBlockingStrategy = eventBlockingStrategy;
            this.indexGenerator = new IndexGenerator();
        }

        @Override
        public void expect(long index) {
            this.listenerBlockingStrategy.expect(index);
        }

        public void submit(
                I item,
                EventStreamListener<O> listener
        ) {
            Event<Optional<O>> event = loop.submit(() -> this.function.apply(item));
            this.eventBlockingStrategy.active(event);
            long submitIndex = this.indexGenerator.next();
            listener.expect(submitIndex);
            event.onSuccess(ox -> {
                ox.ifPresent(listener::submit);
                listener.accept(submitIndex);
            });
        }

        @Override
        public void accept(long index) {
            this.listenerBlockingStrategy.accept(index);
        }

        public void close(long index, EventStreamListener<O> listener) {
            this.listenerBlockingStrategy.close(index);
            this.listenerBlockingStrategy.block();
            this.eventBlockingStrategy.block();
            listener.close(this.indexGenerator.current());
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
            event.onSuccess(ox -> {
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
            this.blockingStrategy.block();
            listener.close(this.indexGenerator.current());
        }
    }
}
