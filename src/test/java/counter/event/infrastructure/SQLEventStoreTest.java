package counter.event.infrastructure;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.junit.Test;
import counter.event.domain.Event;

public class SQLEventStoreTest {

    @Test
    public void should_store_and_fetch_event() {
        // Given
        SQLEventStore eventStore = new SQLEventStore();
        Event event = new Event("aggregateId", "eventType", "value", 1);
        eventStore.save("aggregateId", singletonList(event));

        // When
        List<Event> events = eventStore.get("aggregateId");

        // Then
        assertThat(events).containsExactly(event);
    }

    @Test
    public void should_merge() {
        // Given
        SQLEventStore eventStore = new SQLEventStore();
        Event event1 = new Event("aggregateId", "eventType", "value1", 1);
        Event event2 = new Event("aggregateId", "eventType", "value2", 1);
        eventStore.save("aggregateId", singletonList(event1));

        // When
        boolean committed = eventStore.save("aggregateId", singletonList(event2));

        // Then
        assertThat(committed).isTrue();
        List<Event> events = eventStore.get("aggregateId");
        assertThat(events).containsExactly(event1, new Event("aggregateId", "eventType", "value2", 2));
    }

    @Test
    public void should_not_merge() {
        // Given
        SQLEventStore eventStore = new SQLEventStore();
        Event event1 = new Event("aggregateId", "RESERVATION", "value1", 1);
        Event event2 = new Event("aggregateId", "RESERVATION", "value2", 1);
        eventStore.save("aggregateId", singletonList(event1));

        // When
        boolean committed = eventStore.save("aggregateId", singletonList(event2));

        // Then
        assertThat(committed).isFalse();
    }

}