package sample.yahooservices.RESTSearch;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.clientapi.RESTCall;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.OMOutputImpl;

import java.net.URL;


/**
 * Created by IntelliJ IDEA.
 * User: saminda
 * Date: Jul 11, 2005
 * Time: 2:25:56 PM
 * To change this template use File | Settings | File Templates.
 */
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

