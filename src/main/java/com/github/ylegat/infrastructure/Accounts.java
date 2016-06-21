package com.github.ylegat.infrastructure;

import static com.github.ylegat.domain.Account.loadAccount;
import com.github.ylegat.domain.Account;
import com.github.ylegat.domain.AccountRepository;
import com.github.ylegat.domain.EventStore;
import com.github.ylegat.domain.UnmergeableEventException;

public class Accounts implements AccountRepository {

    private final EventStore eventStore;

    public Accounts(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public void save(Account account) throws UnmergeableEventException {
        eventStore.save(account.getAccountId(), account.consumeEvents());
    }

    @Override
    public Account get(String accountId) {
        return loadAccount(eventStore.get(accountId));
    }

}
