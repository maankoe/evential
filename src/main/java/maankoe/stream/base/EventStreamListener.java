package maankoe.stream.base;


public interface EventStreamListener<T> {
    void expect(long index);
    void submit(T item);
    void submitError(Throwable error);
    void accept(long index);
    void close(long index);

    class Dummy<T> implements EventStreamListener<T> {
        @Override
        public void expect(long index) {
            // do nothing
        }

        @Override
        public void submit(T item) {
            // do nothing
        }

        @Override
        public void submitError(Throwable error) {
            // do nothing
        }

        @Override
        public void accept(long index) {
            // do nothing
        }

        @Override
        public void close(long index) {
            //do nothing
        }
    }

}
