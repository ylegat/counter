package counter.event;

public class CreatedAccountEvent implements Event {

    public static final String CREATED_ACCOUNT_EVENT = "CREATED_ACCOUNT_EVENT";

    public final String account;

    public CreatedAccountEvent(String account) {
        this.account = account;
    }

    @Override
    public String eventType() {
        return CREATED_ACCOUNT_EVENT;
    }

    @Override
    public long version() {
        return 1L;
    }
}
