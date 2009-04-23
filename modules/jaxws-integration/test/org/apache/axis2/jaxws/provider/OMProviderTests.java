package org.apache.axis2.jaxws.provider;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;

import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestSuite;

public class OMProviderTests extends ProviderTestCase {

    String endpointUrl = "http://localhost:6060/axis2/services/OMProviderService.OMProviderPort";
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
        private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        
   public static Test suite() {
      return getTestSetup(new TestSuite(OMProviderTests.class));
   }
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    /**
     * Test sending a SOAP 1.2 request in MESSAGE mode
     */
    public void testOMElementDispatchMessageMode() throws Exception {
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(serviceName);
        service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, endpointUrl);
        Dispatch<OMElement> dispatch = service.createDispatch(
                portName, OMElement.class, Mode.MESSAGE);
        
        // Create the OMElement object with the payload contents.  Since
        // we're in PAYLOAD mode, we don't have to worry about the envelope.
        StringReader sr = new StringReader(SOAP11_ENVELOPE_HEAD+request+SOAP11_ENVELOPE_TAIL);
        XMLStreamReader inputReader = inputFactory.createXMLStreamReader(sr);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inputReader, null); 
        SOAPEnvelope om = (SOAPEnvelope) builder.getDocumentElement();

        
        OMElement response = dispatch.invoke(om);
        
        
        String responseText = response.toStringWithConsume();
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Hello Dispatch OM"));
        
    }
}
