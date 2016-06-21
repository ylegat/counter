package com.github.ylegat.domain;

public interface AccountRepository {

    void save(Account account) throws UnmergeableEventException;

    Account get(String accountId);

}
