package org.apache.axis2.jaxws.client;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.TestLogger;

public class ClientConfigTests extends TestCase {

    public ClientConfigTests(String name) {
        super(name);
    }
    
    public void testBadWsdlUrl() throws Exception {
        
        URL url = null;
        String wsdlLocation = null;
        try {
            try{
                String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
                wsdlLocation = new File(baseDir + "/test-resources/wsdl/BadEndpointAddress.wsdl").getAbsolutePath();
            }catch(Exception e){
                e.printStackTrace();
            }
            File file = new File(wsdlLocation);
            url = file.toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        
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
            TestLogger.logger.debug("[pass] - the proper fault type was thrown");
        }
    }
}
