package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface SubmitStrategy<I, O> {
    Logger LOGGER = LoggerFactory.getLogger(SubmitStrategy.class);

    void submit(I item, long index, EventStreamListener<O> listener);

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
            Event<O> event = loop.submit(() -> {
                O ret = this.function.apply(item);
                this.blockingStrategy.accept(index);
                return ret;
            });
            listener.expect(this.indexGenerator.next());
            event.onComplete(listener::addInput);
        }
    }
}
