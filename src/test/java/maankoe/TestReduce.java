package maankoe;

import maankoe.loop.EventLoop;
import maankoe.stream.base.BaseEventStream;
import maankoe.stream.base.EventStream;
import org.assertj.core.util.Streams;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TestReduce {
    private final static Logger LOGGER = LoggerFactory.getLogger(TestEventStream.class);


//    @Test
//    public void testReduce() {
//        EventLoop loop = new EventLoop();
//        Executors.newSingleThreadExecutor().submit(loop::run);
//        EventStream<Integer> stream = EventStream.create(loop);
//        Reduction<Integer, Integer> result = stream
//                .reduce(Integer::sum, 0);
//        int expected = 0;
//        int n = 10;
//        for (int i=0;i<=n;i++) {
//            expected += i;
//            stream.expect(i);
//            stream.submit(i);
//            stream.accept(i);
//        }
//        stream.close(n);
//        assertThat(result.get()).isEqualTo(expected);
//    }
}
