package com.tvd12.ezyfoxserver.client.testing.concurrent;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.tvd12.ezyfox.collect.Sets;
import com.tvd12.ezyfox.io.EzyMaps;
import com.tvd12.ezyfox.util.EzyMapBuilder;
import com.tvd12.ezyfox.util.EzyRoundRobin;
import com.tvd12.ezyfox.util.EzyThreads;
import com.tvd12.ezyfoxserver.client.concurrent.EzyEventLoopEvent;
import com.tvd12.ezyfoxserver.client.concurrent.EzyEventLoopGroup;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.reflect.FieldUtil;

public class EzyEventLoopGroupTest {

    @Test
    public void addEventTest() {
        // given
        EzyEventLoopGroup underTest = new EzyEventLoopGroup(2);

        EzyEventLoopEvent event1 = mock(EzyEventLoopEvent.class);
        when(event1.call()).thenReturn(true);
        EzyEventLoopEvent event2 = mock(EzyEventLoopEvent.class);
        when(event2.call()).thenReturn(true);

        // when
        underTest.addEvent(event1);
        underTest.addEvent(event2);
        EzyThreads.sleep(100);

        // then
        EzyRoundRobin<Object> eventLoops = FieldUtil.getFieldValue(
            underTest,
            "eventLoops"
        );
        Map<EzyEventLoopEvent, EzyEventLoopEvent> events1 = FieldUtil.getFieldValue(
            eventLoops.get(),
            "events"
        );
        Asserts.assertEquals(
            Collections.singletonMap(event1, event1),
            events1,
            false
        );
        Map<EzyEventLoopEvent, EzyEventLoopEvent> events2 = FieldUtil.getFieldValue(
            eventLoops.get(),
            "events"
        );
        Asserts.assertEquals(
            Collections.singletonMap(event2, event2),
            events2,
            false
        );
        Map<EzyEventLoopEvent, Object> eventLoopByEvent = FieldUtil.getFieldValue(
            underTest,
            "eventLoopByEvent"
        );
        Asserts.assertEquals(
            eventLoopByEvent,
            EzyMapBuilder
                .mapBuilder()
                .put(event1, eventLoops.get())
                .put(event2, eventLoops.get())
                .build(),
            false
        );
        verify(event1, atLeast(1)).call();
        verify(event2, atLeast(1)).call();
        List<EzyEventLoopEvent> unfinishedEvents = underTest.shutdownAndGet();
        Asserts.assertEquals(
            new HashSet<>(unfinishedEvents),
            Sets.newHashSet(event1, event2)
        );
        underTest.shutdown();
    }

    @Test
    public void addScheduleEventTest() {
        // given
        EzyEventLoopGroup underTest = new EzyEventLoopGroup(2);

        EzyEventLoopEvent event1 = mock(EzyEventLoopEvent.class);
        when(event1.call()).thenReturn(true);
        EzyEventLoopEvent event2 = mock(EzyEventLoopEvent.class);
        when(event2.call()).thenReturn(true);

        // when
        underTest.addScheduleEvent(event1, 1);
        underTest.addScheduleEvent(event2, 10);
        EzyThreads.sleep(100);

        // then
        EzyRoundRobin<Object> eventLoops = FieldUtil.getFieldValue(
            underTest,
            "eventLoops"
        );
        Map<EzyEventLoopEvent, EzyEventLoopEvent> events1 = FieldUtil.getFieldValue(
            eventLoops.get(),
            "events"
        );
        Asserts.assertEquals(
            Collections.singletonMap(event1, event1),
            EzyMaps.newHashMapNewValues(
                events1,
                it -> FieldUtil.getFieldValue(it, "event")
            ),
            false
        );
        Map<EzyEventLoopEvent, EzyEventLoopEvent> events2 = FieldUtil.getFieldValue(
            eventLoops.get(),
            "events"
        );
        Asserts.assertEquals(
            Collections.singletonMap(event2, event2),
            EzyMaps.newHashMapNewValues(
                events2,
                it -> FieldUtil.getFieldValue(it, "event")
            ),
            false
        );
        Map<EzyEventLoopEvent, Object> eventLoopByEvent = FieldUtil.getFieldValue(
            underTest,
            "eventLoopByEvent"
        );
        Asserts.assertEquals(
            eventLoopByEvent,
            EzyMapBuilder
                .mapBuilder()
                .put(event1, eventLoops.get())
                .put(event2, eventLoops.get())
                .build(),
            false
        );
        verify(event1, atLeast(1)).call();
        verify(event2, atLeast(1)).call();
        underTest.shutdown();
    }

    @Test
    public void addOneTimeEventTest() {
        // given
        EzyEventLoopGroup underTest = new EzyEventLoopGroup(2);

        Runnable event1 = mock(Runnable.class);
        Runnable event2 = mock(Runnable.class);

        // when
        underTest.addOneTimeEvent(event1, 1);
        underTest.addOneTimeEvent(event2, 10);
        EzyThreads.sleep(100);

        // then
        EzyRoundRobin<Object> eventLoops = FieldUtil.getFieldValue(
            underTest,
            "eventLoops"
        );
        Map<EzyEventLoopEvent, EzyEventLoopEvent> events1 = FieldUtil.getFieldValue(
            eventLoops.get(),
            "events"
        );
        Asserts.assertEmpty(events1);
        Map<EzyEventLoopEvent, EzyEventLoopEvent> events2 = FieldUtil.getFieldValue(
            eventLoops.get(),
            "events"
        );
        Asserts.assertEmpty(events2);
        Map<EzyEventLoopEvent, Object> eventLoopByEvent = FieldUtil.getFieldValue(
            underTest,
            "eventLoopByEvent"
        );
        Asserts.assertEmpty(eventLoopByEvent);
        verify(event1, times(1)).run();
        verify(event2, times(1)).run();
        underTest.shutdown();
    }

    @Test
    public void addRemoveEventTest() {
        // given
        EzyEventLoopGroup underTest = new EzyEventLoopGroup(2);

        EzyEventLoopEvent event1 = mock(EzyEventLoopEvent.class);
        when(event1.call()).thenReturn(true);
        EzyEventLoopEvent event2 = mock(EzyEventLoopEvent.class);
        when(event2.call()).thenReturn(true);

        // when
        underTest.addScheduleEvent(event1, 1);
        underTest.addEvent(event2);
        EzyThreads.sleep(100);
        underTest.removeEvent(event1);
        underTest.removeEvent(event2);
        EzyThreads.sleep(100);

        // then
        EzyRoundRobin<Object> eventLoops = FieldUtil.getFieldValue(
            underTest,
            "eventLoops"
        );
        Map<EzyEventLoopEvent, EzyEventLoopEvent> events1 = FieldUtil.getFieldValue(
            eventLoops.get(),
            "events"
        );
        Asserts.assertEmpty(events1);
        Map<EzyEventLoopEvent, EzyEventLoopEvent> events2 = FieldUtil.getFieldValue(
            eventLoops.get(),
            "events"
        );
        Asserts.assertEmpty(events2);
        Map<EzyEventLoopEvent, Object> eventLoopByEvent = FieldUtil.getFieldValue(
            underTest,
            "eventLoopByEvent"
        );
        Asserts.assertEmpty(eventLoopByEvent);
        verify(event1, atLeast(1)).call();
        verify(event1, times(1)).onRemoved();
        verify(event2, atLeast(1)).call();
        verify(event2, times(1)).onRemoved();
        underTest.shutdown();
    }
}
