package sample.yahooservices.RESTSearch;

import org.apache.axis2.clientapi.Call;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.Constants;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.OMOutputImpl;


import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;


/**
 * Created by IntelliJ IDEA.
 * User: saminda
 * Date: Jul 11, 2005
 * Time: 2:25:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class RESTSearchClient {
    private static String eprGet =
            "http://api.search.yahooservices.com/WebSearchService/V1/webSearch?appid=ApacheRestDemo&query=finances&format=pdf";



    public static void main(String[] args) {
        try{
            Call call = new Call();
            call.setTo(new EndpointReference(eprGet));
            call.setTransportInfo(Constants.TRANSPORT_COMMONS_HTTP,Constants.TRANSPORT_HTTP, false);
            call.setDoREST(true);
            call.setRestThroughPOST(false);

            //if post is through GET of HTTP
            OMElement response = call.invokeBlocking("",OMAbstractFactory.getOMFactory().createOMElement("","",""));

            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
            response.serializeWithCache(new OMOutputImpl(writer));
            writer.flush();

        }catch(Exception e){
            e.printStackTrace();
        }
    }



}
