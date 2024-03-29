package maankoe.stream.submit;

import maankoe.stream.base.EventStreamListener;
import maankoe.utilities.IndexGenerator;


public class SingleErrorIdentitySubmitStrategy<O> implements ErrorSubmitStrategy<O> {
    private final IndexGenerator indexGenerator;

    public SingleErrorIdentitySubmitStrategy(
            IndexGenerator indexGenerator
    ) {
        this.indexGenerator = indexGenerator;
    }

    @Override
    public void submit(Throwable error, EventStreamListener<O> listener) {
        long submitIndex = this.indexGenerator.next();
        listener.expect(submitIndex);
        listener.submitError(error);
        listener.accept(submitIndex);
    }
}
