package maankoe.stream.submit;

import maankoe.stream.base.EventStreamListener;

public interface CloseStrategy {
    void close(long index, EventStreamListener<?> listener);
}
