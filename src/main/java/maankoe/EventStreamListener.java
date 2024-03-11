package maankoe;


public interface EventStreamListener<T> {
    default void expect(long index) {}
    default void submit(T item) {this.submit(item, (int) item);}
    default void submit(T item, long index) {}
    default void accept(long index) {}
    default void close(long index) {}
    default void block() {}

    class Dummy<T> implements EventStreamListener<T> {
        @Override
        public void expect(long index) {
            // do nothing
        }

        @Override
        public void submit(T item, long index) {
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
