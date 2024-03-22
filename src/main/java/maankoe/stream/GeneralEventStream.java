package maankoe.stream;


import maankoe.function.EventFunction;
import maankoe.stream.blocking.EventBlockingStrategy;
import maankoe.stream.blocking.ListenerBlockingStrategy;
import maankoe.stream.submit.SubmitStrategy;
import maankoe.loop.EventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneralEventStream<I, O>
        extends BaseEventStream<O>
        implements EventStreamListener<I> {

    private final static Logger LOGGER = LoggerFactory.getLogger(GeneralEventStream.class);

    private final SubmitStrategy<I, O> submitStrategy;
    private final String name;

    public GeneralEventStream(
            EventLoop loop,
            EventFunction<I, O> function,
            String name
    ) {
        this(
                loop,
                SubmitStrategy.single(
                    loop,
                    function,
                    new ListenerBlockingStrategy(name),
                    new EventBlockingStrategy(name)
                ),
                name
        );
    }

    public GeneralEventStream(
            EventLoop loop,
            SubmitStrategy<I, O> submitStrategy,
            String name
    ) {
        super(loop);
        this.submitStrategy = submitStrategy;
        this.name = name;
    }

    @Override
    public void expect(long index) {
        LOGGER.debug("{}: Expect {}", this.name, index);
        this.submitStrategy.expect(index);
    }

    @Override
    public void submit(I item) {
        LOGGER.debug("{}: Submit {}", this.name, item);
        this.submitStrategy.submit(item, this.listener);
    }

    @Override
    public void submitError(Throwable error) {
        LOGGER.debug("{}: Error {}", this.name, error);
        this.submitStrategy.submitError(error, this.listener);
    }

    @Override
    public void accept(long index) {
        LOGGER.debug("{}: Accept {}", this.name, index);
        this.submitStrategy.accept(index);
    }

    @Override
    public void close(long index) {
        LOGGER.info("{}: Closing stream at index {}", this.name, index);
        this.submitStrategy.close(index, this.listener);
    }
}
