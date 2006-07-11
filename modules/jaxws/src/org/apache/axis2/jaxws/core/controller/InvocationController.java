package org.apache.axis2.jaxws.core.controller;

import java.util.concurrent.Future;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.apache.axis2.jaxws.core.InvocationContext;

public interface InvocationController {
    
    public InvocationContext invoke(InvocationContext ic);
    
    public void invokeOneWay(InvocationContext ic);
    
    public Response invokeAsync(InvocationContext ic);
    
    public Future<?> invokeAsync(InvocationContext ic, AsyncHandler asyncHandler);
}
