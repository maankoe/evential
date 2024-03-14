package maankoe.utilities;

import java.util.concurrent.atomic.AtomicLong;

public class IndexGenerator {

    private final AtomicLong current;

    public IndexGenerator() {
        this.current = new AtomicLong(-1);
    }

    public long next() {
        return current.incrementAndGet();
    }
    public long current() {
        return current.get();
    }
}
