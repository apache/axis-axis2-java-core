package samples.quickstart.service.adb;
import samples.quickstart.service.adb.xsd.GetPriceResponse;
import samples.quickstart.service.adb.xsd.Update;
import samples.quickstart.service.adb.xsd.GetPrice;

import java.util.HashMap;
public class StockQuoteServiceSkeleton implements StockQuoteServiceSkeletonInterface {
    private HashMap map = new HashMap();

    public void update(Update param0) {
        map.put(param0.getSymbol(), new Double(param0.getPrice()));
    }

    public GetPriceResponse getPrice(GetPrice param1) {
        Double price = (Double) map.get(param1.getSymbol());
        double ret = 42;
        if(price != null){
            ret = price.doubleValue();
        }
        GetPriceResponse res =
                new GetPriceResponse();
        res.set_return(ret);
        return res;
    }
}
