package counter;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import counter.StockWriteOperations.StoreEmptyException;

public abstract class StockTest {

    private static final String ACCOUNT = "MY_ACCOUNT_ID";

    public abstract Stock implementation();

    private StockWriteOperations stockWrite;
    private StockReadOperations stockRead;

    @Before
    public void init() {
        Stock stock = implementation();
        stockWrite = stock;
        stockRead = stock;
    }

    @Test
    public void should_initial_credits_be_zero() {
        assertThat(stockRead.getRemainingCredits(ACCOUNT)).isEqualTo(0);
    }

    @Test
    public void should_provision_credits() {
        stockWrite.credit(ACCOUNT, 10);
        assertThat(stockRead.getRemainingCredits(ACCOUNT)).isEqualTo(10);
    }

    @Test
    public void should_reserve_credits() throws Exception {
        stockWrite.credit(ACCOUNT, 10);
        long reservedCredits = stockWrite.reserveCredits(ACCOUNT);
        assertThat(reservedCredits).isGreaterThan(0);
        assertThat(stockRead.getRemainingCredits(ACCOUNT) + reservedCredits).isEqualTo(10);
    }

    @Test(expected = StoreEmptyException.class)
    public void should_fail_to_reserve_if_empty() throws Exception {
        stockWrite.reserveCredits(ACCOUNT);
    }

    @Test
    public void should_not_consume_more_than_provisioned_amount() throws Exception {

        CreditProvisioner creditProvisioner = new CreditProvisioner(stockWrite);
        List<Caller> consumers = rangeClosed(1, 4).boxed().map(i -> new Caller("consumer" + i, stockWrite)).collect(toList());
        creditProvisioner.start();
        consumers.stream().forEach(Caller::start);

        TimeUnit.MILLISECONDS.sleep(10000);

        creditProvisioner.interrupt();
        consumers.stream().forEach(Caller::interrupt);

        List<CreditRecord> creditRecords = stockRead.getCreditRecords(ACCOUNT);
        System.out.println("creditRecords : " + creditRecords);

        List<CallRecord> callRecords = stockRead.getCallRecords(ACCOUNT);
        System.out.println("callRecords : " + callRecords);

        long totalProvisioned = creditRecords.stream().mapToLong(creditRecord -> creditRecord.getProvisionedCredits()).sum();
        System.out.println("totalProvisioned : " + totalProvisioned);

        long totalConsumed = callRecords.stream().mapToLong(creditRecord -> creditRecord.getConsumedCredits()).sum();
        System.out.println("totalConsumed : " + totalConsumed);

        long stock = stockRead.getRemainingCredits(ACCOUNT);
        System.out.println("stock : " + stock);

        assertThat(stock + totalConsumed).isEqualTo(totalProvisioned);
    }

    private static class Caller extends Thread {

        private final String name;
        private final StockWriteOperations stock;

        public Caller(String name, StockWriteOperations stock) {
            this.name = name;
            this.stock = stock;
        }

        @Override
        public void run() {
            while (true) {
                final long creditsToConsumeForThisCall = ThreadLocalRandom.current().nextLong(100);
                long consumedCredits = 0;
                try {
                    do {
                        long reservedCredits = stock.reserveCredits(ACCOUNT);
                        long remainingToConsume = creditsToConsumeForThisCall - consumedCredits;
                        if (remainingToConsume >= reservedCredits) {
                            consumedCredits += reservedCredits;
                        } else {
                            consumedCredits += remainingToConsume;
                            stock.refundRemainingCredits(ACCOUNT, reservedCredits - remainingToConsume);
                        }
                    } while (consumedCredits != creditsToConsumeForThisCall);
                } catch (StoreEmptyException e) {
                    if (consumedCredits > 0) {
                        System.err.println("Consumption ended because the stock is empty");
                    } else {
                        System.err.println("Consumption not started because the stock is empty");
                        try {
                            TimeUnit.MILLISECONDS.sleep(1100);
                        } catch (InterruptedException ignored) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                if (consumedCredits > 0) {
                    stock.recordCall(ACCOUNT, name, consumedCredits);
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private static class CreditProvisioner extends Thread {

        private final StockWriteOperations stock;

        public CreditProvisioner(StockWriteOperations stock) {
            this.stock = stock;
        }

        @Override
        public void run() {
            while (true) {
                stock.credit(ACCOUNT, 2000);
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

}
