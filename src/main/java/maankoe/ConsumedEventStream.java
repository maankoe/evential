package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ConsumedEventStream<T>
        extends BaseEventStream<T>
        implements EventStreamListener<T> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConsumedEventStream.class);

    private final Consumer<T> consumer;
    private final Collection<Long> expecting;
    private final IndexGenerator indexGenerator;
    private final AtomicBoolean isBlocked = new AtomicBoolean(false);
    private CompletableFuture<Boolean> isComplete = null;

    public ConsumedEventStream(
            EventLoop loop,
            Consumer<T> consumer
    ) {
        super(loop);
        this.consumer = consumer;
        this.expecting = ConcurrentHashMap.newKeySet();
        this.indexGenerator = new IndexGenerator();
    }

    public void expect(long index) {
        if (isBlocked.get()) {
            throw new RuntimeException("CANNOT");
        }
        this.expecting.add(index);
    }

    public void addInput(T item) {
        this.addInput(item, (int) item);
    }

    public void addInput(T item, long index) {
        LOGGER.info("CONSUME {} {}", item, expecting);
        this.expecting.remove(index);
        if (this.isBlocked.get() && this.expecting.isEmpty()) {
            this.isComplete.complete(true);
        }
        Event<T> event = loop.submit(() -> {
            consumer.accept(item);
            return item;
        });
//        long eventIndex = this.indexGenerator.next();
        event.onComplete(x -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            listener.addInput(x);
        });
    }

    public void block() throws ExecutionException, InterruptedException {
        LOGGER.debug("BLOCK");
        this.isBlocked.compareAndSet(false, true);
        this.isComplete = new CompletableFuture<>();
        while (!this.expecting.isEmpty()) {
            LOGGER.debug("AWAITING {} inputs", this.expecting);
            try {
                this.isComplete.get(1000, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                //do nothing
            }
        }
    }
}
