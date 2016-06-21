package com.github.ylegat.infrastructure;

import static com.github.ylegat.domain.Account.createNewAccount;
import static com.github.ylegat.uncheck.Uncheck.uncheck;
import com.github.ylegat.domain.Account;
import com.github.ylegat.domain.AccountRepository;
import com.github.ylegat.domain.UnmergeableEventException;

public class AccountService {

    private AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account getAccount(String accountId) {
        return accountRepository.get(accountId);
    }

    public Account createAccount() {
        Account account = createNewAccount();
        uncheck(() -> accountRepository.save(account));
        return account;
    }

    public void provisionCredit(String accountId, long credit) {
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
        Account account = accountRepository.get(accountId);
        if (!account.reserveCredit(callId, credit)) {
            return false;
        }

        accountRepository.save(account);
        return true;
    }

    private void tryTerminateCall(String accountId, String callId, long consumedCredit) throws UnmergeableEventException {
        Account account = accountRepository.get(accountId);
        account.terminateCall(callId, consumedCredit);
        accountRepository.save(account);
    }
}
