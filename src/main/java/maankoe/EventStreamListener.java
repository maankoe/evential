package maankoe;


public interface EventStreamListener<T> {
    void expect(long index);
    void submit(T item);
    void accept(long index);
    void close(long index);
    void block();

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
        public void accept(long index) {
            // do nothing
        }

        @Override
        public void close(long index) {
            //do nothing
        }

        @Override
        public void block() {
            // do nothing
        }
    }

}
