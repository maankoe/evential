package maankoe;


public class GeneralEventStream<I, O>
        extends BaseEventStream<O>
        implements EventStreamListener<I> {

    private final BlockingStrategy blockingStrategy;
    private final SubmitStrategy<I, O> submitStrategy;

    public GeneralEventStream(
            EventLoop loop,
            EventFunction<I, O> function,
            String name
    ) {
        super(loop);
        this.blockingStrategy = new BlockingStrategy.Expecting();
        this.submitStrategy = new SubmitStrategy.Single<>(
                loop, function, this.blockingStrategy, name
        );
    }

    public void expect(long index) {
        this.blockingStrategy.expect(index);
    }

    public void submit(I item, long index) {
        this.submitStrategy.submit(item, index, this.listener);
    }

    public void accept(long index) {
        this.blockingStrategy.accept(index);
    }

    public void close(long index) {
        this.submitStrategy.close(index, this.listener);
    }

    public void block() {
        this.blockingStrategy.block();
    }
}
