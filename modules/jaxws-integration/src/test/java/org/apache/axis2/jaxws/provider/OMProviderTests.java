package org.apache.axis2.jaxws.provider;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPModelBuilder;
import org.apache.axis2.jaxws.Constants;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;

public class OMProviderTests extends ProviderTestCase {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    private QName serviceName = new QName("http://ws.apache.org/axis2", "OMProviderService");
    
        private static final String SOAP11_NS_URI = "http://schemas.xmlsoap.org/soap/envelope/";

        /**
         * SOAP 1.1 header
         */
        private static final String SOAP11_ENVELOPE_HEAD = "<?xml version='1.0' encoding='utf-8'?>"
                        + "<soapenv:Envelope xmlns:soapenv=\""
                        + SOAP11_NS_URI
                        + "\">"
                        + "<soapenv:Header />" + "<soapenv:Body>";

        /**
         * SOAP 1.1 footer
         */
        private static final String SOAP11_ENVELOPE_TAIL = "</soapenv:Body>"
                        + "</soapenv:Envelope>";


        private static String request = "<invokeOp>Hello Provider OM</invokeOp>";
        private static String SOAPFaultRequest ="<invokeOp>SOAPFault</invokeOp>";

    private static String getEndpoint() throws Exception {
        return server.getEndpoint("OMProviderService.OMProviderPort");
    }

    /**
     * Test sending a SOAP 1.2 request in MESSAGE mode
     */
    @Test
    public void testOMElementDispatchMessageMode() throws Exception {
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(serviceName);
        service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, getEndpoint());
        Dispatch<OMElement> dispatch = service.createDispatch(
                portName, OMElement.class, Mode.MESSAGE);
        
        // Create the OMElement object with the payload contents.  Since
        // we're in PAYLOAD mode, we don't have to worry about the envelope.
        StringReader sr = new StringReader(SOAP11_ENVELOPE_HEAD+request+SOAP11_ENVELOPE_TAIL);
        SOAPModelBuilder builder = OMXMLBuilderFactory.createSOAPModelBuilder(sr); 
        SOAPEnvelope om = (SOAPEnvelope) builder.getDocumentElement();

        
        OMElement response = dispatch.invoke(om);
        
        
        String responseText = response.toStringWithConsume();
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Hello Dispatch OM"));
        
    }
    
    /**
     * Test sending a SOAP 1.2 request in MESSAGE mode
     */
    @Test
    public void testOMElementDispatchMessageModeSOAPFaultException() throws Exception {
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(serviceName);
        service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, getEndpoint());
        Dispatch<OMElement> dispatch = service.createDispatch(
                portName, OMElement.class, Mode.MESSAGE);
        
        StringReader sr = new StringReader(SOAP11_ENVELOPE_HEAD+SOAPFaultRequest+SOAP11_ENVELOPE_TAIL);
        SOAPModelBuilder builder = OMXMLBuilderFactory.createSOAPModelBuilder(sr); 
        SOAPEnvelope om = (SOAPEnvelope) builder.getDocumentElement();
        OMElement response = null;
        try{
        	response = dispatch.invoke(om);
        	String responseText = response.toStringWithConsume();
        }catch(Exception e){
        	assertTrue(e instanceof SOAPFaultException);
        }
        assertTrue(response ==null);
    }
    
    @Test
    public void testOMElementDispatchMessageModeSOAPFault() throws Exception {
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(serviceName);
        service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, getEndpoint());
        Dispatch<OMElement> dispatch = service.createDispatch(
                portName, OMElement.class, Mode.MESSAGE);
        BindingProvider bp = (BindingProvider)dispatch;
        bp.getRequestContext().put(Constants.THROW_EXCEPTION_IF_SOAP_FAULT, Boolean.FALSE);
       
        StringReader sr = new StringReader(SOAP11_ENVELOPE_HEAD+SOAPFaultRequest+SOAP11_ENVELOPE_TAIL);
        SOAPModelBuilder builder = OMXMLBuilderFactory.createSOAPModelBuilder(sr); 
        SOAPEnvelope om = (SOAPEnvelope) builder.getDocumentElement();
        OMElement response = null;
        try{
        	response = dispatch.invoke(om);
        	String responseText = response.toStringWithConsume();
        }catch(Exception e){
        	fail();
        }
        assertTrue(response !=null);
        assertTrue(response instanceof OMElement);
    }
}
