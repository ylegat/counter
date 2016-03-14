package counter.java;

import counter.CallRecord;

class JavaCallRecord extends CallRecord {
    private String account;
    private String caller;
    private long consumedCredits;

    public JavaCallRecord(String account, String caller, long consumedCredits) {
        this.account = account;
        this.caller = caller;
        this.consumedCredits = consumedCredits;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public String getCaller() {
        return caller;
    }

    @Override
    public long getConsumedCredits() {
        return consumedCredits;
    }
}
