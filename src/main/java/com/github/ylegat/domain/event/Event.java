package com.github.ylegat.domain.event;

import java.util.Objects;

public abstract class Event {

    public final String aggregateId;

    public final String eventType;

    public final long version;

    public Event(String aggregateId, String eventType, long version) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.version = version;
    }

    public abstract Event updateVersion(int incVersion);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return version == event.version &&
                Objects.equals(aggregateId, event.aggregateId) &&
                Objects.equals(eventType, event.eventType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregateId);
    }

    @Override
    public String toString() {
        return "Event{" +
                "aggregateId='" + aggregateId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", version=" + version +
                '}';
    }
}
