package maankoe.stream.submit;

import maankoe.stream.base.EventStreamListener;
import maankoe.utilities.IndexGenerator;

public class SingleIdentitySubmitStrategy<O> implements EventSubmitStrategy<O, O> {
    private final IndexGenerator indexGenerator;

    public SingleIdentitySubmitStrategy(
            IndexGenerator indexGenerator
    ) {
        this.indexGenerator = indexGenerator;
    }

    @Override
    public void submit(O item, EventStreamListener<O> listener) {
        long submitIndex = this.indexGenerator.next();
        listener.expect(submitIndex);
        listener.submit(item);
        listener.accept(submitIndex);
    }
}
