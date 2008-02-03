package org.apache.axis2.jaxws.handler.factory;

import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.handler.HandlerInvoker;

/**
 * Implementations of this interface is called by the EndpointController 
 * in JAX-WS in order to serve up an instance of a HandlerInvoker. 
 * Implementations can be registered via the FactoryRegistry.
 *
 */
public interface HandlerInvokerFactory {
    
    /**
     * Returns an instance of the HandlerInvoker class.
     */
    public HandlerInvoker createHandlerInvoker(MessageContext messageContext);

}
