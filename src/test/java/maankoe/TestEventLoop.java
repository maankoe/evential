package maankoe;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class TestEventLoop {
    class SideEffectConsumer<T> implements Consumer<T> {
        List<T> items = new ArrayList<>();
        public void accept(T item) {
//            LOGGER.info("accept {}", item);
            this.items.add(item);
        }
    }

    @Test
    public void testLoopSubmitAndComplete() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        SideEffectConsumer<Integer> consumer = new SideEffectConsumer<>();
        int value = 1;
        loop.submit(() -> value)
                .onComplete(consumer);
        waitForCompletion(loop);
        assertThat(consumer.items).containsExactly(value);
    }

    @Test
    public void testLoopSubmitMultipleAndComplete() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        SideEffectConsumer<Integer> consumer = new SideEffectConsumer<>();
        List<Integer> values = new ArrayList<>();
        for (int i=0;i<1000;i++) {
            int value = i;
            values.add(value);
            loop.submit(() -> value)
                    .onComplete(consumer);
        }
        waitForCompletion(loop);
        assertThat(consumer.items).containsAll(values);
    }

    @Test
    public void testLoopChainedSubmit() throws InterruptedException {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        SideEffectConsumer<Integer> consumer = new SideEffectConsumer<>();
        SideEffectConsumer<Exception> errorConsumer = new SideEffectConsumer<>();
        int value = 3;
        int multiplier = 2;
        loop.submit(() -> value)
                .onComplete(x ->
                        loop.submit(() -> x * multiplier)
                                .onComplete(consumer)
                );
        Thread.sleep(100);
        assertThat(errorConsumer.items).isEmpty();
        assertThat(consumer.items).containsExactly(value * multiplier);
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(TestEventLoop.class);

    private void waitForCompletion(EventLoop loop) {
        while (loop.hasEvents()) {
//            LOGGER.info("{}", loop.events);
        }
    }
}