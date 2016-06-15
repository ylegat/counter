package counter.java;

import counter.Stock;
import counter.StockTest;

public class JavaStockTest extends StockTest {

    @Override
    public Stock implementation() {
        return new JavaStock();
    }
}
