package counter;

import java.util.List;

public interface Stock {

    long book() throws StoreEmptyException;

    Provision provision(long amount);

    void refund(long amountNotConsumed);

    long remainingAmount();

    Bill bill(String consumer, long amount);

    List<Bill> bills();

    List<Provision> provisions();

    public static class StoreEmptyException extends Exception {

    }

    static class Bill {
        private String consumer;
        private long amount;

        public Bill(String consumer, long amount) {
            this.amount = amount;
            this.consumer = consumer;
        }

        public String getConsumer() {
            return consumer;
        }

        public long getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return "Bill [consumer=" + consumer + ", amount=" + amount + "]";
        }
    }

    static class Provision {
        private long amount;

        public Provision(long amount) {
            this.amount = amount;
        }

        public long getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return "Provision [amount=" + amount + "]";
        }
    }
}
