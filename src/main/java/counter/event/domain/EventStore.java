package counter.event.domain;

import java.util.List;

public abstract class EventStore {

    protected abstract boolean save(Event event);

    public boolean save(String aggregateId, List<Event> events) {
        start();
        for (Event event : events) {
            if (save(event)) {
                continue;
            }

            rollback();
            List<Event> missedEvents = get(aggregateId, events.get(0).getVersion());
            if (!isMergeable(events, missedEvents)) {
                System.out.println("not mergeable");
                return false;
            }

            System.out.println("merging");
            events.forEach(e -> e.updateVersion(missedEvents.size()));
            return save(aggregateId, events);
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

    public boolean isMergeable(List<Event> events1, List<Event> events2) {
        return events1.stream().filter(event -> event.getEventType().equals("RESERVATION")).count() == 0
                || events2.stream().filter(event -> event.getEventType().equals("RESERVATION")).count() == 0;
    }

}
