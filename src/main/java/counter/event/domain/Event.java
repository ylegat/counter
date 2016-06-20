package counter.event.domain;

import java.util.Objects;

public class Event {

    private int version;

    private String aggregateId;

    private String eventType;

    private String value;

    public Event() {
    }

    public Event(String aggregateId, String eventType, String value, int version) {
        this.aggregateId = aggregateId;
        this.version = version;
        this.eventType = eventType;
        this.value = value;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getValue() {
        return value;
    }

    public int getVersion() {
        return version;
    }

    public String getEventType() {
        return eventType;
    }

    public void updateVersion(int inc) {
        version += inc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return version == event.version &&
                Objects.equals(aggregateId, event.aggregateId) &&
                Objects.equals(eventType, event.eventType) &&
                Objects.equals(value, event.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, aggregateId, eventType, value);
    }

    @Override
    public String toString() {
        return "Event{" +
                "version=" + version +
                ", aggregateId='" + aggregateId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
