package com.github.ylegat.domain;

public class ProvisionedCreditEvent extends Event {

    public static final String PROVISIONED_CREDIT_EVENT = "PROVISIONED_CREDIT_EVENT";

    public final long provisionedCredit;

    public ProvisionedCreditEvent(String accountId, long provisionedCredit, long version) {
        super(accountId, PROVISIONED_CREDIT_EVENT, version);
        this.provisionedCredit = provisionedCredit;
    }

    @Override
    public ProvisionedCreditEvent updateVersion(int incVersion) {
        return new ProvisionedCreditEvent(aggregateId, provisionedCredit, version + incVersion);
    }

    @Override
    public ProvisionedCreditEvent applyTo(Account account) {
        return account.applyProvisionedCreditEvent(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProvisionedCreditEvent that = (ProvisionedCreditEvent) o;
        return provisionedCredit == that.provisionedCredit;
    }

    @Override
    public String toString() {
        return "ProvisionedCreditEvent{" +
                "provisionedCredit=" + provisionedCredit +
                "} " + super.toString();
    }
}
