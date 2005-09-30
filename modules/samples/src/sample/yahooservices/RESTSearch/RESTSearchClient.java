package sample.yahooservices.RESTSearch;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.RESTCall;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.OMOutputImpl;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

public class RESTSearchClient {
    public static void main(String[] args) {
        try{

            String epr = "http://api.search.yahoo.com/WebSearchService/V1/webSearch?appid=ApacheRestDemo&query=finances&format=pdf";

            RESTCall call = new RESTCall();
            call.setTo(new EndpointReference(epr));
            call.setTransportInfo(Constants.TRANSPORT_HTTP,Constants.TRANSPORT_HTTP, false);
            call.setDoREST(true);
            call.setRestThroughPOST(false);

            //if post is through GET of HTTP
            OMElement response = call.invokeBlocking();
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
            response.serializeWithCache(new OMOutputImpl(writer));
            writer.flush();

        }catch(Exception e){
            e.printStackTrace();
        }
    }



}

