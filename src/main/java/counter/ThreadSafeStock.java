package counter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadSafeStock implements Stock {
    private static final long STANDARD_BOOKING = 10;

    private AtomicLong stock = new AtomicLong();

    private List<Bill> bills = Collections.synchronizedList(new ArrayList<>());

    private List<Provision> provisions = Collections.synchronizedList(new ArrayList<>());

    @Override
    public long book() throws StoreEmptyException {
        long toBook = STANDARD_BOOKING;
        do {
            long stockBeforeOperation = stock.getAndAccumulate(toBook,
                    (stock, amountToBook) ->
                    stock >= amountToBook ? stock - amountToBook
                                         : stock);

            if (stockBeforeOperation == 0) {
                throw new StoreEmptyException();
            }
            if (stockBeforeOperation >= toBook) {
                return toBook;
            }
            toBook = stockBeforeOperation;
        } while (true);
    }

    @Override
    public Provision provision(long amount) {
        Provision provision = new Provision(amount);
        provisions.add(provision);
        this.stock.addAndGet(amount);
        System.out.println(provision);
        return provision;
    }

    @Override
    public void refund(long amountNotConsumed) {
        this.stock.addAndGet(amountNotConsumed);
        System.out.println("Refund : " + amountNotConsumed);
    }

    @Override
    public long remainingAmount() {
        return this.stock.get();
    }

    @Override
    public Bill bill(String consumer, long amount) {
        Bill bill = new Bill(consumer, amount);
        bills.add(bill);
        System.out.println(bill);
        return bill;
    }

    @Override
    public List<Bill> bills() {
        List<Bill> bills = new ArrayList<>();
        synchronized (this.bills) {
            bills.addAll(this.bills);
        }
        return bills;
    }

    @Override
    public List<Provision> provisions() {
        List<Provision> provisions = new ArrayList<>();
        synchronized (this.provisions) {
            provisions.addAll(this.provisions);
        }
        return provisions;
    }
}
