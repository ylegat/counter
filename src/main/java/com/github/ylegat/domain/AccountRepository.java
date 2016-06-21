package com.github.ylegat.domain;

public interface AccountRepository {

    boolean save(Account account);

    Account get(String accountId);

}
