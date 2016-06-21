package com.github.ylegat.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static com.github.ylegat.domain.Account.createNewAccount;
import static com.github.ylegat.domain.Account.loadAccount;
import java.util.LinkedList;
import org.junit.Test;

public class ReplayAccountEventTest {

    @Test
    public void should_replay_all_events() {
        // Given
        Account account = createNewAccount();
        account.provisionCredit(10L);

        // When
        Account duplicatedAccount = loadAccount(account.consumeEvents(new LinkedList<>()));

        // Then
        assertThat(duplicatedAccount).isEqualTo(account);
    }
}
