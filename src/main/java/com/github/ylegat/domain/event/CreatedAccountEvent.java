package com.github.ylegat.domain.event;

public class CreatedAccountEvent extends Event {

    public static final String CREATED_ACCOUNT_EVENT = "CREATED_ACCOUNT_EVENT";

    public CreatedAccountEvent(String accountId) {
        super(accountId, CREATED_ACCOUNT_EVENT, 1L);
    }

    @Override
    public CreatedAccountEvent updateVersion(int incVersion) {
        throw new UnsupportedOperationException("account creation should be the first event for an aggregate");
    }

}
