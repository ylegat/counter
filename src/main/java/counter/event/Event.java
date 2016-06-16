package counter.event;

public interface Event {

    String eventType();

    long version();
}
