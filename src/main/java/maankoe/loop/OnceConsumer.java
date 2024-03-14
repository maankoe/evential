package maankoe.loop;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class OnceConsumer<T> implements Consumer<T> {
    private final Consumer<T> base;
    private final AtomicBoolean consumed;

    public OnceConsumer(Consumer<T> base) {
        this.base = base;
        this.consumed = new AtomicBoolean(false);
    }

    @Override
    public void accept(T t) {
        if (this.consumed.compareAndSet(false, true)) {
            this.base.accept(t);
        }
    }
}
