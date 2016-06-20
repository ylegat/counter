package com.github.ylegat.domain;

import com.github.ylegat.domain.event.CreatedAccountEvent;
import com.github.ylegat.domain.event.Event;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static com.github.ylegat.domain.Account.createNewAccount;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateNewAccountTest {

    @Test
    public void should_generate_create_new_account_event() {
        // Given
        Account account = createNewAccount();

        // When
        List<Event> events = account.consumeEvents(new LinkedList<>());

        // Then
        Assertions.assertThat(events).hasSize(1);
        Event event = events.get(0);
        CreatedAccountEvent expectedEvent = new CreatedAccountEvent(account.getAccountId());
        assertThat(event).isEqualTo(expectedEvent);
    }

}
