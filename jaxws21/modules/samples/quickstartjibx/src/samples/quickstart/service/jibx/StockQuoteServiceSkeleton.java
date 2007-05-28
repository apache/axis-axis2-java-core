package samples.quickstart.service.jibx;

import java.util.HashMap;

public class StockQuoteServiceSkeleton implements StockQuoteServiceSkeletonInterface {
    private HashMap map = new HashMap();

    public void update(String symbol, Double price) {
        map.put(symbol, price);
    }

    public Double getPrice(String symbol) {
        Double ret = (Double) map.get(symbol);
        if (ret == null) {
            ret = new Double(42.0);
        }
        return ret;
    }
}
