package org.apache.axis2.jaxws.client;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;

import junit.framework.TestCase;

public class ClientConfigTests extends TestCase {

    public ClientConfigTests(String name) {
        super(name);
    }
    
    public void testBadWsdlUrl() throws Exception {
        URL url = new URL("file:./test-resources/wsdl/BadEndpointAddress.wsdl");
        
        Service svc = Service.create(url, new QName("http://jaxws.axis2.apache.org", "EchoService"));
        Dispatch dispatch = svc.createDispatch(new QName("http://jaxws.axis2.apache.org", "EchoPort"), 
                String.class, Mode.PAYLOAD);
        
        try {
            dispatch.invoke("");
            
            // If an exception wasn't thrown, then it's an error.
            fail();
        } catch (WebServiceException e) {
            // We should only get a WebServiceException here.  Anything else
            // is a failure.
            System.out.println("[pass] - the proper fault type was thrown");
        }
    }
}
