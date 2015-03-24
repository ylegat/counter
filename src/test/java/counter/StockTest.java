package counter;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import counter.Stock.Bill;
import counter.Stock.Provision;
import counter.Stock.StoreEmptyException;

public class StockTest {

    private Stock stock = new ThreadSafeStock();

    @Test
    public void should_initial_stock_amount_be_zero() {
        assertThat(stock.remainingAmount()).isEqualTo(0);
    }

    @Test
    public void should_provision() {
        stock.provision(10);
        assertThat(stock.remainingAmount()).isEqualTo(10);
    }

    @Test
    public void should_book() throws Exception {
        stock.provision(10);
        long booked = stock.book();
        assertThat(booked).isGreaterThan(0);
        assertThat(stock.remainingAmount() + booked).isEqualTo(10);
    }

    @Test(expected = StoreEmptyException.class)
    public void should_not_book() throws Exception {
        stock.book();
    }

    @Test
    public void should_not_consume_more_than_provisioned_amount() throws Exception {

        Provisioner provisioner = new Provisioner(stock);
        List<Consumer> consumers = rangeClosed(1, 4).boxed().map(i -> new Consumer("consumer" + i, stock)).collect(toList());
        provisioner.start();
        consumers.stream().forEach(Consumer::start);

        TimeUnit.MILLISECONDS.sleep(10000);

        provisioner.interrupt();
        consumers.stream().forEach(Consumer::interrupt);

        List<Provision> provisions = stock.provisions();
        System.out.println("provisions : " + provisions);

        List<Bill> bills = stock.bills();
        System.out.println("bills : " + bills);

        long totalProvisioned = provisions.stream().mapToLong(provision -> provision.getAmount()).sum();
        System.out.println("totalProvisioned : " + totalProvisioned);

        long totalBilled = bills.stream().mapToLong(bill -> bill.getAmount()).sum();
        System.out.println("totalBilled : " + totalBilled);

        long stock = this.stock.remainingAmount();
        System.out.println("stock : " + stock);

        assertThat(stock + totalBilled).isEqualTo(totalProvisioned);
    }

    private static class Consumer extends Thread {

        private final String name;
        private final Stock stock;

        public Consumer(String name, Stock stock) {
            this.name = name;
            this.stock = stock;
        }

        @Override
        public void run() {
            while (true) {
                final long toConsume = ThreadLocalRandom.current().nextLong(100);
                long consumed = 0;
                try {
                    do {
                        long booked = stock.book();
                        long remainingToConsume = toConsume - consumed;
                        if (remainingToConsume >= booked) {
                            consumed += booked;
                        } else {
                            consumed += remainingToConsume;
                            stock.refund(booked - remainingToConsume);
                        }
                    } while (consumed != toConsume);
                } catch (StoreEmptyException e) {
                    if (consumed > 0) {
                        System.err.println("Comsumption ended because the stock is empty");
                    } else {
                        System.err.println("Comsumption not started because the stock is empty");
                        try {
                            TimeUnit.MILLISECONDS.sleep(2000);
                        } catch (InterruptedException ignored) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                if (consumed > 0) {
                    stock.bill(name, consumed);
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

    private static class Provisioner extends Thread {

        private final Stock stock;

        public Provisioner(Stock stock) {
            this.stock = stock;
        }

        @Override
        public void run() {
            while (true) {
                stock.provision(1000);
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
