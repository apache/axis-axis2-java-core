package org.apache.axis2.rest;

import java.io.ByteArrayInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.impl.llom.builder.StAXOMBuilder;

//This Sample test Client is written for Yahoo Web Search

public class RESTGetTest extends TestCase implements TestConstants {

    public void testRESTGet() throws Exception {
        String epr = "http://api.search.yahoo.com/WebSearchService/V1/webSearch";

        String xml =
                "<websearch>" +
                        "<appid>ApacheRestDemo</appid>" +
                        "<query>finances</query>" +
                        "<format>pdf</format>" +
                        "</websearch>";

        byte arr[] = xml.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(arr);

        XMLStreamReader reader = null;
        try {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            reader = xif.createXMLStreamReader(bais);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        StAXOMBuilder builder = new StAXOMBuilder(reader);
        OMElement data = builder.getDocumentElement();
        Options options = new Options();
        options.setTo(new EndpointReference(epr));
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
        options.setProperty(Constants.Configuration.ENABLE_REST_THROUGH_GET, Constants.VALUE_TRUE);

        //if post is through GET of HTTP

        ServiceClient sender = new ServiceClient();
        sender.setOptions(options);
        options.setTo(targetEPR);
        OMElement response = sender.sendReceive(data);

        response.serialize(System.out);
    }
}
