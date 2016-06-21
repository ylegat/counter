package com.github.ylegat.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static com.github.ylegat.domain.Account.createNewAccount;
import java.util.LinkedList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import com.github.ylegat.domain.event.CreatedAccountEvent;
import com.github.ylegat.domain.event.Event;

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
