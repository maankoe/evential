package maankoe;

import org.assertj.core.util.Streams;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;

import static maankoe.Utilities.sleepConsumer;
import static org.assertj.core.api.Assertions.assertThat;

public class TestEventStream {
    private final static Logger LOGGER = LoggerFactory.getLogger(TestEventStream.class);

    @Test
    public void testConsume() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<Integer> results = new ConcurrentLinkedQueue<>();
        EventStream<Integer> stream = new EventStream<>(loop);
        ConsumedEventStream<Integer> outStream = stream
                .consume(sleepConsumer(25))
                .consume(results::add)
                .consume(sleepConsumer(25));
        List<Integer> expected = new ArrayList<>();
        int n = 1000;
        for (int i=0;i<=n;i++) {
            expected.add(i);
            stream.expect(i);
            stream.submit(i);
            stream.accept(i);
        }
        stream.close(n);
        LOGGER.info("{}", loop.numEvents());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testSlowConsume() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        EventStream<Integer> stream = new EventStream<>(loop);
        ConsumedEventStream<Integer> outStream = stream
                .consume(sleepConsumer(25));
        int n = 1000;
        for (int i=0;i<=n;i++) {
            stream.expect(i);
            stream.submit(i);
            stream.accept(i);
        }
        stream.close(n);
        LOGGER.info("{}", loop.numEvents());
        assertThat(loop.hasEvents()).isFalse();
    }

    @Test
    public void testDoubleConsume() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<Integer> resultsA = new ConcurrentLinkedQueue<>();
        Collection<Integer> resultsB = new ConcurrentLinkedQueue<>();
        EventStream<Integer> stream = new EventStream<>(loop);
        ConsumedEventStream<Integer> outStream = stream
                .consume(resultsA::add)
                .consume(resultsB::add);
        List<Integer> expected = new ArrayList<>();
        int n = 1000;
        for (int i=0;i<=n;i++) {
            expected.add(i);
            stream.expect(i);
            stream.submit(i);
            stream.accept(i);
        }
        stream.close(n);
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
        ConsumedEventStream<Integer> outStream = stream
                .map(mapper)
                .consume(results::add);
        List<Integer> expected = new ArrayList<>();
        int n = 1000;
        for (int i=0;i<=n;i++) {
            expected.add(mapper.apply(i));
            stream.expect(i);
            stream.submit(i);
            stream.accept(i);
        }
        stream.close(n);
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
        ConsumedEventStream<String> outStream = stream
                .flatMap(mapper)
                .consume(results::add);
        List<String> expected = new ArrayList<>();
        int n = 1000;
        for (int i=0;i<=n;i++) {
            String input = String.format("%d %d %d %d %d", i, i, i, i, i);
            expected.addAll(Streams.stream(mapper.apply(input)).toList());
            stream.expect(i);
            stream.submit(input);
            stream.accept(i);
        }
        stream.close(n);
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testFilter() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<Integer> results = new ConcurrentLinkedQueue<>();
        EventStream<Integer> stream = new EventStream<>(loop);
        Predicate<Integer> predicate = x -> x%2==0;
        ConsumedEventStream<Integer> outStream = stream
                .filter(predicate)
                .consume(results::add);
        List<Integer> expected = new ArrayList<>();
        int n = 1000;
        for (int i=0;i<=n;i++) {
            if (predicate.test(i)) expected.add(i);
            stream.expect(i);
            stream.submit(i);
            stream.accept(i);
        }
        stream.close(n);
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testLongChain() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<Integer> results = new ConcurrentLinkedQueue<>();
        EventStream<String> stream = new EventStream<>(loop);
        Function<String, Iterable<String>> splitMapper = x -> Arrays.stream(x.split(" ")).toList();
        Function<String, Integer> parseIntMapper = Integer::parseInt;
        Function<Integer, Integer> multiplyMapper = x -> x * 5;
        ConsumedEventStream<Integer> outStream = stream
                .consume(sleepConsumer(10))
                .flatMap(splitMapper)
                .map(parseIntMapper)
                .map(multiplyMapper)
                .consume(results::add);
        List<Integer> expected = new ArrayList<>();
        int n = 1000;
        for (int i=0;i<=n;i++) {
            String input = String.format("%d %d %d %d %d", i, i, i, i, i);
            expected.addAll(Streams.stream(splitMapper.apply(input)).map(parseIntMapper).map(multiplyMapper).toList());
            stream.expect(i);
            stream.submit(input);
            stream.accept(i);
        }
        stream.close(n);
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }
}
