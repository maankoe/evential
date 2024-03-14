package maankoe;

import maankoe.loop.EventLoop;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static maankoe.Utilities.waitForCompletion;
import static org.assertj.core.api.Assertions.assertThat;

public class TestEventLoop {
    class SideEffectConsumer<T> implements Consumer<T> {
        Collection<T> items = new ConcurrentLinkedQueue<>();
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
                .onSuccess(consumer);
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
                    .onSuccess(consumer);
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
                .onSuccess(x ->
                        loop.submit(() -> x * multiplier)
                                .onSuccess(consumer)
                                .onError(errorConsumer)
                );
        Thread.sleep(100);
        assertThat(errorConsumer.items).isEmpty();
        assertThat(consumer.items).containsExactly(value * multiplier);
    }
}