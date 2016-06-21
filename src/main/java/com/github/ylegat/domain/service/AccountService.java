package com.github.ylegat.domain.service;

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
        return retryUntilMerged(accountId, account -> tryReserveCredit(account, callId, credit));
    }

    public boolean terminateCall(String accountId, String callId, long consumedCredit) {
        return retryUntilMerged(accountId, account -> tryTerminateCall(account, callId, consumedCredit));
    }

    private boolean tryReserveCredit(Account account, String callId, long credit) throws UnmergeableEventException {
        if (!account.reserveCredit(callId, credit)) {
            return false;
        }

        accounts.save(account);
        return true;
    }

    private boolean tryTerminateCall(Account account,
                                     String callId,
                                     long consumedCredit) throws UnmergeableEventException {
        account.terminateCall(callId, consumedCredit);
        accounts.save(account);
        return true;
    }

    private boolean retryUntilMerged(String accountId, MergeableProcess process) {
        Account account = accounts.get(accountId);
        while (true) {
            try {
                return process.execute(account.copy());
            } catch (UnmergeableEventException e) {
                accounts.refresh(account);
            }
        }
    }

    private interface MergeableProcess {
        boolean execute(Account account) throws UnmergeableEventException;
    }
}
