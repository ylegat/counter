package counter;

public interface StockWriteOperations {

    long reserveCredits(String account) throws StoreEmptyException;

    CreditRecord credit(String account, long amountToProvision);

    void refundRemainingCredits(String account, long amountNotConsumed);

    CallRecord recordCall(String account, String caller, long amountConsumed);

    public static class StoreEmptyException extends Exception {

    }
}
