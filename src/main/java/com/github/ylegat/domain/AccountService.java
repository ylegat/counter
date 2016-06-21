package com.github.ylegat.domain;

import static com.github.ylegat.domain.Account.createNewAccount;
import static com.github.ylegat.uncheck.Uncheck.uncheck;

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
        return retryUntilMerged(() -> tryReserveCredit(accountId, callId, credit));
    }

    public boolean terminateCall(String accountId, String callId, long consumedCredit) {
        return retryUntilMerged(() -> tryTerminateCall(accountId, callId, consumedCredit));
    }

    private boolean tryReserveCredit(String accountId, String callId, long credit) throws UnmergeableEventException {
        Account account = accounts.get(accountId);
        if (!account.reserveCredit(callId, credit)) {
            return false;
        }

        accounts.save(account);
        return true;
    }

    private boolean tryTerminateCall(String accountId,
                                     String callId,
                                     long consumedCredit) throws UnmergeableEventException {
        Account account = accounts.get(accountId);
        account.terminateCall(callId, consumedCredit);
        accounts.save(account);
        return true;
    }

    private boolean     retryUntilMerged(MergeableProcess process) {
        while (true) {
            try {
                return process.process();
            } catch (UnmergeableEventException e) {
            }
        }
    }

    private interface MergeableProcess {
        boolean process() throws UnmergeableEventException;
    }
}
