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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public class Reduction<I>
        extends BaseEventStream<I>
        implements EventStreamListener<I> {

    private final static Logger LOGGER = LoggerFactory.getLogger(Reduction.class);

    private final IndexGenerator indexGenerator;
    private final ListenerBlockingStrategy listenerBlockingStrategy;
    private final EventBlockingStrategy eventBlockingStrategy;
    private final ErrorSubmitStrategy<I> errorSubmitStrategy;

    private final IndexGenerator reductionIndexGenerator;
    private final ListenerBlockingStrategy reductionBlockingStrategy;

    private final AtomicReference<LimitedCollection<I>> current;
    private final BinaryOperator<I> function;
    private final AtomicReference<I> result;

    public Reduction(
            EventLoop loop,
            IndexGenerator indexGenerator,
            ListenerBlockingStrategy listenerBlockingStrategy,
            EventBlockingStrategy eventBlockingStrategy,
            BinaryOperator<I> function,
            ErrorSubmitStrategy<I> errorSubmitStrategy
    ) {
        super(loop);
        this.function = function;
        this.listenerBlockingStrategy = listenerBlockingStrategy;
        this.eventBlockingStrategy = eventBlockingStrategy;
        this.indexGenerator = indexGenerator;
        this.current = new AtomicReference<>(new LimitedCollection<>(2));
        this.errorSubmitStrategy = errorSubmitStrategy;

        this.reductionIndexGenerator = new IndexGenerator();
        this.reductionBlockingStrategy = new ListenerBlockingStrategy("REDUCTION");
        this.result = new AtomicReference<>(null);
    }

    public static <I> Reduction<I> create(
            EventLoop loop,
            BinaryOperator<I> function,
            String name
    ) {
        IndexGenerator indexGenerator = new IndexGenerator();
        ListenerBlockingStrategy listenerBlockingStrategy = new ListenerBlockingStrategy(name);
        EventBlockingStrategy eventBlockingStrategy = new EventBlockingStrategy(name);
        return new Reduction<>(
                loop,
                indexGenerator,
                listenerBlockingStrategy,
                eventBlockingStrategy,
                function,
                new SingleErrorIdentitySubmitStrategy<>(indexGenerator)
        );

    }

    @Override
    public void expect(long index) {
        this.listenerBlockingStrategy.expect(index);
    }

    @Override
    public void submit(I item) {
        this.submitOne(item, "SUBMIT");
    }

    public void submitOnReduce(I item) {
        this.submitOne(item, "SUBMIT_REDUCE");
        while (!this.result.compareAndSet(this.result.get(), item)) {}
    }

    private void submitOne(I item, String source) {
        LimitedCollection<I> attempt = this.current.get();
        LOGGER.info("{} {}, current: {}", source, item, attempt);
        while (!attempt.add(item)) {
            LOGGER.info("{} FAILED {}, current: {}", source, item, attempt);
            this.current.compareAndSet(attempt, new LimitedCollection<>(2));
            attempt.get().ifPresent(this::submit);
            attempt = current.get();
        }
        LOGGER.info("{} UPDATE {}, current: {}", source, item, attempt);
    }
//submit \[\d+\]
    private void submit(Collection<I> items) {
        LOGGER.info("{}: Submit {}", "REDUCE", items);
        Event<Optional<I>> event = loop.submit(() -> {
            Optional<I> value = items.stream().reduce(this.function);
            LOGGER.info("REDUCE {} = {}", items, value.get());
            return value;
        });
        this.eventBlockingStrategy.submit(event);
        long reductionIndex = this.reductionIndexGenerator.next();
        this.reductionBlockingStrategy.expect(reductionIndex);
        event.onSuccess(x -> {
            x.ifPresentOrElse(this::submitOnReduce, () -> LOGGER.info("{}", 1/0));
        });
        event.onComplete(x -> this.reductionBlockingStrategy.accept(reductionIndex));

//        long submitIndex = this.indexGenerator.next();
//        listener.expect(submitIndex);
//        event.onSuccess(listener::submit);
//        event.onError(listener::submitError);
//        event.onComplete(orx -> listener.accept(submitIndex));
    }

    @Override
    public void submitError(Throwable error) {
        this.errorSubmitStrategy.submit(error, this.listener);
    }

    @Override
    public void accept(long index) {
        this.listenerBlockingStrategy.accept(index);
    }

    @Override
    public void close(long index) {
        LOGGER.info("{}: Closing stream at index {}", "REDUCE", index);
        this.listenerBlockingStrategy.close(index);
        this.listenerBlockingStrategy.block();

//        try {
//            Thread.sleep(4000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        this.current.get().get()
                .filter(x -> !x.isEmpty())
                .ifPresent(this::submit);

        this.reductionBlockingStrategy.close(index-1);
        this.reductionBlockingStrategy.block();
        this.eventBlockingStrategy.block();
        listener.close(this.indexGenerator.current());
    }

    public I get() {
        return this.result.get();
    }
}
