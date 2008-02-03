package org.apache.axis2.jaxws.handler;


/**
 * This interface represents a class that will be called by the
 * JAX-WS EndpointController to handle the invocation of both
 * inbound and outbound handlers. Instances of these will be
 * served up by the HandlerInvokerFactory.
 *
 */
public interface HandlerInvoker {

    /**
     * Invokes all inbound handlers for the incoming request to the endpoint.
     */
    public boolean invokeInboundHandlers(HandlerInvocationContext context);
    
    /**
     * Invokes all inbound handlers for the incoming request to the endpoint.
     */
    public boolean invokeOutboundHandlers(HandlerInvocationContext context);
    
}
