package com.github.ylegat.domain.event;

import java.util.Objects;

public class TerminatedCallEvent extends Event {

    public static final String TERMINATED_CALL_EVENT = "TERMINATED_CALL_EVENT";

    public final String caller;
    public final long consumedCredit;
    public final String callId;

    public TerminatedCallEvent(String accountId, String callId, String caller, long consumedCredit, long version) {
        super(accountId, TERMINATED_CALL_EVENT, version);
        this.caller = caller;
        this.consumedCredit = consumedCredit;
        this.callId = callId;
    }

    @Override
    public TerminatedCallEvent updateVersion(int incVersion) {
        return new TerminatedCallEvent(aggregateId, callId, caller, consumedCredit, version + incVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TerminatedCallEvent that = (TerminatedCallEvent) o;
        return consumedCredit == that.consumedCredit &&
                Objects.equals(caller, that.caller) &&
                Objects.equals(callId, that.callId);
    }

    @Override
    public String toString() {
        return "TerminatedCallEvent{" +
                "caller='" + caller + '\'' +
                ", consumedCredit=" + consumedCredit +
                ", callId='" + callId + '\'' +
                "} " + super.toString();
    }
}
