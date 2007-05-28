/**
 * StockQuoteServiceSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.1-RC2 Nov 02, 2006 (02:37:50 LKT)
 */
package samples.quickstart.service.xmlbeans;
import samples.quickstart.service.xmlbeans.xsd.GetPriceDocument;
import samples.quickstart.service.xmlbeans.xsd.GetPriceResponseDocument;
import samples.quickstart.service.xmlbeans.xsd.UpdateDocument;

import java.util.HashMap;
/**
 * StockQuoteServiceSkeleton java skeleton for the axisService
 */
public class StockQuoteServiceSkeleton implements StockQuoteServiceSkeletonInterface {

    private HashMap map = new HashMap();

    public void update(UpdateDocument param0) {
        map.put(param0.getUpdate().getSymbol(), new Double(param0.getUpdate().getPrice()));
    }

    public GetPriceResponseDocument getPrice(GetPriceDocument param1) {
        Double price = (Double) map.get(param1.getGetPrice().getSymbol());
        double ret = 42;
        if(price != null){
            ret = price.doubleValue();
        }
        System.err.println();
        GetPriceResponseDocument resDoc =
                GetPriceResponseDocument.Factory.newInstance();
        GetPriceResponseDocument.GetPriceResponse res =
                resDoc.addNewGetPriceResponse();
        res.setReturn(ret);
        return resDoc;
    }
}
    