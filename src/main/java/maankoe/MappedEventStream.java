//package maankoe;
//
//import java.util.Collection;
//import java.util.function.Consumer;
//import java.util.function.Function;
//
//public class MappedEventStream<T, O> extends EventStream<T> {
//    private final EventStream<T> stream;
//    private final Function<T, O> mapper;
//
//    private EventStream<O> listener;
//
//    public MappedEventStream(
//            EventStream<T> stream,
//            Function<T, O> mapper
//    ) {
//        this.stream = stream;
//        this.mapper = mapper;
//        this.listener = null;
//    }
//
//    public void addInput(T item) {
//        Event<O> event = loop.submit(() -> mapper.apply(item));
//        event.onComplete(x -> listener.addInput(x));
//    }
//}
