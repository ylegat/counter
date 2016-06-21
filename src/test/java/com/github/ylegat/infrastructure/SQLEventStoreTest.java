package com.github.ylegat.infrastructure;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static com.github.ylegat.uncheck.Uncheck.uncheck;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Ignore;
import org.junit.Test;
import com.github.ylegat.EventSerializer;
import com.github.ylegat.domain.UnmergeableEventException;
import com.github.ylegat.domain.event.Event;

public class SQLEventStoreTest {

    @Test
    public void should_store_and_fetch_event() throws Exception {
        // Given
        SQLEventStore eventStore = eventStore();
        DumbEvent dumbEvent = new DumbEvent(1L);
        saveEvent(eventStore, dumbEvent);

        // When
        List<Event> fetchEvents = loadEvents(eventStore);

        // Then
        assertThat(fetchEvents).containsOnly(dumbEvent);
    }

    @Test
    public void should_merge()  throws Exception {
        // Given
        SQLEventStore eventStore = eventStore();
        DumbEvent dumbEvent1 = new DumbEvent(1L);
        DumbEvent dumbEvent2 = new DumbEvent(1L);
        saveEvent(eventStore, dumbEvent1);

        // When
        saveEvent(eventStore, dumbEvent2);

        // Then
        List<Event> events = loadEvents(eventStore);
        assertThat(events).containsExactly(dumbEvent1, new DumbEvent(2L));
    }

    @Test
    public void should_not_merge() throws Exception {
        // Given
        SQLEventStore eventStore = eventStore(singleton(DumbEvent.dumbEventType));
        DumbEvent dumbEvent1 = new DumbEvent(1L);
        DumbEvent dumbEvent2 = new DumbEvent(1L);

        saveEvent(eventStore, dumbEvent1);

        // When
        Throwable throwable = catchThrowable(() -> saveEvent(eventStore, dumbEvent2));

        // Then
        assertThat(throwable).isInstanceOf(UnmergeableEventException.class);
    }

    @Test
    @Ignore
    public void should_bench_concurrent_call_with_random_conflict() throws Exception {
        SQLEventStore eventStore = eventStore();
        Runnable runnable = () -> {
            for (int i = 0; i < 1000; i++) {
                List<Event> events = loadEvents(eventStore);
                DumbEvent dumbEvent = new DumbEvent(events.size() + 1);
                uncheck(() -> saveEvent(eventStore, dumbEvent));
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        long start = System.currentTimeMillis();
        executorService.submit(runnable);
        executorService.submit(runnable);
        executorService.submit(runnable);
        executorService.submit(runnable);
        executorService.shutdown();
        executorService.awaitTermination(1L, MINUTES);
        long end = System.currentTimeMillis();
        System.out.println("process duration:" + (end - start));
    }

    @Test
    @Ignore
    public void should_bench_concurrent_call_without_random_conflict() throws InterruptedException {
        SQLEventStore eventStore = eventStore();
        Runnable runnable = () -> {
            for (int i = 0; i < 4000; i++) {
                List<Event> events = loadEvents(eventStore);
                DumbEvent dumbEvent = new DumbEvent(events.size() + 1);
                uncheck(() -> saveEvent(eventStore, dumbEvent));
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        long start = System.currentTimeMillis();
        executorService.submit(runnable);
        executorService.shutdown();
        executorService.awaitTermination(1L, MINUTES);
        long end = System.currentTimeMillis();
        System.out.println("process duration:" + (end - start));
    }

    private SQLEventStore eventStore() {
        return new SQLEventStore(new EventSerializer(singletonMap(DumbEvent.dumbEventType, DumbEvent.class)));
    }

    private SQLEventStore eventStore(Set<String> conflictingEvent) {
        return new SQLEventStore(new EventSerializer(singletonMap(DumbEvent.dumbEventType, DumbEvent.class)),
                                 conflictingEvent);
    }

    private List<Event> loadEvents(SQLEventStore eventStore) {
        return eventStore.get(DumbEvent.aggregateId);
    }

    private void saveEvent(SQLEventStore eventStore, DumbEvent dumbEvent) throws UnmergeableEventException {
        eventStore.save(DumbEvent.aggregateId, singletonList(dumbEvent));
    }

    private class DumbEvent extends Event {

        public static final String aggregateId = "aggregateId";
        public static final String dumbEventType = "eventType";

        public DumbEvent(long version) {
            super(aggregateId, dumbEventType, version);
        }

        @Override
        public DumbEvent updateVersion(int incVersion) {
            return new DumbEvent(version + incVersion);
        }
    }


}