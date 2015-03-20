package counter;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Before;
import org.junit.Test;

public class StockTest {

    private Stock stock = null;

    @Before
    public void before() {
        stock = new ThreadSafeStock();
    }

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
        stock.book(5);
        assertThat(stock.remainingAmount()).isEqualTo(5);
    }


    @Test(expected = Exception.class)
    public void should_not_book() throws Exception {
        stock.book(1);
    }

    @Test
    public void should_not_book_more_than_provisioned_amout() throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        stock.provision(10000);
        AtomicLong booked = new AtomicLong(0);

        for (int i = 0; i < 100000; i++) {
            threadPool.submit(() -> {
                try {
                    stock.book(1);
                    booked.incrementAndGet();
                } catch (Exception ignored) {
                }
            });
        }

        threadPool.shutdown();
        threadPool.awaitTermination(10, TimeUnit.SECONDS);
        assertThat(booked.get()).isEqualTo(10000);
    }
}