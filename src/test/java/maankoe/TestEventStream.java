package maankoe;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static maankoe.Utilities.waitForCompletion;
import static org.assertj.core.api.Assertions.assertThat;

public class TestEventStream {
    @Test
    public void testSomething() throws InterruptedException {
        EventLoop loop = new EventLoop();
        Executors.newSingleThreadExecutor().submit(loop::run);

        Collection<Integer> results = new ConcurrentLinkedQueue<>();
        EventStream<Integer> stream = new EventStream<>(loop);
        stream
//                .map(x -> x*2)
                .consume(results::add);
        List<Integer> inputs = new ArrayList<>();
        for (int i=0;i<1000;i++) {
            inputs.add(i);
            stream.addInput(i);
        }
        waitForCompletion(loop);
        assertThat(results).containsExactlyInAnyOrderElementsOf(
                inputs.stream()
//                        .map(x -> x*2)
                        .collect(Collectors.toList())
        );
    }
}
