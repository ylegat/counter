package counter;

public class SimpleStock implements Stock {
    private long amount = 0L;

    @Override
    public void book(long amount) throws Exception {
        if (this.amount < amount) {
            throw new Exception("Not enough amount remaining");
        }
        this.amount -= amount;
    }

    @Override
    public void provision(long amount) {
        this.amount += amount;
    }

    @Override
    public long remainingAmount() {
        return amount;
    }
}
