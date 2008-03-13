package org.apache.axis2.jaxws.handler.header.tests;

import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.axis2.jaxws.framework.AbstractTestCase;

public class HandlerTests extends AbstractTestCase {
    public static Test suite() {
        return getTestSetup(new TestSuite(HandlerTests.class));
    }
    
    public void testHandler_getHeader_invocation(){
        System.out.println("----------------------------------");
        System.out.println("test: " + getName());
        Object res = null;
        //Add myHeader to SOAPMessage that will be injected by handler.getHeader().
        String soapMessage = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:demo=\"http://demo/\"><soap:Header> <demo:myheader soap:mustUnderstand=\"1\"/></soap:Header><soap:Body><demo:echo><arg0>test</arg0></demo:echo></soap:Body></soap:Envelope>";
        String url = "http://localhost:6060/axis2/services/DemoService.DemoServicePort";
        QName name = new QName("http://demo/", "DemoService");
        QName portName = new QName("http://demo/", "DemoServicePort");
        try{
                //Create Service
                Service s = Service.create(name);
                assertNotNull(s);
                //add port
                s.addPort(portName, null, url);
                
                //Create Dispatch
                Dispatch<String> dispatch = s.createDispatch(portName, String.class, Service.Mode.MESSAGE);
                assertNotNull(dispatch);
                res = dispatch.invoke(soapMessage);
                assertNotNull(res);
                System.out.println("----------------------------------");
        }catch(Exception e){
                e.printStackTrace();
                fail();
        }       
    }
    
    public void test_MU_Failure(){
        System.out.println("----------------------------------");
        System.out.println("test: " + getName());
        Object res = null;
        //Add bad header to SOAPMessage, we expect MU to fail 
        String soapMessage = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:demo=\"http://demo/\"><soap:Header> <demo:badHeader soap:mustUnderstand=\"1\"/></soap:Header><soap:Body><demo:echo><arg0>test</arg0></demo:echo></soap:Body></soap:Envelope>";
        String url = "http://localhost:6060/axis2/services/DemoService.DemoServicePort";
        QName name = new QName("http://demo/", "DemoService");
        QName portName = new QName("http://demo/", "DemoServicePort");
        try{
                //Create Service
                Service s = Service.create(name);
                assertNotNull(s);
                //add port
                s.addPort(portName, null, url);                
                //Create Dispatch
                Dispatch<String> dispatch = s.createDispatch(portName, String.class, Service.Mode.MESSAGE);
                assertNotNull(dispatch);
                res = dispatch.invoke(soapMessage);
                System.out.println("Expecting SOAPFaultException with MustUnderstand failed");
                fail();
                System.out.println("----------------------------------");
        }catch(SOAPFaultException e){       	
            e.printStackTrace();
            System.out.println("MustUnderstand failed as exptected");
            System.out.println("----------------------------------");
                
        }       
    }
}
