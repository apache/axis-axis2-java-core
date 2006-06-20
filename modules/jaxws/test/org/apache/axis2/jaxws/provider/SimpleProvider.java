package org.apache.axis2.jaxws.provider;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.apache.axis2.jaxws.framework.StartServer;
import org.apache.axis2.jaxws.framework.StopServer;

import junit.framework.TestCase;

public class SimpleProvider extends TestCase {

    String endpointUrl = "http://localhost:8080/axis2/services/SimpleProviderService";
    String xmlString = "<invoke>test input</invoke>";
    private QName serviceName = new QName("http://ws.apache.org/axis2", "SimpleProviderService");
    private QName portName = new QName("http://ws.apache.org/axis2", "SimpleProviderServiceSOAP11port0");
    
	protected void setUp() throws Exception {
		super.setUp();
		StartServer startServer = new StartServer("server1");
		startServer.testStartServer();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		StopServer stopServer = new StopServer("server1");
		stopServer.testStopServer();
	}
	
    public SimpleProvider(String name) {
        super(name);
    }
    
    public void testProvider() {
        System.out.println("---------------------------------------");
        Service svc = Service.create(serviceName);
        svc.addPort(portName,null, null);
        Dispatch<String> dispatch = svc
                .createDispatch(portName, String.class, null);
        Map<String, Object> requestContext = dispatch.getRequestContext();
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpointUrl);
        System.out.println(">> Invoking SimpleProvider");
        String retVal = dispatch.invoke(xmlString);
        System.out.println(">> Response [" + retVal + "]");
    }
}
