package com.github.ylegat.domain;

import static java.lang.String.format;
import static com.google.common.base.Preconditions.checkArgument;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;

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

    private final Queue<Event> eventsToProcess;

    private Account() {
        version = 0;
        reservedCredits = new HashMap<>();
        eventsToProcess = new LinkedList<>();
    }

    private Account(String accountId, Map<String, Long> reservedCredits, long credit, long version, Queue<Event> eventsToProcess) {
        this.accountId = accountId;
        this.reservedCredits = reservedCredits;
        this.credit = credit;
        this.version = version;
        this.eventsToProcess = eventsToProcess;
    }

    public void provisionCredit(long provisionedCredit) {
        checkArgument(provisionedCredit > 0,
                      format("Provisioned credit (%s) should be geater than 0.", provisionedCredit));

        ProvisionedCreditEvent event = new ProvisionedCreditEvent(accountId, provisionedCredit, version + 1);
        eventsToProcess.offer(applyEvent(event));
    }

    public boolean reserveCredit(String callId, long reservedCredit) {
        checkArgument(reservedCredit > 0,
                      format("Reservered credit (%s) should be geater than 0.", reservedCredit));

        if (reservedCredit > credit) {
            return false;
        }

        ReservedCreditEvent event = new ReservedCreditEvent(accountId, callId, reservedCredit, version + 1);
        eventsToProcess.offer(applyEvent(event));
        return true;
    }

    public void terminateCall(String callId, long consumedCredit) {
        Long reservedCredit = reservedCredits.get(callId);
        checkArgument(reservedCredit != null, format("Call-id %s unknown for user.", callId));
        checkArgument(reservedCredit >= consumedCredit,
                      format("Consumed credit (%s) is greater than reserved credit (%s)", consumedCredit, reservedCredit));

        TerminatedCallEvent event = new TerminatedCallEvent(accountId, callId, consumedCredit, version + 1);
        eventsToProcess.offer(applyEvent(event));
    }

    public void consumeEvents(Consumer<Event> eventConsumer) {
        Event event;
        while ((event = eventsToProcess.poll()) != null) {
            eventConsumer.accept(event);
        }
    }

    public List<Event> consumeEvents() {
        return consumeEvents(new ArrayList<>(eventsToProcess.size()));
    }

    public <T extends List<Event>> T consumeEvents(T events) {
        consumeEvents((Consumer<Event>) events::add);
        return events;
    }

    public String getAccountId() {
        return accountId;
    }

    public long getCredit() {
        return credit;
    }

    public Account copy() {
        return new Account(accountId, new HashMap<>(reservedCredits), credit, version, new LinkedList<>(eventsToProcess));
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

    void applyEvents(List<Event> events) {
        events.stream().forEach(this::applyEvent);
    }

    ReservedCreditEvent applyReservedCreditEvent(ReservedCreditEvent event) {
        return applyEvent(event, () -> {
            long newReservedCredit = event.reservedCredit;
            reservedCredits.compute(event.callId, (key, reservedCredit) -> {
                return (reservedCredit == null) ? newReservedCredit : reservedCredit + newReservedCredit;
            });
            credit -= newReservedCredit;
        });
    }

    TerminatedCallEvent applyTerminatedCallEvent(TerminatedCallEvent event) {
        return applyEvent(event, () -> {
            long consumedCredit = event.consumedCredit;
            Long reservedCredit = reservedCredits.remove(event.callId);
            credit += (reservedCredit - consumedCredit);
        });
    }

    CreatedAccountEvent applyCreatedAccountEvent(CreatedAccountEvent event) {
        return applyEvent(event, () -> accountId = event.aggregateId);
    }

    ProvisionedCreditEvent applyProvisionedCreditEvent(ProvisionedCreditEvent event) {
        return applyEvent(event, () -> credit += event.provisionedCredit);
    }

    long version() {
        return version;
    }

    long reservedCredit(String callId) {
        Long reservedCredit = reservedCredits.get(callId);
        return (reservedCredit == null) ? 0L : reservedCredit;
    }

    private <T extends Event> T applyEvent(T event, Runnable eventProcess) {
        checkArgument(event.version == version + 1);
        eventProcess.run();
        version++;
        return event;
    }

    private <T extends Event> T applyEvent(T event) {
        return event.applyTo(this);
    }
}
