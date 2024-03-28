package maankoe.stream.submit;

import maankoe.stream.base.EventStreamListener;

public interface EventSubmitStrategy<I, O> {
    void submit(I item,  EventStreamListener<O> listener);
}
