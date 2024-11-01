package maankoe.stream.reduce;

import maankoe.loop.Event;
import maankoe.loop.EventLoop;
import maankoe.stream.base.BaseEventStream;
import maankoe.stream.base.EventStreamListener;
import maankoe.stream.base.WindowedEventStream;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.stream.submit.ErrorSubmitStrategy;
import maankoe.stream.submit.SingleErrorIdentitySubmitStrategy;
import maankoe.utilities.IndexGenerator;
import maankoe.utilities.LimitedCollection;
import org.assertj.core.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;

public class Reduction<I>
        extends BaseEventStream<I>
        implements EventStreamListener<I> {

    private final static Logger LOGGER = LoggerFactory.getLogger(Reduction.class);

    private final IndexGenerator indexGenerator;
    private final BinaryOperator<I> function;
    private final WindowedEventStream<I> submitWindow;
    private final WindowedEventStream<I> reduceWindow;
    private final String name;
    private final CompletableFuture<Long> reduceCloseIndex = new CompletableFuture<>();
    private I result;
    private AtomicReference<Throwable> error = new AtomicReference<>();

    public Reduction(
            EventLoop loop,
            BinaryOperator<I> function,
            String name,
            IndexGenerator indexGenerator
     ) {
        super(loop);
        this.indexGenerator = indexGenerator;
        this.submitWindow = WindowedEventStream.create(loop, 2, String.format("%s-submitWindow", name));
        this.reduceWindow = WindowedEventStream.create(loop, 2, String.format("%s-reduceWindow", name));
        this.function = function;
        this.name = name;
        this.result = null;
        this.submitWindow.consume(this::reduce);
        this.reduceWindow.consume(this::reduce);
    }

    public static <I> Reduction<I> create(EventLoop loop, BinaryOperator<I> function, String name) {
        IndexGenerator indexGenerator = new IndexGenerator();
        return new Reduction<>(loop, function, name, indexGenerator);
    }

    public void reduce(Iterable<I> items) {
        List<I> lst = Streams.stream(items).toList();
        long index = indexGenerator.next();
        try {
            I result = lst.stream().reduce(this.function).get();
            LOGGER.info("{}: ReduceCalc {}/{} size={} {} {}",
                    this.name, index, this.reduceCloseIndex.getNow(-1L),
                    lst.size(), lst, result);
            if (index <= this.reduceCloseIndex.getNow(Long.MAX_VALUE)) {
                this.reduceWindow.expect(index);
                this.reduceWindow.submit(result);
                this.reduceWindow.accept(index);
            } else {
                LOGGER.info("{}: ReduceDone {} : {}", this.name, result, index);
                this.result = result;
                this.listener.expect(0);
                this.listener.submit(result);
                this.listener.accept(0);
            }
        } catch (Throwable e) {
            this.error.compareAndSet(null, e);
            this.listener.submitError(e);
        }
    }

    @Override
    public void expect(long index) {
        LOGGER.info("{}: Expect {}", this.name, index);
        this.submitWindow.expect(index);
    }

    @Override
    public void submit(I item) {
        LOGGER.info("{}: Submit {}", this.name, item);
        this.submitWindow.submit(item);
    }

    @Override
    public void submitError(Throwable error) {
        LOGGER.info("{}: submitError {}", this.name, error);
        this.submitWindow.submitError(error);
    }

    @Override
    public void accept(long index) {
        LOGGER.info("{}: accept {}", this.name, index);
        this.submitWindow.accept(index);
    }

    @Override
    public void close(long index) {
        long reduceCloseIndex = index % 2 == 0 ? index - 1 : index - 2;
        this.reduceCloseIndex.complete(reduceCloseIndex);
        this.submitWindow.close(index);
        this.reduceWindow.close(reduceCloseIndex);
        LOGGER.info("{} done {}", this.name, this.indexGenerator.current());
    }

    public Optional<Throwable> getError() {
        return Optional.ofNullable(this.error.get());
    }

    public I get() {
        return this.result;
    }
}
