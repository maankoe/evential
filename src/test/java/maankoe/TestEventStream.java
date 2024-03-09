package maankoe;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static maankoe.Utilities.waitForCompletion;
import static org.assertj.core.api.Assertions.assertThat;

public class TestEventStream {
    @Test
    public void testConsume() throws InterruptedException {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<Integer> results = new ConcurrentLinkedQueue<>();
        EventStream<Integer> stream = new EventStream<>(loop);
        stream.consume(results::add);
        List<Integer> expected = new ArrayList<>();
        for (int i=0;i<1000;i++) {
            expected.add(i);
            stream.addInput(i);
        }
        waitForCompletion(loop);
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testMap() throws InterruptedException {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<Integer> results = new ConcurrentLinkedQueue<>();
        EventStream<Integer> stream = new EventStream<>(loop);
        Function<Integer, Integer> mapper = x -> x*3;
        stream
                .map(mapper)
                .consume(results::add);
        List<Integer> expected = new ArrayList<>();
        for (int i=0;i<1000;i++) {
            expected.add(mapper.apply(i));
            stream.addInput(i);
        }
        waitForCompletion(loop);
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }
}
