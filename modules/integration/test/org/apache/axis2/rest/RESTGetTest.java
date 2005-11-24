/*
 * Created on Nov 24, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis2.rest;

import java.io.ByteArrayInputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Call;
//import org.apache.axis2.client.*;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;

/**
 * @author Thilini
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

//This Sample test Client is written for Yahoo Web Search

public class RESTGetTest extends TestCase implements TestConstants{
	
	public void testRESTGet(){
		//String epr = "http://api.search.yahoo.com/WebSearchService/V1/webSearch?appid=ApacheRestDemo&query=finances&format=pdf";
    	//String epr = "http://127.0.0.1:8080/WebSearchService/V1/webSearch";
    	//String epr = "http://webservices.amazon.com/onca/xml";
    	String epr = "http://api.search.yahoo.com/WebSearchService/V1/webSearch";
    	
        String  xml = 
        	"<websearch>"+
			"<appid>ApacheRestDemo</appid>"+
			"<query>finances</query>"+
			"<format>pdf</format>"+
			"</websearch>";
    	
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

        try {
			//RESTCall call = new RESTCall();
			Call call = new Call();
			call.setTo(new EndpointReference(epr));
			call.setTransportInfo(Constants.TRANSPORT_HTTP,Constants.TRANSPORT_HTTP, false);
			call.set(Constants.Configuration.ENABLE_REST,Constants.VALUE_TRUE);
			call.set(Constants.Configuration.ENABLE_REST_THROUGH_GET,Constants.VALUE_TRUE);

			//if post is through GET of HTTP
			OMElement response = call.invokeBlocking("webSearch",data);
			//OMElement response = call.invokeBlocking();  
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
			response.serialize(new OMOutputImpl(writer));
			writer.flush();
		} catch (AxisFault e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (XMLStreamException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryConfigurationError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}   
	}
}
