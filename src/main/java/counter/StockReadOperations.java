package counter;

import java.util.List;

public interface StockReadOperations {

    long getRemainingCredits(String account);

    List<CallRecord> getCallRecords(String account);

    List<CreditRecord> getCreditRecords(String account);
}
