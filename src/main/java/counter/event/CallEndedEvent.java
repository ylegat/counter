package counter.event;

public class CallEndedEvent implements Event {

    public static final String CALL_ENDED_EVENT = "CALL_ENDED_EVENT";

    public final String caller;
    public final String account;
    public final long consumedCredit;
    public final long version;
    public final String callId;

    public CallEndedEvent(String account, String callId, String caller, long consumedCredit, long version) {
        this.caller = caller;
        this.account = account;
        this.consumedCredit = consumedCredit;
        this.version = version;
        this.callId = callId;
    }

    @Override
    public String eventType() {
        return CALL_ENDED_EVENT;
    }

    @Override
    public long version() {
        return version;
    }
}
