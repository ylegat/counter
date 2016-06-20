package com.github.ylegat.infrastructure;

import com.github.ylegat.EventSerializer;
import com.github.ylegat.domain.event.Event;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SQLEventStoreTest {

    @Test
    public void should_store_and_fetch_event() {
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
    public void should_merge() {
        // Given
        SQLEventStore eventStore = eventStore();
        DumbEvent dumbEvent1 = new DumbEvent(1L);
        DumbEvent dumbEvent2 = new DumbEvent(1L);
        saveEvent(eventStore, dumbEvent1);

        // When
        boolean committed = saveEvent(eventStore, dumbEvent2);

        // Then
        assertThat(committed).isTrue();
        List<Event> events = loadEvents(eventStore);
        assertThat(events).containsExactly(dumbEvent1, new DumbEvent(2L));
    }

    @Test
    public void should_not_merge() {
        // Given
        SQLEventStore eventStore = eventStore(singleton(DumbEvent.dumbEventType));
        DumbEvent dumbEvent1 = new DumbEvent(1L);
        DumbEvent dumbEvent2 = new DumbEvent(1L);

        saveEvent(eventStore, dumbEvent1);

        // When
        boolean committed = saveEvent(eventStore, dumbEvent2);

        // Then
        assertThat(committed).isFalse();
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

    private boolean saveEvent(SQLEventStore eventStore, DumbEvent dumbEvent) {
        return eventStore.save(DumbEvent.aggregateId, singletonList(dumbEvent));
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