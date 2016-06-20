package com.github.ylegat.domain;

import com.github.ylegat.domain.event.Event;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public abstract class EventStore {

    public Set<String> conflictingEvents;

    public EventStore(Set<String> conflictingEvents) {
        this.conflictingEvents = conflictingEvents;
    }

    protected abstract boolean save(Event event);

    public boolean save(String aggregateId, List<Event> events) {
        start();
        for (Event event : events) {
            if (save(event)) {
                continue;
            }

            rollback();
            List<Event> missedEvents = get(aggregateId, events.get(0).version);
            if (!isMergeable(events, missedEvents)) {
                System.out.println("not mergeable");
                return false;
            }

            System.out.println("merging");
            return save(aggregateId, events.stream()
                                           .map(e -> e.updateVersion(missedEvents.size()))
                                           .collect(toList()));
        }

        commit();
        return true;
    }

    public List<Event> get(String aggregateId) {
        return get(aggregateId, 1);
    }

    public abstract List<Event> get(String aggregateId, long fromVersion);

    protected abstract void start();

    protected abstract boolean commit();

    protected abstract void rollback();

    public boolean isMergeable(List<? extends Event> events1, List<? extends Event> events2) {
        return events1.stream().filter(event -> conflictingEvents.contains(event.eventType)).count() == 0
                || events2.stream().filter(event -> conflictingEvents.contains(event.eventType)).count() == 0;
    }

}
