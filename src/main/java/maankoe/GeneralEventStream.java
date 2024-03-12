package maankoe;


public class GeneralEventStream<I, O>
        extends BaseEventStream<O>
        implements EventStreamListener<I> {

    private final SubmitStrategy<I, O> submitStrategy;

    public GeneralEventStream(
            EventLoop loop,
            EventFunction<I, O> function,
            String name
    ) {
        this(
                loop,
                new SubmitStrategy.Single<>(
                    loop, function, new BlockingStrategy.Expecting(), name
                )
        );
    }

    public GeneralEventStream(
            EventLoop loop,
            SubmitStrategy<I, O> submitStrategy
    ) {
        super(loop);
        this.submitStrategy = submitStrategy;
    }

    public void expect(long index) {
        this.submitStrategy.expect(index);
    }

    public void submit(I item, long index) {
        this.submitStrategy.submit(item, index, this.listener);
    }

    public void accept(long index) {
        this.submitStrategy.accept(index);
    }

    public void close(long index) {
        this.submitStrategy.close(index, this.listener);
    }

    public void block() {
        this.submitStrategy.block();
    }
}
