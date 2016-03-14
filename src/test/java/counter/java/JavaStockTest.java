package counter.java;

import counter.Stock;
import counter.StockTest;

public class JavaStockTest extends StockTest {

    private Stock stock = new JavaStock();

    @Override
    public Stock implementation() {
        return stock;
    }
}
