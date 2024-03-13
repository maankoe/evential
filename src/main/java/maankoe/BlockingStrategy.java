package maankoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.max;

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

        private final String name;
        private final Collection<Long> expecting;
        private final Collection<Long> accepted;
        private long closeIndex = Long.MAX_VALUE;
        private long maxExpecting = Long.MIN_VALUE;

        public Expecting(String name) {
            this.name = name;
            this.expecting = ConcurrentHashMap.newKeySet();
            this.accepted = ConcurrentHashMap.newKeySet();
        }

        @Override
        public void expect(long index) {
            if (index >= this.closeIndex) {
                LOGGER.error("Cannot expect {} on stream closed at {}", index, this.closeIndex);
                throw new IllegalStateException(String.format(
                        "Stream is closed and cannot expect more input, closed at %d, received %d",
                        this.closeIndex,
                        index
                ));
            }
            this.expecting.add(index);
            this.maxExpecting = max(this.maxExpecting, index);
        }

        @Override
        public void close(long index) {
            this.closeIndex = index;
        }

        @Override
        public void block() {
            LOGGER.info("{}: BLOCK closeIndex={}, maxExpecting={}", this.name, closeIndex, maxExpecting);
            this.isBlocked.compareAndSet(false, true);
            this.isComplete = new CompletableFuture<>();
            while (!this.expecting.isEmpty() || this.maxExpecting < this.closeIndex) {
                LOGGER.info(
                        "{}: AWAITING {} inputs, {}/{}",
                        this.name, this.expecting.size(),
                        this.maxExpecting, this.closeIndex
                );
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
            this.accepted.add(index);
            if (this.isBlocked.get() && this.expecting.isEmpty()) {
                LOGGER.info(
                        "{}: Completed, accepted: {}, {}/{}",
                        this.name, this.accepted.size(),
                        this.maxExpecting, this.closeIndex
                );
                this.isComplete.complete(true);
            }
        }
    }
}
