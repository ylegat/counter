package com.github.ylegat.domain;

import java.util.Objects;

public class ReservedCreditEvent extends Event {

    public static final String RESERVED_CREDIT_EVENT = "RESERVED_CREDIT_EVENT";

    public final String callId;

    public final long reservedCredit;

    public ReservedCreditEvent(String accountId, String callId, long reservedCredit, long version) {
        super(accountId, RESERVED_CREDIT_EVENT, version);
        this.callId = callId;
        this.reservedCredit = reservedCredit;
    }

    @Override
    public ReservedCreditEvent updateVersion(int incVersion) {
        return new ReservedCreditEvent(aggregateId, callId, reservedCredit, version + incVersion);
    }

    @Override
    public ReservedCreditEvent applyTo(Account account) {
        return account.applyReservedCreditEvent(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ReservedCreditEvent that = (ReservedCreditEvent) o;
        return reservedCredit == that.reservedCredit &&
                Objects.equals(callId, that.callId);
    }

    @Override
    public String toString() {
        return "ReservedCreditEvent{" +
                "callId='" + callId + '\'' +
                ", reservedCredit=" + reservedCredit +
                "} " + super.toString();
    }
}
