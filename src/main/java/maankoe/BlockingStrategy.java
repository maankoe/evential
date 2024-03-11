package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public interface BlockingStrategy {

    void expect(long index);
    void close(long index);
    void accept(long index);
    void block();

    class None implements BlockingStrategy {
        @Override
        public void expect(long index) {
            //do nothing
        }

        @Override
        public void close(long index) {
            // do nothing
        }

        @Override
        public void accept(long index) {
            // do nothing
        }

        @Override
        public void block() {
            // do nothing
        }
    }

    class Expecting implements BlockingStrategy {
        private final static Logger LOGGER = LoggerFactory.getLogger(BlockingStrategy.class);

        private final AtomicBoolean isBlocked = new AtomicBoolean(false);
        private CompletableFuture<Boolean> isComplete = null;

        private final Collection<Long> expecting;
        private long closeIndex = Long.MAX_VALUE;

        public Expecting() {
            this(ConcurrentHashMap.newKeySet());
        }

        public Expecting(Collection<Long> expecting) {
            this.expecting = expecting;
        }

        @Override
        public void expect(long index) {
            if (index > this.closeIndex) {
                throw new IllegalStateException(String.format(
                        "Stream is closed and cannot expect more input, closed at %d, received %d",
                        this.closeIndex,
                        index
                ));
            }
            this.expecting.add(index);
        }

        @Override
        public void close(long index) {
            this.closeIndex = index;
        }

        @Override
        public void block() {
            LOGGER.debug("BLOCK");
            this.isBlocked.compareAndSet(false, true);
            this.isComplete = new CompletableFuture<>();
            while (!this.expecting.isEmpty()) {
                LOGGER.debug("AWAITING {} inputs", this.expecting);
                try {
                    this.isComplete.get(1000, TimeUnit.MILLISECONDS);
                } catch (TimeoutException | InterruptedException | ExecutionException e) {
                    //do nothing
                }
            }
        }

        @Override
        public void accept(long index) {
            this.expecting.remove(index);
            if (this.isBlocked.get() && this.expecting.isEmpty()) {
                this.isComplete.complete(true);
            }
        }
    }
}
