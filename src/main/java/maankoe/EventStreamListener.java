package maankoe;

import java.util.concurrent.ExecutionException;

public interface EventStreamListener<T> {
//    void expect(long index);
//    void close();
    void addInput(T input);

    default void expect(long next) {};
//    void block() throws ExecutionException, InterruptedException;

    class Dummy<T> implements EventStreamListener<T> {
//        @Override
//        public void expect(long index) {}

        @Override
        public void addInput(T input) {
            // do nothing
        }

//        @Override
//        public void block() {
//             do nothing
//        }
    }

}
