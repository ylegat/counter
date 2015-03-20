package counter;

import java.util.concurrent.atomic.AtomicLong;

public class ThreadSafeStock implements Stock {
    private AtomicLong amount = new AtomicLong();

    @Override
    public void book(long amount) throws Exception {
        long remainingAmountBeforeOperation = this.amount.getAndAccumulate(amount,
                (remainingAmount, amountToBook) ->
                        remainingAmount >= amountToBook ?
                                remainingAmount - amountToBook :
                                remainingAmount)
                ;

        if(remainingAmountBeforeOperation < amount) {
            throw new Exception();
        }
    }

    @Override
    public void provision(long amount) {
        this.amount.addAndGet(amount);
    }

    @Override
    public long remainingAmount() {
        return this.amount.get();
    }
}
