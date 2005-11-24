/*
 * Created on Nov 24, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis2.rest;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Call;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;

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

        Call call = new Call();
        call.setTo(new EndpointReference(epr));
        call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);
        call.set(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
        call.set(Constants.Configuration.ENABLE_REST_THROUGH_GET, Constants.VALUE_TRUE);

        //if post is through GET of HTTP
        OMElement response = call.invokeBlocking("webSearch", data);
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
        response.serialize(new OMOutputImpl(writer));
        writer.flush();
    }
}
