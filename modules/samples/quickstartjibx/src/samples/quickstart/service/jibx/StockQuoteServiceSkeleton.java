package samples.quickstart.service.jibx;

import java.util.HashMap;
public class StockQuoteServiceSkeleton implements StockQuoteServiceSkeletonInterface {
    private HashMap map = new HashMap();

    public void update(String symbol, Double price) {
        map.put(symbol, price);
    }

    public Double getPrice(String symbol) {
        if (symbol == null) {
            return null;
        } else {
            return (Double) map.get(symbol);
        }
    }
}
