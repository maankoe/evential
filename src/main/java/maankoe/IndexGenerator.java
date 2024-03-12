package maankoe;

import java.util.concurrent.atomic.AtomicLong;

public class IndexGenerator {

    private final AtomicLong current;

    public IndexGenerator() {
        this.current = new AtomicLong(0);
    }

    public long next() {
        return current.getAndIncrement();
    }
    public long current() {
        return current.get();
    }
}
