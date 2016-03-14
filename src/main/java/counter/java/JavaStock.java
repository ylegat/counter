package counter.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import counter.CallRecord;
import counter.CreditRecord;
import counter.Stock;

class JavaStock implements Stock {
    private static final long STANDARD_BOOKING = 10;

    private AtomicLong stock = new AtomicLong();

    private List<CallRecord> callRecords = Collections.synchronizedList(new ArrayList<>());

    private List<CreditRecord> creditRecords = Collections.synchronizedList(new ArrayList<>());

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
    public CreditRecord credit(String account, long amountToProvision) {
        CreditRecord creditRecord = new JavaCreditRecord(account, amountToProvision);
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
    public CallRecord recordCall(String account, String caller, long amountConsumed) {
        CallRecord callRecord = new JavaCallRecord(account, caller, amountConsumed);
        callRecords.add(callRecord);
        System.out.println(callRecord);
        return callRecord;
    }

    @Override
    public long getRemainingCredits(String account) {
        return this.stock.get();
    }

    @Override
    public List<CallRecord> getCallRecords(String account) {
        List<CallRecord> bills = new ArrayList<>();
        synchronized (this.callRecords) {
            bills.addAll(this.callRecords);
        }
        return bills;
    }

    @Override
    public List<CreditRecord> getCreditRecords(String account) {
        List<CreditRecord> provisions = new ArrayList<>();
        synchronized (this.creditRecords) {
            provisions.addAll(this.creditRecords);
        }
        return provisions;
    }
}
