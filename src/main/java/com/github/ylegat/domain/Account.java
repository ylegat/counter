package com.github.ylegat.domain;

import com.github.ylegat.domain.event.*;

import java.util.*;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

public class Account {

    public static Account createNewAccount() {
        Account account = new Account();
        CreatedAccountEvent event = new CreatedAccountEvent(UUID.randomUUID().toString());
        account.eventsToProcess.offer(account.applyEvent(event));
        return account;
    }

    public static Account loadAccount(List<Event> events) {
        Account account = new Account();
        account.applyEvents(events);
        return account;
    }

    private String accountId;

    private final Map<String, Long> reservedCredits;

    private long credit;

    private long version;

    private final Queue<Event> eventsToProcess = new LinkedList<>();

    private Account() {
        version = 0;
        reservedCredits = new HashMap<>();
    }

    public void provisionCredit(long provisionedCredit) {
        checkArgument(provisionedCredit > 0,
                      format("Provisioned credit (%s) should be geater than 0.", provisionedCredit));

        ProvisionedCreditEvent event = new ProvisionedCreditEvent(accountId, provisionedCredit, nextVersion());
        eventsToProcess.offer(applyEvent(event));
    }

    public void reserveCredit(String callId, long reservedCredit) {
        checkArgument(credit >= reservedCredit,
                      format("Not enough credit (%s) for a reservation of %s.", credit, reservedCredit));

        ReservedCreditEvent event = new ReservedCreditEvent(accountId, callId, reservedCredit, nextVersion());
        eventsToProcess.offer(applyEvent(event));
    }

    public void terminateCall(String callId, String caller, long consumedCredit) {
        Long reservedCredit = reservedCredits.get(callId);
        checkArgument(reservedCredit != null, format("CallId %s unknown for user.", callId));
        checkArgument(reservedCredit >= consumedCredit,
                      format("Consumed credit (%s) is greater than reserved credit (%s)", consumedCredit, reservedCredit));

        TerminatedCallEvent event = new TerminatedCallEvent(accountId, callId, caller, consumedCredit, nextVersion());
        eventsToProcess.offer(applyEvent(event));
    }

    private void applyEvents(List<Event> events) {
        events.stream().forEach(this::applyEvent);
    }

    private <T extends Event> T applyEvent(T event) {
        switch (event.eventType) {
            case CreatedAccountEvent.CREATED_ACCOUNT_EVENT:
                CreatedAccountEvent createdAccountEvent = (CreatedAccountEvent) event;
                accountId = createdAccountEvent.aggregateId;
                break;
            case ProvisionedCreditEvent.PROVISIONED_CREDIT_EVENT:
                ProvisionedCreditEvent provisionedCreditEvent = (ProvisionedCreditEvent) event;
                credit += provisionedCreditEvent.provisionedCredit;
                break;
            case ReservedCreditEvent.RESERVED_CREDIT_EVENT:
                ReservedCreditEvent reservedCreditEvent = (ReservedCreditEvent) event;
                long newReservedCredit = reservedCreditEvent.reservedCredit;
                reservedCredits.compute(reservedCreditEvent.callId, (key, reservedCredit) -> {
                    return (reservedCredit == null) ? newReservedCredit : reservedCredit + newReservedCredit;
                });
                credit -= newReservedCredit;
                break;
            case TerminatedCallEvent.TERMINATED_CALL_EVENT:
                TerminatedCallEvent terminatedCallEvent = (TerminatedCallEvent) event;
                long consumedCredit = terminatedCallEvent.consumedCredit;
                Long reservedCredit = reservedCredits.remove(terminatedCallEvent.callId);
                credit += (reservedCredit - consumedCredit);
                break;
        }

        version = event.version;
        return event;
    }

    public void consumeEvents(Consumer<Event> eventConsumer) {
        Event event;
        while ((event = eventsToProcess.poll()) != null) {
            eventConsumer.accept(event);
        }
    }

    public <T extends Collection<Event>> T consumeEvents(T events) {
        consumeEvents((Consumer<Event>) events::add);
        return events;
    }

    private long nextVersion() {
        return version + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return credit == account.credit &&
                version == account.version &&
                Objects.equals(accountId, account.accountId) &&
                Objects.equals(reservedCredits, account.reservedCredits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }

    public String getAccountId() {
        return accountId;
    }

    public long getCredit() {
        return credit;
    }

    public long getVersion() {
        return version;
    }

    public long reservedCredit(String callId) {
        Long reservedCredit = reservedCredits.get(callId);
        return (reservedCredit == null) ? 0L : reservedCredit;
    }
}
