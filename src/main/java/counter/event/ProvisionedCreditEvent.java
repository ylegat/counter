package counter.event;

public class ProvisionedCreditEvent implements Event {

    public static final String PROVISIONED_CREDIT_EVENT = "PROVISIONED_CREDIT_EVENT";

    public final String account;

    public final long provisionedCredit;

    public final long version;

    public ProvisionedCreditEvent(String account, long provisionedCredit, long version) {
        this.account = account;
        this.provisionedCredit = provisionedCredit;
        this.version = version;
    }

    @Override
    public String eventType() {
        return PROVISIONED_CREDIT_EVENT;
    }

    @Override
    public long version() {
        return version;
    }
}
