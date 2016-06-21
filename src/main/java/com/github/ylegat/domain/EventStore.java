package com.github.ylegat.domain;

import static java.util.stream.Collectors.toList;
import java.util.List;
import java.util.Set;
import com.github.ylegat.domain.event.Event;

public abstract class EventStore {

    public Set<String> conflictingEvents;

    public EventStore(Set<String> conflictingEvents) {
        this.conflictingEvents = conflictingEvents;
    }

    protected abstract boolean save(Event event);

    public void save(String aggregateId, List<Event> events) throws UnmergeableEventException {
        for (Event event : events) {
            if (save(event)) {
                continue;
            }

            rollback();
            List<Event> missedEvents = get(aggregateId, events.get(0).version);
            if (!isMergeable(events, missedEvents)) {
                System.out.println("not mergeable");
                throw new UnmergeableEventException();
            }

            System.out.println("merging");
            save(aggregateId, events.stream()
                                    .map(e -> e.updateVersion(missedEvents.size()))
                                    .collect(toList()));
        }

        commit();
    }

    public List<Event> get(String aggregateId) {
        return get(aggregateId, 1);
    }

    public abstract List<Event> get(String aggregateId, long fromVersion);

    protected abstract boolean commit();

    protected abstract void rollback();

    public boolean isMergeable(List<? extends Event> events1, List<? extends Event> events2) {
        return events1.stream().filter(event -> conflictingEvents.contains(event.eventType)).count() == 0
                || events2.stream().filter(event -> conflictingEvents.contains(event.eventType)).count() == 0;
    }

}
