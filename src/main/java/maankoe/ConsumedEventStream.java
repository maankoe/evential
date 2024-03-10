package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class ConsumedEventStream<T>
        extends BaseEventStream<T>
        implements EventStreamListener<T> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConsumedEventStream.class);

    private final Consumer<T> consumer;
    private final IndexGenerator indexGenerator;
    private final BlockingStrategy blockingStrategy;

    public ConsumedEventStream(
            EventLoop loop,
            Consumer<T> consumer
    ) {
        super(loop);
        this.consumer = consumer;
        this.blockingStrategy = new BlockingStrategy.Expecting();
        this.indexGenerator = new IndexGenerator();
    }

    public void expect(long index) {
        this.blockingStrategy.expect(index);
    }

    public void addInput(T item) {
        this.addInput(item, (int) item);
    }

    public void addInput(T item, long index) {
        LOGGER.info("CONSUME {}", item);
        this.blockingStrategy.accept(index);
        Event<T> event = loop.submit(() -> {
            consumer.accept(item);
            return item;
        });
        long eventIndex = this.indexGenerator.next();
//        this.listener.expect(eventIndex);
        event.onComplete(x -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            listener.addInput(x);
        });
    }

    public void block() {
        this.blockingStrategy.block();
    }
}
