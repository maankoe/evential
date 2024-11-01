package maankoe;

import maankoe.loop.EventLoop;
import maankoe.stream.base.BaseEventStream;
import maankoe.stream.base.EventStream;
import maankoe.stream.reduce.Accumulation;
import maankoe.stream.reduce.Reduction;
import org.junit.jupiter.api.Test;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class TestReduce {

    @Test
    public void testAccumulate() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        EventStream<Integer> stream = EventStream.create(loop);
        Accumulation<Integer, Integer> result = stream
                .reduce(Integer::sum, () -> 0);
        int expected = 0;
        int n = 1000;
        for (int i=0;i<=n;i++) {
            expected += i;
            stream.expect(i);
            stream.submit(i);
            stream.accept(i);
        }
        stream.close(n);
        assertThat(result.get()).isEqualTo(expected);
    }
    @Test
    public void testReduceEvenBatch() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        EventStream<Integer> stream = EventStream.create(loop);
        Reduction<Integer> result = stream
                .reduce(Integer::sum);
        int expected = 0;
        int n = 1000;
        for (int i=0;i<n;i++) {
            expected += i;
            stream.expect(i);
            stream.submit(i);
            stream.accept(i);
        }
        stream.close(n-1);
        assertThat(result.get()).isEqualTo(expected);
    }

    @Test
    public void testReduceOddBatch() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        EventStream<Integer> stream = EventStream.create(loop);
        Reduction<Integer> result = stream
                .reduce(Integer::sum);
        int expected = 0;
        int n = 999;
        for (int i=0;i<n;i++) {
            expected += i;
            stream.expect(i);
            stream.submit(i);
            stream.accept(i);
        }
        stream.close(n-1);
        assertThat(result.get()).isEqualTo(expected);
    }

    @Test
    public void testReduceConsume() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        EventStream<Integer> stream = EventStream.create(loop);
        AtomicInteger value = new AtomicInteger();
        stream
                .reduce(Integer::sum)
                .consume(value::set);
        int expected = 0;
        int n = 99;
        for (int i=0;i<n;i++) {
            expected += i;
            stream.expect(i);
            stream.submit(i);
            stream.accept(i);
        }
        stream.close(n-1);
        assertThat(value.get()).isEqualTo(expected);
    }
}
