package com.github.ylegat.domain;

import static org.assertj.core.groups.Tuple.tuple;
import static com.github.ylegat.domain.Account.createNewAccount;
import java.util.LinkedList;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class AccountEventConsumingTest {

    @Test
    public void should_fetch_all_event_in_a_collection_in_order() {
        // Given
        Account account = createNewAccount();
        account.provisionCredit(10L);

        // When
        LinkedList<Event> events = account.consumeEvents(new LinkedList<>());

        // Then
        Assertions.assertThat(events).extracting(e -> e.eventType, e -> e.version)
                  .containsExactly(tuple(CreatedAccountEvent.CREATED_ACCOUNT_EVENT, 1L),
                                           tuple(ProvisionedCreditEvent.PROVISIONED_CREDIT_EVENT, 2L));
    }

    @Test
    public void should_consume_event_when_fetching() {
        // Given
        Account account = createNewAccount();
        account.provisionCredit(10L);
        account.consumeEvents(new LinkedList<>());

        // When
        LinkedList<Event> events = account.consumeEvents(new LinkedList<>());

        // Then
        Assertions.assertThat(events).isEmpty();
    }

}
