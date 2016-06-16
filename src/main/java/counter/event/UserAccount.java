package counter.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static counter.event.CallEndedEvent.CALL_ENDED_EVENT;
import static counter.event.CreatedAccountEvent.CREATED_ACCOUNT_EVENT;
import static counter.event.ProvisionedCreditEvent.PROVISIONED_CREDIT_EVENT;
import static counter.event.ReservedCreditEvent.RESERVED_CREDIT_EVENT;

public class UserAccount {

    public static UserAccount createUserAccount(Consumer<Event> eventConsumer) {
        UserAccount userAccount = new UserAccount();
        eventConsumer.accept(userAccount.applyEvent(new CreatedAccountEvent(UUID.randomUUID().toString())));
        return userAccount;
    }

    private String account;

    private final Map<String, Long> reservedCredits;

    private long credit;

    private long version;

    private UserAccount() {
        version = 0;
        reservedCredits = new HashMap<>();
    }

    public void provisionCredit(long provisionedCredit, Consumer<Event> eventConsumer) {
        eventConsumer.accept(applyEvent(new ProvisionedCreditEvent(account, provisionedCredit, nextVersion())));
    }

    public void reserveCredit(String callId, long reservedCredit, Consumer<Event> eventConsumer) {
        if (credit < reservedCredit) {
            throw new IllegalArgumentException("Not enough credit for a reservation of " + reservedCredit);
        }

        eventConsumer.accept(applyEvent(new ReservedCreditEvent(account, callId, reservedCredit, nextVersion())));
    }

    public void endCall(String callId, String caller, long consumedCredit, Consumer<Event> eventConsumer) {
        if (reservedCredits.get(callId) < consumedCredit) {
            throw new IllegalArgumentException("consumed credit is greater than reserved credit");
        }

        eventConsumer.accept(applyEvent(new CallEndedEvent(account, callId, caller, consumedCredit, nextVersion())));
    }

    public void applyEvents(List<Event> events) {
        events.stream().forEach(this::applyEvent);
    }

    public <T extends Event> T applyEvent(T event) {
        switch (event.eventType()) {
            case CREATED_ACCOUNT_EVENT:
                CreatedAccountEvent createdAccountEvent = (CreatedAccountEvent) event;
                account = createdAccountEvent.account;
                break;
            case PROVISIONED_CREDIT_EVENT:
                ProvisionedCreditEvent provisionedCreditEvent = (ProvisionedCreditEvent) event;
                credit += provisionedCreditEvent.provisionedCredit;
                break;
            case RESERVED_CREDIT_EVENT:
                ReservedCreditEvent reservedCreditEvent = (ReservedCreditEvent) event;
                long newReservedCredit = reservedCreditEvent.reservedCredit;
                reservedCredits.compute(reservedCreditEvent.callId, (key, reservedCredit) -> {
                    return (reservedCredit == null) ? newReservedCredit : reservedCredit + newReservedCredit;
                });
                credit -= newReservedCredit;
                break;
            case CALL_ENDED_EVENT:
                CallEndedEvent callEndedEvent = (CallEndedEvent) event;
                long consumedCredit = callEndedEvent.consumedCredit;
                Long reservedCredit = reservedCredits.remove(callEndedEvent.callId);
                credit += (reservedCredit - consumedCredit);
                break;
        }

        version = event.version();
        return event;
    }

    private long nextVersion() {
        return version + 1;
    }

}
