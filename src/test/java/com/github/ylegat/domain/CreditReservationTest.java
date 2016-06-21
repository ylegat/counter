package com.github.ylegat.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static com.github.ylegat.domain.Account.createNewAccount;
import java.util.LinkedList;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class CreditReservationTest {

    private Consumer<Event> ignoreEvents = event -> {};

    @Test
    public void should_fail_when_reserving_credit_greater_then_provisioned() {
        // Given
        Account account = createNewAccount();

        // When
        boolean result = account.reserveCredit("callId", 1L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void should_authorize_credit_reservation_when_enough_credit_is_provisioned() {
        // Given
        Account account = createNewAccount();
        account.provisionCredit(1L);

        // When
        account.reserveCredit("callId", 1L);

        // Then
        assertThat(account.getCredit()).isEqualTo(0L);
        assertThat(account.reservedCredit("callId")).isEqualTo(1L);
    }

    @Test
    public void should_generate_reserved_credit_event() {
        // Given
        Account account = createNewAccount();
        account.provisionCredit(1L);
        account.consumeEvents(ignoreEvents);

        // When
        account.reserveCredit("callId", 1L);
        LinkedList<Event> events = account.consumeEvents(new LinkedList<>());

        // Then
        Assertions.assertThat(events).hasSize(1);
        Event event = events.get(0);
        ReservedCreditEvent expectedEvent = new ReservedCreditEvent(account.getAccountId(), "callId", 1L, account.version());
        assertThat(event).isEqualTo(expectedEvent);
    }

}
