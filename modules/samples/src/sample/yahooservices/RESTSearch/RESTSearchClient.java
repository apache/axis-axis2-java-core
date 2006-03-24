package sample.yahooservices.RESTSearch;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.RESTCall;

public class RESTSearchClient {
    public static void main(String[] args) {
        try {

            String epr = "http://api.search.yahoo.com/WebSearchService/V1/webSearch?appid=ApacheRestDemo&query=finances&format=pdf";

            RESTCall call = new RESTCall();
            Options options = new Options();
            call.setOptions(options);
            options.setTo(new EndpointReference(epr));
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            options.setProperty(Constants.Configuration.ENABLE_REST_THROUGH_GET, Constants.VALUE_TRUE);

            //if post is through GET of HTTP
            OMElement response = call.sendReceive();
            response.serialize(System.out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

