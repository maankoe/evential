package maankoe.loop;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventLoop {
    private final static Logger LOGGER = LoggerFactory.getLogger(EventLoop.class);

    private final Collection<Event<?>> events;
    private final ExecutorService executor;

    private boolean running = false;

    public EventLoop() {
        this(5);
    }

    public EventLoop(int numThread) {
        this.events = new ConcurrentLinkedQueue<>();
        this.executor = Executors.newFixedThreadPool(numThread);
    }

    public void run() {
        this.running = true;
        while (this.running()) {
            for (Event<?> event : this.events) {
                if (event.isDone()) {
                    LOGGER.debug("EMIT {}", event);
                    this.emit(event);
                }
            }
        }
    }

    public <T> Event<T> submit(Callable<T> task) {
        LOGGER.debug("SUBMIT {}", task);
        Event<T> event = new Event<>(this.executor.submit(task));
        this.events.add(event);
        return event;
    }

    private <T> void emit(Event<T> event) {
        event.complete();
        this.events.remove(event);
    }

    public void start() {
        Executors.newSingleThreadExecutor().submit(this::run);
    }
    public void stop() {
        this.running = false;
    }

    public boolean running() {
        return this.running;
    }

    public boolean hasEvents() {
        return !this.events.isEmpty();
    }

    public int numEvents() {
        return this.events.size();
    }
}