package org.apache.axis2.jaxws.handler.impl;

import org.apache.axis2.jaxws.handler.HandlerInvocationContext;
import org.apache.axis2.jaxws.handler.HandlerInvoker;
import org.apache.axis2.jaxws.handler.HandlerInvokerUtils;

/**
 * This class will be responsible for driving both inbound and
 * outbound handler chains for an endpoint invocation.
 *
 */
public class HandlerInvokerImpl implements HandlerInvoker {

    /**
     * This invokes the inbound handlers for the invocation.
     */
    public boolean invokeInboundHandlers(HandlerInvocationContext context) {
        return HandlerInvokerUtils.invokeInboundHandlers(context.getMessageContext().
                                                  getMEPContext(), context.getHandlers(), 
                                                  context.getMEP(), context.isOneWay());
    }

    /**
     * This invokes the outbound handlers for the invocation.
     */
    public boolean invokeOutboundHandlers(HandlerInvocationContext context) {
        return HandlerInvokerUtils.invokeOutboundHandlers(context.getMessageContext().
                                                         getMEPContext(), context.getHandlers(), 
                                                         context.getMEP(), context.isOneWay());
    }
    
}
