package counter.java;

import counter.CreditRecord;

class JavaCreditRecord extends CreditRecord {
    private String account;
    private long provisionedCredits;

    public JavaCreditRecord(String account, long provisionedCredits) {
        this.account = account;
        this.provisionedCredits = provisionedCredits;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public long getProvisionedCredits() {
        return provisionedCredits;
    }
}
