package org.apache.axis2.rest;

import java.io.ByteArrayInputStream;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Call;
import org.apache.axis2.engine.util.TestConstants;
//import org.apache.axis2.clientapi.RESTCall;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

public class GetTest extends TestCase implements TestConstants {

	public void testRESTGet() throws Exception{

            String epr = "http://localhost:8080/axis2/services/MyService";
        	
            String  xml = 
            	"<echo>"+
				"<Text>Hello</Text>"+
				"</echo>";
        	
        	byte arr[] = xml.getBytes();
        	ByteArrayInputStream bais = new ByteArrayInputStream(arr);
        	
        	XMLStreamReader reader = null;
        	try {
        		XMLInputFactory xif= XMLInputFactory.newInstance();
        		reader= xif.createXMLStreamReader(bais);
        	} catch (XMLStreamException e) {
        		e.printStackTrace();
        	}
        	StAXOMBuilder builder= new StAXOMBuilder(reader);
        	OMElement data = builder.getDocumentElement();
            
        	OMFactory fac = OMAbstractFactory.getOMFactory();
            /*OMNamespace omNs = fac.createOMNamespace("http://example1.org/example1", "example1");
            OMElement payload = fac.createOMElement("echo", omNs);
            OMElement value = fac.createOMElement("Text", omNs);
            value.addChild(fac.createText(value, "Hello"));
            payload.addChild(value);*/

            //RESTCall call = new RESTCall();
        	//OMElement val = fac.
            Call call = new Call();
            call.setTo(new EndpointReference(epr));
            call.setTransportInfo(Constants.TRANSPORT_HTTP,Constants.TRANSPORT_HTTP, false);
            call.set(Constants.Configuration.ENABLE_REST,Constants.VALUE_TRUE);
            call.set(Constants.Configuration.ENABLE_REST_THROUGH_GET,Constants.VALUE_TRUE);

            //if post is through GET of HTTP
            //OMElement response = call.invokeBlocking("webSearch",data);
            OMElement response = call.invokeBlocking("webSearch",data);
            //OMElement response = call.invokeBlocking();  
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
            response.serialize(new OMOutputImpl(writer));
            writer.flush();
		}
}

