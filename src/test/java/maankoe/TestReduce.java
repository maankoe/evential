package maankoe;

import maankoe.loop.EventLoop;
import maankoe.stream.EventStream;
import maankoe.stream.base.BaseEventStream;
import org.assertj.core.util.Streams;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TestReduce {
    private final static Logger LOGGER = LoggerFactory.getLogger(TestEventStream.class);

    @Test
    public void testWindow() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        EventStream<Integer> stream = new EventStream<>(loop);
        Collection<Iterable<Integer>> results = new ConcurrentLinkedQueue<>();
        int windowSize = 5;
        BaseEventStream<Iterable<Integer>> outStream = stream
                .window(windowSize)
                .consume(results::add);
        List<Integer> expected = new ArrayList<>();
        int n = 999;
        for (int i=0;i<=n;i++) {
            expected.add(i);
            stream.expect(i);
            stream.submit(i);
            stream.accept(i);
        }
        stream.close(n);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("{}", results);
        assertThat(results.stream().flatMap(Streams::stream).collect(Collectors.toList()))
                .containsExactlyInAnyOrderElementsOf(expected);
        assertThat(results).hasSize((int) Math.ceil(n / (double) windowSize));
        assertThat(results).allMatch(x -> Streams.stream(x).toList().size() == windowSize);
    }

    @Test
    public void testWindowNoSleep() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        EventStream<Integer> stream = new EventStream<>(loop);
        Collection<Iterable<Integer>> results = new ConcurrentLinkedQueue<>();
        int windowSize = 5;
        BaseEventStream<Iterable<Integer>> outStream = stream
                .window(windowSize)
                .consume(results::add);
        List<Integer> expected = new ArrayList<>();
        int n = 9999;
        for (int i=0;i<=n;i++) {
            expected.add(i);
            stream.expect(i);
            stream.submit(i);
            stream.accept(i);
        }
        stream.close(n);
        LOGGER.info("{}", results);
        assertThat(results.stream().flatMap(Streams::stream).collect(Collectors.toList()))
                .containsExactlyInAnyOrderElementsOf(expected);
        assertThat(results).hasSize((int) Math.ceil(n / (double) windowSize));
        assertThat(results).allMatch(x -> Streams.stream(x).toList().size() == windowSize);
    }

    @Test
    public void testWindowOddSizes() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        EventStream<Integer> stream = new EventStream<>(loop);
        Collection<Iterable<Integer>> results = new ConcurrentLinkedQueue<>();
        int windowSize = 7;
        BaseEventStream<Iterable<Integer>> outStream = stream
                .window(windowSize)
                .consume(results::add);
        List<Integer> expected = new ArrayList<>();
        int n = 999;
        for (int i=0;i<=n;i++) {
            expected.add(i);
            stream.expect(i);
            stream.submit(i);
            stream.accept(i);
        }
        stream.close(n);
        LOGGER.info("{}", results);
        assertThat(results.stream().flatMap(Streams::stream).collect(Collectors.toList()))
                .containsExactlyInAnyOrderElementsOf(expected);
        assertThat(results).hasSize((int) Math.ceil(n / (double) windowSize));
//        assertThat(results).allMatch(x -> Streams.stream(x).toList().size() == windowSize);
    }

//    @Test
//    public void testReduce() {
//        EventLoop loop = new EventLoop();
//        Executors.newSingleThreadExecutor().submit(loop::run);
//        EventStream<Integer> stream = new EventStream<>(loop);
//        Reduction<Integer, Integer> result = stream
//                .reduce(Integer::sum, 0);
//        int expected = 0;
//        int n = 10;
//        for (int i=0;i<=n;i++) {
//            expected += i;
//            stream.expect(i);
//            stream.submit(i);
//            stream.accept(i);
//        }
//        stream.close(n);
//        assertThat(result.get()).isEqualTo(expected);
//    }
}
