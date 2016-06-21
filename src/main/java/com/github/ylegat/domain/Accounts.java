package com.github.ylegat.domain;

import static com.github.ylegat.domain.Account.loadAccount;

public class Accounts {

    private final EventStore eventStore;

    public Accounts(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void save(Account account) throws UnmergeableEventException {
        eventStore.save(account.getAccountId(), account.consumeEvents());
    }

    public Account get(String accountId) {
        return loadAccount(eventStore.get(accountId));
    }

    public void refresh(Account account) {
        account.applyEvents(eventStore.get(account.getAccountId(), account.version() + 1));
    }

}