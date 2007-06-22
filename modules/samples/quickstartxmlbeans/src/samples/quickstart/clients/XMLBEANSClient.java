package samples.quickstart.clients;

import samples.quickstart.service.xmlbeans.StockQuoteServiceStub;
import samples.quickstart.service.xmlbeans.xsd.GetPriceDocument;
import samples.quickstart.service.xmlbeans.xsd.GetPriceResponseDocument;
import samples.quickstart.service.xmlbeans.xsd.UpdateDocument;

public class XMLBEANSClient{

    public static void main(java.lang.String args[]){
        try{
            StockQuoteServiceStub stub =
                new StockQuoteServiceStub
                ("http://localhost:8080/axis2/services/StockQuoteService");

            getPrice(stub);
            update(stub);
            getPrice(stub);

        } catch(Exception e){
            e.printStackTrace();
            System.err.println("\n\n\n");
        }
    }

    /* fire and forget */
    public static void update(StockQuoteServiceStub stub){
        try{
            UpdateDocument reqDoc = UpdateDocument.Factory.newInstance();
            UpdateDocument.Update req = reqDoc.addNewUpdate();
            req.setSymbol ("BCD");
            req.setPrice (42.32);

            stub.update(reqDoc);
            System.err.println("price updated");
        } catch(Exception e){
            e.printStackTrace();
            System.err.println("\n\n\n");
        }
    }

    /* two way call/receive */
    public static void getPrice(StockQuoteServiceStub stub){
        try{
            GetPriceDocument reqDoc = GetPriceDocument.Factory.newInstance();
            GetPriceDocument.GetPrice req = reqDoc.addNewGetPrice();
            req.setSymbol("BCD");

            GetPriceResponseDocument res =
                stub.getPrice(reqDoc);

            System.err.println(res.getGetPriceResponse().getReturn());
        } catch(Exception e){
            e.printStackTrace();
            System.err.println("\n\n\n");
        }
    }
}
