package counter;

public abstract class CreditRecord {

    public abstract String getAccount();

    public abstract long getProvisionedCredits();

    @Override
    public String toString() {
        return "Credit [account=" + getAccount() + ", provisionedCredits=" + getProvisionedCredits() + "]";
    }
}