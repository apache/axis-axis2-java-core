package org.apache.axis2.jaxws;

import java.util.concurrent.Future;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import junit.framework.TestCase;
import client.EchoString;
import client.EchoStringResponse;
import client.ObjectFactory;

public class JAXBDispatch extends TestCase {

    private Dispatch<Object> dispatch;
    private EchoString request;
    
    public JAXBDispatch(String name) {
        super(name);
    }
    
    public void setUp() {
        //Create the Service object
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        
        //Create the JAX-B Dispatch object
        JAXBContext jbc = null;
        try {
            jbc = JAXBContext.newInstance("client");
            dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                    jbc, Service.Mode.PAYLOAD);
        } catch (Exception e) {
            e.printStackTrace();
            fail("[ERROR] - could not create JAXBContext");
        }
        
        //Create the input param
        ObjectFactory factory = new ObjectFactory();
        request = factory.createEchoString(); 
    }
    
    public void testSync() {
        System.out.println("test: " + getName());
 
        request.setInput("SYNC JAXB TEST");
        try {
            System.out.println(">> Invoking sync Dispatch with JAX-B Parameter");
            EchoStringResponse response = (EchoStringResponse) dispatch.invoke(request);
            
            assertTrue("[ERROR] - Response object was null", response != null);
            assertTrue("[ERROR] - No content in response object", response.getEchoStringReturn() != null);
            assertTrue("[ERROR] - Zero length content in response", response.getEchoStringReturn().length() > 0);
            
            System.out.println(">> Response [" + response.getEchoStringReturn() + "]");
        } catch(WebServiceException e) {
            e.printStackTrace();
            fail("[ERROR] - Sync Dispatch invocation failed");
        }
    }
    
    public void testAysnc() {
        System.out.println("test: " + getName());
        
        request.setInput("ASYNC(CALLBACK) JAXB TEST");
        try {
            JAXBCallbackHandler<Object> callback = new JAXBCallbackHandler<Object>();
            
            System.out.println(">> Invoking async(callback) Dispatch with JAX-B Parameter");
            Future<?> monitor = dispatch.invokeAsync(request, callback);
            
            while (!monitor.isDone()) {
                 System.out.println(">> Async invocation still not complete");
                 Thread.sleep(1000);
            }
        } catch(Exception e) {
            e.printStackTrace();
            fail("[ERROR] - Async(callback) Dispatch invocation failed");
        }
    }
    
    public void testOneWay() {
        System.out.println("test: " + getName());

        request.setInput("ONE-WAY JAXB TEST");
        try {
            System.out.println(">> Invoking one-way Dispatch with JAX-B Parameter");
            dispatch.invokeOneWay(request);
        } catch(WebServiceException e) {
            e.printStackTrace();
            fail("[ERROR] - One-way Dispatch invocation failed");
        }
    }
}
