package maankoe;

import org.assertj.core.util.Streams;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static maankoe.Utilities.waitForCompletion;
import static org.assertj.core.api.Assertions.assertThat;

public class TestEventStream {
    @Test
    public void testConsume() {
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
    public void testDoubleConsume() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<Integer> resultsA = new ConcurrentLinkedQueue<>();
        Collection<Integer> resultsB = new ConcurrentLinkedQueue<>();
        EventStream<Integer> stream = new EventStream<>(loop);
        stream
                .consume(resultsA::add)
                .consume(resultsB::add);
        List<Integer> expected = new ArrayList<>();
        for (int i=0;i<1000;i++) {
            expected.add(i);
            stream.addInput(i);
        }
        waitForCompletion(loop);
        assertThat(resultsA).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(resultsB).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testMap() {
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

    @Test
    public void testFlatMap() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<String> results = new ConcurrentLinkedQueue<>();
        EventStream<String> stream = new EventStream<>(loop);
        Function<String, Iterable<String>> mapper =
                x -> Arrays.stream(x.split(" ")).toList();
        stream
                .flatMap(mapper)
                .consume(results::add);
        List<String> expected = new ArrayList<>();
        for (int i=0;i<1000;i++) {
            String input = String.format("%d %d %d", i, i, i);
            expected.addAll(Streams.stream(mapper.apply(input)).toList());
            stream.addInput(input);
        }
        waitForCompletion(loop);
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testFilter() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<Integer> results = new ConcurrentLinkedQueue<>();
        EventStream<Integer> stream = new EventStream<>(loop);
        Predicate<Integer> predicate = x -> x%2==0;
        stream
                .filter(predicate)
                .consume(results::add);
        List<Integer> expected = new ArrayList<>();
        for (int i=0;i<1000;i++) {
            if (predicate.test(i)) expected.add(i);
            stream.addInput(i);
        }
        waitForCompletion(loop);
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }
}
