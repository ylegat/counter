package com.github.ylegat.domain;

public interface Accounts {

    void save(Account account) throws UnmergeableEventException;

    Account get(String accountId);

}
