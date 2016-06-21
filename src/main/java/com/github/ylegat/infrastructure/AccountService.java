package com.github.ylegat.infrastructure;

import static com.github.ylegat.uncheck.Uncheck.uncheck;
import com.github.ylegat.domain.Account;
import com.github.ylegat.domain.AccountRepository;
import com.github.ylegat.domain.UnmergeableEventException;

public class AccountService {

    private AccountRepository accountRepository;

    public void addCredit(String accountId, long credit) {
        Account account = accountRepository.get(accountId);
        account.provisionCredit(credit);
        uncheck(() -> accountRepository.save(account));
    }

    public boolean reserveCredit(String accountId, String callId, long credit) {
        while (true) {
            try {
                return tryReserveCredit(accountId, callId, credit);
            } catch (UnmergeableEventException e) {
            }
        }
    }

    private boolean tryReserveCredit(String accountId, String callId, long credit) throws UnmergeableEventException {
        Account account = accountRepository.get(accountId);
        if (!account.reserveCredit(callId, credit)) {
            return false;
        }

        accountRepository.save(account);
        return true;
    }

    public void terminateCall(String accountId, String callId, long consumedCredit) {
        Account account = accountRepository.get(accountId);
        account.terminateCall(callId, consumedCredit);
        uncheck(() -> accountRepository.save(account));
    }
}
