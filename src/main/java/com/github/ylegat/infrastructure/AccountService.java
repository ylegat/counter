package com.github.ylegat.infrastructure;

import com.github.ylegat.domain.Account;
import com.github.ylegat.domain.AccountRepository;

public class AccountService {

    private AccountRepository accountRepository;

    public void addCredit(String accountId, long credit) {
        Account account = accountRepository.get(accountId);
        account.provisionCredit(credit);
        accountRepository.save(account);
    }

    public boolean reserveCredit(String accountId, String callId, long credit) {
        Account account = accountRepository.get(accountId);
        if (!account.reserveCredit(callId, credit)) {
            return false;
        }

        return accountRepository.save(account);
    }

    public void terminateCall(String accountId, String callId, long consumedCredit) {
        Account account = accountRepository.get(accountId);
        account.terminateCall(callId, consumedCredit);
        accountRepository.save(account);
    }
}
