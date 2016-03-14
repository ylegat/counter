package counter;

public abstract class CallRecord {

    public abstract String getAccount();

    public abstract String getCaller();

    public abstract long getConsumedCredits();

    @Override
    public String toString() {
        return "Call [account=" + getAccount() + ", caller=" + getCaller() + ", consumedCredits=" + getConsumedCredits() + "]";
    }
}