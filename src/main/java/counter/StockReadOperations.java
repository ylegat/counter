package counter;

import java.util.List;

public interface StockReadOperations {

    long getRemainingCredits(String account);

    List<? extends CallRecord> getCallRecords(String account);

    List<? extends CreditRecord> getCreditRecords(String account);
}
