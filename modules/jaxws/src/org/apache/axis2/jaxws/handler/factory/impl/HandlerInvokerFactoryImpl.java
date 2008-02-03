package org.apache.axis2.jaxws.handler.factory.impl;

import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.handler.HandlerInvoker;
import org.apache.axis2.jaxws.handler.factory.HandlerInvokerFactory;
import org.apache.axis2.jaxws.handler.impl.HandlerInvokerImpl;

/**
 * This is the default implementation of the HandlerInvokerFactory, 
 * and it will be registered with the FactoryRegistry.
 *
 */
public class HandlerInvokerFactoryImpl implements HandlerInvokerFactory {

    public HandlerInvoker createHandlerInvoker(MessageContext messageContext) {
        return new HandlerInvokerImpl();
    }
    
}
