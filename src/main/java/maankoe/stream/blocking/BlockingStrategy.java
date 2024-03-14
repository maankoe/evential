package maankoe.stream.blocking;


public interface BlockingStrategy {
    void expect(long index);
    void accept(long index);
    void close(long index);
    void block();
}
