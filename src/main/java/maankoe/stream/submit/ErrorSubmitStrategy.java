package maankoe.stream.submit;

import maankoe.stream.base.EventStreamListener;

public interface ErrorSubmitStrategy<O> {
    void submit(Throwable error, EventStreamListener<O> listener);
}
