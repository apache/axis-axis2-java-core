package samples.quickstart.clients;

import samples.quickstart.service.jibx.StockQuoteServiceStub;

public class JiBXClient{
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
            stub.update("ABC", new Double(42.35));
            System.err.println("done");
        } catch(Exception e){
            e.printStackTrace();
            System.err.println("\n\n\n");
        }
    }

    /* two way call/receive */
    public static void getPrice(StockQuoteServiceStub stub){
        try{
            System.err.println(stub.getPrice("ABC"));
        } catch(Exception e){
            e.printStackTrace();
            System.err.println("\n\n\n");
        }
    }

}
