package org.apache.axis2.jaxws.client.dispatch;

import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.controller.InvocationController;
import org.apache.axis2.jaxws.core.controller.InvocationControllerFactory;
import org.apache.axis2.jaxws.core.controller.impl.AxisInvocationController;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import java.util.concurrent.Future;

import junit.framework.TestCase;

public class InvocationControllerTest extends TestCase {

    private QName svcQname = new QName("http://test", "TestService");
    private QName portQname = new QName("http://test", "TestPort");
    
    public void testDefaultInvocationController() {
        Service svc = Service.create(svcQname);
        svc.addPort(portQname, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost");
        Dispatch d = svc.createDispatch(portQname, Source.class, Service.Mode.PAYLOAD);
        
        BaseDispatch bd = (BaseDispatch) d;
        
        assertTrue("An InvocationController instance was not created", bd.ic != null);
        assertTrue("The default InvocationController type was incorrect.", 
            AxisInvocationController.class.isAssignableFrom(bd.ic.getClass()));
    }
    
    public void testPluggableInvocationController() {
        FactoryRegistry.setFactory(InvocationControllerFactory.class, new TestInvocationControllerFactory());
        
        Service svc = Service.create(svcQname);
        svc.addPort(portQname, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost");
        Dispatch d = svc.createDispatch(portQname, Source.class, Service.Mode.PAYLOAD);
        
        BaseDispatch bd = (BaseDispatch) d;
        
        assertTrue("An InvocationController instance was not created", bd.ic != null);
        assertTrue("The default InvocationController type was incorrect.", 
            TestInvocationController.class.isAssignableFrom(bd.ic.getClass()));
    }
}

class TestInvocationControllerFactory implements InvocationControllerFactory {
    public InvocationController getInvocationController() {
        return new TestInvocationController(); 
    }
}

class TestInvocationController implements InvocationController {

    public InvocationContext invoke(InvocationContext ic) {
        return null;
    }

    public Future<?> invokeAsync(InvocationContext ic, AsyncHandler asyncHandler) {
        return null;
    }

    public Response invokeAsync(InvocationContext ic) {
        return null;
    }

    public void invokeOneWay(InvocationContext ic) throws Exception {}    
}
