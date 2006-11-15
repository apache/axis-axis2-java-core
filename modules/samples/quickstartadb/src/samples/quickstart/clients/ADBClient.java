package samples.quickstart.clients;

import samples.quickstart.service.adb.StockQuoteServiceStub;
public class ADBClient{
    public static void main(java.lang.String args[]){
        try{
            StockQuoteServiceStub stub =
                new StockQuoteServiceStub
                ("http://localhost:8080/axis2/services/StockQuoteService");

            getPrice(stub);
            update(stub);

        } catch(Exception e){
            e.printStackTrace();
            System.err.println("\n\n\n");
        }
    }

    /* fire and forget */
    public static void update(StockQuoteServiceStub stub){
        try{
            StockQuoteServiceStub.Update req = new StockQuoteServiceStub.Update();
            req.setSymbol ("ABC");
            req.setPrice (42.35);

            stub.update(req);
            System.err.println("done");
        } catch(Exception e){
            e.printStackTrace();
            System.err.println("\n\n\n");
        }
    }

    /* two way call/receive */
    public static void getPrice(StockQuoteServiceStub stub){
        try{
            StockQuoteServiceStub.GetPrice req = new StockQuoteServiceStub.GetPrice();

            req.setSymbol("ABC");

            StockQuoteServiceStub.GetPriceResponse res =
                stub.getPrice(req);

            System.err.println(res.get_return());
        } catch(Exception e){
            e.printStackTrace();
            System.err.println("\n\n\n");
        }
    }

}
