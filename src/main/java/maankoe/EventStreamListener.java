package maankoe;

public interface EventStreamListener<T> {
    class Dummy<T> implements EventStreamListener<T> {
        @Override
        public void addInput(T input) {
            // do nothing
        }
    }

    void addInput(T input);
}
