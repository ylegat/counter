package com.github.ylegat.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static com.github.ylegat.domain.event.CreatedAccountEvent.CREATED_ACCOUNT_EVENT;
import static com.github.ylegat.domain.event.ProvisionedCreditEvent.PROVISIONED_CREDIT_EVENT;
import static com.github.ylegat.domain.event.ReservedCreditEvent.RESERVED_CREDIT_EVENT;
import static com.github.ylegat.domain.event.TerminatedCallEvent.TERMINATED_CALL_EVENT;
import static com.google.common.collect.Sets.newHashSet;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.Test;
import com.github.ylegat.EventSerializer;
import com.github.ylegat.domain.Account;
import com.github.ylegat.domain.EventStore;
import com.github.ylegat.domain.event.CreatedAccountEvent;
import com.github.ylegat.domain.event.Event;
import com.github.ylegat.domain.event.ProvisionedCreditEvent;
import com.github.ylegat.domain.event.ReservedCreditEvent;
import com.github.ylegat.domain.event.TerminatedCallEvent;

public class AccountServiceTest {

    @Test
    public void should_create_account() {
        // Given
        AccountService accountService = accountService();

        // When
        Account account = accountService.createAccount();

        // Then
        assertThat(refreshAccount(accountService, account)).isEqualTo(account);
    }

    @Test
    public void should_add_credit() {
        // Given
        AccountService accountService = accountService();
        Account account = accountService.createAccount();

        // When
        accountService.provisionCredit(account.getAccountId(), 10L);

        // Then
        assertThat(refreshAccount(accountService, account).getCredit()).isEqualTo(10L);
    }

    @Test
    public void should_reserve_credit() {
        // Given
        AccountService accountService = accountService();
        Account account = accountService.createAccount();
        accountService.provisionCredit(account.getAccountId(), 10L);

        // When
        accountService.reserveCredit(account.getAccountId(), "callId", 2L);

        // Then
        assertThat(refreshAccount(accountService, account).getCredit()).isEqualTo(8L);
    }

    @Test
    public void should_terminate_call() {
        // Given
        AccountService accountService = accountService();
        Account account = accountService.createAccount();
        accountService.provisionCredit(account.getAccountId(), 10L);
        accountService.reserveCredit(account.getAccountId(), "callId", 2L);

        // When
        accountService.terminateCall(account.getAccountId(), "callId", 1L);

        // Then
        assertThat(refreshAccount(accountService, account).getCredit()).isEqualTo(9L);
    }

    private Account refreshAccount(AccountService accountService, Account account) {
        return accountService.getAccount(account.getAccountId());
    }

    private AccountService accountService() {
        HashMap<String, Class<? extends Event>> eventsMap = new HashMap<>();
        eventsMap.put(CREATED_ACCOUNT_EVENT, CreatedAccountEvent.class);
        eventsMap.put(PROVISIONED_CREDIT_EVENT, ProvisionedCreditEvent.class);
        eventsMap.put(RESERVED_CREDIT_EVENT, ReservedCreditEvent.class);
        eventsMap.put(TERMINATED_CALL_EVENT, TerminatedCallEvent.class);

        HashSet<String> conflictingEvents = newHashSet(RESERVED_CREDIT_EVENT, TERMINATED_CALL_EVENT);

        EventSerializer eventSerializer = new EventSerializer(eventsMap);
        EventStore eventStore = new SQLEventStore(eventSerializer, conflictingEvents);
        return new AccountService(new SQLAccountRepository(eventStore));
    }
}