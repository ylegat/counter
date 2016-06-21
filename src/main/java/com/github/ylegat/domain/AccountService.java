package com.github.ylegat.domain;

import static com.github.ylegat.domain.Account.createNewAccount;
import static com.github.ylegat.uncheck.Uncheck.uncheck;
import com.github.ylegat.domain.Account;
import com.github.ylegat.domain.Accounts;
import com.github.ylegat.domain.UnmergeableEventException;

public class AccountService {

    private Accounts accounts;

    public AccountService(Accounts accounts) {
        this.accounts = accounts;
    }

    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    public Account createAccount() {
        Account account = createNewAccount();
        uncheck(() -> accounts.save(account));
        return account;
    }

    public void provisionCredit(String accountId, long credit) {
        Account account = accounts.get(accountId);
        account.provisionCredit(credit);
        uncheck(() -> accounts.save(account));
    }

    public boolean reserveCredit(String accountId, String callId, long credit) {
        while (true) {
            try {
                return tryReserveCredit(accountId, callId, credit);
            } catch (UnmergeableEventException e) {
            }
        }
    }

    public void terminateCall(String accountId, String callId, long consumedCredit) {
        while (true) {
            try {
                tryTerminateCall(accountId, callId, consumedCredit);
                return;
            } catch (UnmergeableEventException e) {
            }
        }
    }

    private boolean tryReserveCredit(String accountId, String callId, long credit) throws UnmergeableEventException {
        Account account = accounts.get(accountId);
        if (!account.reserveCredit(callId, credit)) {
            return false;
        }

        accounts.save(account);
        return true;
    }

    private void tryTerminateCall(String accountId, String callId, long consumedCredit) throws UnmergeableEventException {
        Account account = accounts.get(accountId);
        account.terminateCall(callId, consumedCredit);
        accounts.save(account);
    }
}
