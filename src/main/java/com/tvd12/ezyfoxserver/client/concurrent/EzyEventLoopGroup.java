package com.tvd12.ezyfoxserver.client.concurrent;

import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfox.util.EzyRoundRobin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class EzyEventLoopGroup {

    private final ExecutorService executorService;
    private final EzyRoundRobin<EventLoop> eventLoops;
    private final Map<EzyEventLoopEvent, EventLoop> eventLoopByEvent;

    public static final int DEFAULT_MAX_SLEEP_TIME = 3;

    public EzyEventLoopGroup(int numberOfThreads) {
        this(
            numberOfThreads,
            EzyNettyThreadFactory.create("ezy-event-loop")
        );
    }

    public EzyEventLoopGroup(
        int numberOfThreads,
        ThreadFactory threadFactory
    ) {
        this(
            DEFAULT_MAX_SLEEP_TIME,
            numberOfThreads,
            threadFactory
        );
    }

    public EzyEventLoopGroup(
        int maxSleepTime,
        int numberOfThreads,
        ThreadFactory threadFactory
    ) {
        eventLoopByEvent = new ConcurrentHashMap<>();
        eventLoops = new EzyRoundRobin<>(
            () -> new EventLoop(maxSleepTime),
            numberOfThreads
        );
        executorService = Executors.newFixedThreadPool(
            numberOfThreads,
            threadFactory
        );
        for (int i = 0; i < numberOfThreads; ++i) {
            executorService.execute(
                () -> eventLoops.get().start()
            );
        }
    }

    public void addEvent(EzyEventLoopEvent event) {
        final EventLoop eventLoop = eventLoops.get();
        eventLoopByEvent.put(
            event instanceof ScheduledEvent
                ? ((ScheduledEvent) event).event
                : event,
            eventLoop
        );
        eventLoop.addEvent(event);
    }

    public void addScheduleEvent(
        EzyEventLoopEvent event,
        long period
    ) {
        addScheduleEvent(event, 0, period);
    }

    public void addScheduleEvent(
        EzyEventLoopEvent event,
        long delayTime,
        long period
    ) {
        addEvent(new ScheduledEvent(event, delayTime, period));
    }

    public void addOneTimeEvent(
        Runnable event,
        long delayTime
    ) {
        addEvent(
            new ScheduledEvent(
                () -> {
                    event.run();
                    return false;
                },
                delayTime,
                delayTime
            )
        );
    }

    public void removeEvent(EzyEventLoopEvent event) {
        final EventLoop eventLoop = eventLoopByEvent.remove(event);
        if (eventLoop != null) {
            eventLoop.removeEvent(event);
        }
    }

    public void stop() {
        eventLoops.forEach(EventLoop::stop);
        executorService.shutdown();
    }

    private static final class EventLoop extends EzyLoggable {

        private volatile boolean active;
        private final int maxSleepTime;
        private final Map<EzyEventLoopEvent, EzyEventLoopEvent> events;

        private EventLoop(int maxSleepTime) {
            this.maxSleepTime = maxSleepTime;
            this.events = new ConcurrentHashMap<>();
        }

        public void addEvent(EzyEventLoopEvent event) {
            events.put(
                event instanceof ScheduledEvent
                    ? ((ScheduledEvent) event).event
                    : event,
                event
            );
        }

        public void removeEvent(EzyEventLoopEvent event) {
            events.remove(
                event instanceof ScheduledEvent
                    ? ((ScheduledEvent) event).event
                    : event
            );
        }

        public void start() {
            active = true;
            final List<EzyEventLoopEvent> eventBuffers = new ArrayList<>();
            while (active) {
                final long startTime = System.currentTimeMillis();
                eventBuffers.addAll(events.values());
                for (EzyEventLoopEvent event : eventBuffers) {
                    try {
                        if (event instanceof ScheduledEvent) {
                            final ScheduledEvent scheduledEvent = (ScheduledEvent) event;
                            if (scheduledEvent.isNotFireTime()) {
                                continue;
                            }
                        }
                        if (!event.fire()) {
                            removeEvent(event);
                            event.onFinished();
                        }
                    } catch (Throwable e) {
                        logger.error("fatal error on event loop with event: {}", event, e);
                    }
                }
                eventBuffers.clear();
                final long elapsedTime = System.currentTimeMillis() - startTime;
                final long sleepTime = maxSleepTime - elapsedTime;
                if (sleepTime > 0) {
                    try {
                        //noinspection BusyWait
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }

        public void stop() {
            active = false;
        }
    }

    private static final class ScheduledEvent implements EzyEventLoopEvent {
        private final long period;
        private final EzyEventLoopEvent event;
        private final AtomicLong nextFireTime = new AtomicLong();

        private ScheduledEvent(
            EzyEventLoopEvent event,
            long delayTime,
            long period
        ) {
            this.period = period;
            this.event = event;
            this.nextFireTime.set(
                System.currentTimeMillis() + (delayTime <= 0 ? 0 : period)
            );
        }

        public boolean isNotFireTime() {
            return System.currentTimeMillis() < nextFireTime.get();
        }

        @Override
        public boolean fire() {
            this.nextFireTime.addAndGet(period);
            return event.fire();
        }
    }
}
