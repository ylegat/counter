package com.github.ylegat.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static com.github.ylegat.domain.Account.createNewAccount;
import java.util.LinkedList;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TerminatedCallTest {

    private Consumer<Event> ignoreEvents = event -> {};

    @Test
    public void should_fail_when_terminated_unknown_call() {
        // Given
        Account account = createNewAccount();

        // When
        Throwable throwable = catchThrowable(() -> account.terminateCall("callId", 1L));

        // Then
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_restore_unused_credit_when_call_terminated() {
        // Given
        Account account = createNewAccount();
        account.provisionCredit(2L);
        account.reserveCredit("callId", 2L);

        // When
        account.terminateCall("callId", 1L);

        // Then
        assertThat(account.getCredit()).isEqualTo(1L);
        assertThat(account.reservedCredit("callId")).isEqualTo(0L);
    }

    @Test
    public void should_generate_terminated_call_event() {
        // Given
        Account account = createNewAccount();
        account.provisionCredit(1L);
        account.reserveCredit("callId", 1L);
        account.consumeEvents(ignoreEvents);

        // When
        account.terminateCall("callId", 1L);
        LinkedList<Event> events = account.consumeEvents(new LinkedList<>());

        // Then
        Assertions.assertThat(events).hasSize(1);
        Event event = events.get(0);
        TerminatedCallEvent expectedEvent = new TerminatedCallEvent(account.getAccountId(), "callId",
                                                                    1L, account.version());
        assertThat(event).isEqualTo(expectedEvent);
    }

}
