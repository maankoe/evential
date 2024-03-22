package maankoe;

import maankoe.loop.EventLoop;
import maankoe.stream.ConsumedEventStream;
import maankoe.stream.EventStream;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.function.Function;
import static org.assertj.core.api.Assertions.assertThat;

public class TestEventStreamDistinct {
    @Test
    public void testDistinct() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<Integer> results = new ConcurrentLinkedQueue<>();
        EventStream<Integer> stream = new EventStream<>(loop);
        ConsumedEventStream<Integer> outStream = stream
                .distinct(Function.identity())
                .consume(results::add);
        Set<Integer> expected = new HashSet<>();
        int n = 1000;
        for (int i=0;i<=n;i++) {
            int item = i / 2;
            stream.expect(i);
            if (!expected.contains(item)) {
                expected.add(item);
            }
            stream.submit(item);
            stream.accept(i);
        }
        stream.close(n);
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testDistinctUsingKey() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<Integer> results = new ConcurrentLinkedQueue<>();
        EventStream<Integer> stream = new EventStream<>(loop);
        Function<Integer, Integer> key = x -> x / 2;
        ConsumedEventStream<Integer> outStream = stream
                .distinct(key)
                .consume(results::add);
        Set<Integer> observed = new HashSet<>();
        int n = 100;
        for (int i=0;i<=n;i++) {
            stream.expect(i);
            observed.add(key.apply(i));
            stream.submit(i);
            stream.accept(i);
        }
        stream.close(n);
        assertThat(results).hasSameSizeAs(observed);
    }
}
