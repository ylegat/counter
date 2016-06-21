package com.github.ylegat.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static com.github.ylegat.domain.Account.createNewAccount;
import java.util.LinkedList;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import com.github.ylegat.domain.event.Event;
import com.github.ylegat.domain.event.ProvisionedCreditEvent;

public class CreditProvisioningTest {

    private Consumer<Event> ignoreEvents = event -> {};

    @Test
    public void should_provision_credit() {
        // Given
        Account account = createNewAccount();

        // When
        account.provisionCredit(10L);

        // Then
        assertThat(account.getCredit()).isEqualTo(10L);
    }

    @Test
    public void should_generate_provisioned_credit_event() {
        // Given
        Account account = createNewAccount();
        account.consumeEvents(ignoreEvents);

        // When
        account.provisionCredit(10L);
        LinkedList<Event> events = account.consumeEvents(new LinkedList<>());

        // Then
        Assertions.assertThat(events).hasSize(1);
        Event event = events.get(0);
        ProvisionedCreditEvent expectedEvent = new ProvisionedCreditEvent(account.getAccountId(), 10L, account.getVersion());
        assertThat(event).isEqualTo(expectedEvent);
    }

    @Test
    public void void_should_prevent_crediting_negative_amount() {
        // Given
        Account account = createNewAccount();

        // When
        Throwable throwable = catchThrowable(() -> account.provisionCredit(-1L));

        // Then
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
    }

}
