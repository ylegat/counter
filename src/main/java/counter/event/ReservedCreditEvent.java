package counter.event;

public class ReservedCreditEvent implements Event {

    public static final String RESERVED_CREDIT_EVENT = "RESERVED_CREDIT_EVENT";

    public final String account;

    public final String callId;

    public final long reservedCredit;

    public final long version;

    public ReservedCreditEvent(String account, String callId, long reservedCredit, long version) {
        this.callId = callId;
        this.account = account;
        this.reservedCredit = reservedCredit;
        this.version = version;
    }

    @Override
    public String eventType() {
        return RESERVED_CREDIT_EVENT;
    }

    @Override
    public long version() {
        return version;
    }
}
