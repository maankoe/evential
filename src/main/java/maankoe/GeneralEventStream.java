package maankoe;


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
                new SubmitStrategy.Single<>(
                    loop, function, new BlockingStrategy.Expecting(name)
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

    public void expect(long index) {
        LOGGER.debug("{}: Expect {}", this.name, index);
        this.submitStrategy.expect(index);
    }

    public void submit(I item) {
        LOGGER.debug("{}: Submit {}", this.name, item);
        this.submitStrategy.submit(item, this.listener);
    }

    public void accept(long index) {
        LOGGER.debug("{}: Accept {}", this.name, index);
        this.submitStrategy.accept(index);
    }

    public void close(long index) {
        LOGGER.info("{}: Closing stream at index {}", this.name, index);
        this.submitStrategy.close(index, this.listener);
    }

    public void block() {
        LOGGER.info("{}: Blocking stream", this.name);
        this.submitStrategy.block();
    }
}
