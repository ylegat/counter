package counter.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import counter.Stock;

class JavaStock implements Stock {
    private AtomicLong stock = new AtomicLong();

    private List<JavaCallRecord> callRecords = Collections.synchronizedList(new ArrayList<>());

    private List<JavaCreditRecord> creditRecords = Collections.synchronizedList(new ArrayList<>());

    @Override
    public long reserveCredits(String account) throws StoreEmptyException {
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
    public JavaCreditRecord credit(String account, long amountToProvision) {
        JavaCreditRecord creditRecord = new JavaCreditRecord(account, amountToProvision);
        creditRecords.add(creditRecord);
        this.stock.addAndGet(amountToProvision);
        System.out.println(creditRecord);
        return creditRecord;
    }

    @Override
    public void refundRemainingCredits(String account, long amountNotConsumed) {
        this.stock.addAndGet(amountNotConsumed);
        System.out.println("Refund : " + amountNotConsumed);
    }

    @Override
    public JavaCallRecord recordCall(String account, String caller, long amountConsumed) {
        JavaCallRecord callRecord = new JavaCallRecord(account, caller, amountConsumed);
        callRecords.add(callRecord);
        System.out.println(callRecord);
        return callRecord;
    }

    @Override
    public long getRemainingCredits(String account) {
        return this.stock.get();
    }

    @Override
    public List<JavaCallRecord> getCallRecords(String account) {
        List<JavaCallRecord> bills = new ArrayList<>();
        synchronized (this.callRecords) {
            bills.addAll(this.callRecords);
        }
        return bills;
    }

    @Override
    public List<JavaCreditRecord> getCreditRecords(String account) {
        List<JavaCreditRecord> provisions = new ArrayList<>();
        synchronized (this.creditRecords) {
            provisions.addAll(this.creditRecords);
        }
        return provisions;
    }
}
