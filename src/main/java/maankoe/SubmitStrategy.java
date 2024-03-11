package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public interface SubmitStrategy<I, O> {
    Logger LOGGER = LoggerFactory.getLogger(SubmitStrategy.class);

    void submit(I item, long index, EventStreamListener<O> listener);
    void close(long index, EventStreamListener<O> listener);

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

        public void close(long index, EventStreamListener<O> listener) {
            listener.close(this.indexGenerator.next());
            this.blockingStrategy.close(index);
        }
    }
}
