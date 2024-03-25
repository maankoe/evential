//package maankoe.stream.reduce;
//
//import maankoe.loop.Event;
//import maankoe.loop.EventLoop;
//import maankoe.stream.base.BaseEventStream;
//import maankoe.stream.base.EventStreamListener;
//import maankoe.utilities.Optional;
//
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.concurrent.atomic.AtomicReferenceArray;
//import java.util.function.BiFunction;
//
//public class Reduction<I, O>
//        extends BaseEventStream<O>
//        implements EventStreamListener<I> {
//
//    private final BiFunction<I, O, O> function;
//
//    public Reduction(
//            EventLoop loop,
//            BiFunction<I, O, O> function,
//            O identity
//    ) {
//        super(loop);
//        this.function = function;
//    }
//
//    @Override
//    public void expect(long index) {
//
//    }
//
//    @Override
//    public void submit(I item) {
//        AtomicReferenceArray
//        Event<Optional<O>> event = loop.submit(() -> this.function.apply(item));
//        this.eventBlockingStrategy.submit(event);
//        listener.expect(submitIndex);
//        event.onSuccess(ox -> ox.ifPresent(listener::submit));
//        event.onError(listener::submitError);
//    }
//
//    @Override
//    public void submitError(Throwable throwable) {
//
//    }
//
//    @Override
//    public void accept(long index) {
//
//    }
//
//    @Override
//    public void close(long index) {
//
//    }
//
//    public O get() {
//        return null;
//    }
//}
