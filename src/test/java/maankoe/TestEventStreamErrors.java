package maankoe;

import maankoe.loop.EventLoop;
import maankoe.stream.base.EventStream;
import maankoe.stream.base.BaseEventStream;
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

import static org.assertj.core.api.Assertions.assertThat;


public class TestEventStreamErrors {
    private final static Logger LOGGER = LoggerFactory.getLogger(TestEventStream.class);

    @Test
    public void testConsumeError() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<Integer> results = new ConcurrentLinkedQueue<>();
        Collection<Throwable> errors = new ConcurrentLinkedQueue<>();
        EventStream<Integer> stream = EventStream.create(loop);
        BaseEventStream<Integer> outStream = stream
                .consume(results::add)
                .consumeError(errors::add)
                .consume(results::add);
        List<Throwable> expected = new ArrayList<>();
        int n = 10;
        for (int i=0;i<=n;i++) {
            Throwable error = new Exception(String.format("ERROR: %d", i));
            expected.add(error);
            stream.expect(i);
            stream.submitError(error);
            stream.accept(i);
        }
        stream.close(n);
        LOGGER.info("{}", loop.numEvents());
        assertThat(errors).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(results).isEmpty();
    }

     @Test
    public void testConsumeThrownError() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<Integer> results = new ConcurrentLinkedQueue<>();
        Collection<String> errors = new ConcurrentLinkedQueue<>();
        EventStream<Integer> stream = EventStream.create(loop);
        Function<Integer, String> errorGenerator =
                x -> String.format("ERROR: %d", x);
        Function<Integer, Integer> mapping = x -> {
            throw new IllegalStateException(errorGenerator.apply(x));
        };
        BaseEventStream<Integer> outStream = stream
                .map(mapping)
                .consumeError(e -> errors.add(e.getMessage()))
                .consume(results::add);
        List<String> expected = new ArrayList<>();
        int n = 10;
        for (int i=0;i<=n;i++) {
            expected.add(errorGenerator.apply(i));
            stream.expect(i);
            stream.submit(i);
            stream.accept(i);
        }
        stream.close(n);
        LOGGER.info("{}", loop.numEvents());
        assertThat(errors).allMatch(x -> expected.stream().anyMatch(x::contains));
        assertThat(results).isEmpty();
    }

    @Test
    public void testFlatMapForwardsError() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<String> results = new ConcurrentLinkedQueue<>();
        Collection<Throwable> errors = new ConcurrentLinkedQueue<>();
        EventStream<String> stream = EventStream.create(loop);
        Function<String, Iterable<String>> mapper =
                x -> Arrays.stream(x.split(" ")).toList();
        BaseEventStream<String> outStream = stream
                .flatMap(mapper)
                .consumeError(errors::add)
                .consume(results::add);
        List<Throwable> expected = new ArrayList<>();
        int n = 10;
        for (int i=0;i<=n;i++) {
            Throwable error = new Exception(String.format("ERROR: %d", i));
            expected.add(error);
            stream.expect(i);
            stream.submitError(error);
            stream.accept(i);
        }
        stream.close(n);
        LOGGER.info("{}", loop.numEvents());
        assertThat(errors).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(results).isEmpty();
    }

    @Test
    public void testFlatMapThrowsError() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<Integer> results = new ConcurrentLinkedQueue<>();
        Collection<String> errors = new ConcurrentLinkedQueue<>();
        EventStream<Integer> stream = EventStream.create(loop);
        Function<Integer, String> errorGenerator =
                x -> String.format("ERROR: %d", x);
        Function<Integer, Iterable<Integer>> mapping = x -> {
            throw new IllegalStateException(errorGenerator.apply(x));
        };
        BaseEventStream<Integer> outStream = stream
                .flatMap(mapping)
                .consumeError(e -> errors.add(e.getMessage()))
                .consume(results::add);
        List<String> expected = new ArrayList<>();
        int n = 10;
        for (int i=0;i<=n;i++) {
            expected.add(errorGenerator.apply(i));
            stream.expect(i);
            stream.submit(i);
            stream.accept(i);
        }
        stream.close(n);
        LOGGER.info("{}", loop.numEvents());
        assertThat(errors).allMatch(x -> expected.stream().anyMatch(x::contains));
        assertThat(results).isEmpty();
    }

    @Test
    public void testErrorMapToSuccess() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<Integer> results = new ConcurrentLinkedQueue<>();
        EventStream<Integer> stream = EventStream.create(loop);
        Function<Throwable, Integer> mapping = x -> Integer.parseInt(x.getMessage());
        BaseEventStream<Integer> outStream = stream
                .mapError(mapping)
                .consume(results::add);
        List<Integer> expected = new ArrayList<>();
        int n = 10;
        for (int i=0;i<=n;i++) {
            Throwable error = new Exception(Integer.toString(i));
            expected.add(mapping.apply(error));
            stream.expect(i);
            stream.submitError(error);
            stream.accept(i);
        }
        stream.close(n);
        LOGGER.info("{}", loop.numEvents());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testItermittentError() {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);
        Collection<Integer> results = new ConcurrentLinkedQueue<>();
        Collection<Throwable> errors = new ConcurrentLinkedQueue<>();
        EventStream<Integer> stream = EventStream.create(loop);
        BaseEventStream<Integer> outStream = stream
                .consumeError(errors::add)
                .consume(results::add);
        List<Integer> expected = new ArrayList<>();
        List<Throwable> expectedErrors = new ArrayList<>();
        int n = 10;
        for (int i=0;i<=n;i++) {
            stream.expect(i);
            if (i % 2 == 0) {
                Throwable error = new Exception(Integer.toString(i));
                stream.submitError(error);
                expectedErrors.add(error);
            } else {
                stream.submit(i);
                expected.add(i);
            }
            stream.accept(i);
        }
        stream.close(n);
        LOGGER.info("{}", loop.numEvents());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(errors).containsExactlyInAnyOrderElementsOf(expectedErrors);
    }
}
