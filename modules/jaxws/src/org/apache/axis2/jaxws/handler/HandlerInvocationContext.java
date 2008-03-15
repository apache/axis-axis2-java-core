package org.apache.axis2.jaxws.handler;

import org.apache.axis2.jaxws.core.MessageContext;

import javax.xml.ws.handler.Handler;
import java.util.List;

/**
 * This data bean will be passed to the HandlerInvoker instance.
 * The bean will contain the necessary data in order to invoke
 * either inbound or outbound Handler instances for a given request.
 *
 */
public class HandlerInvocationContext {
    
    private MessageContext messageContext;
    
    private HandlerChainProcessor.MEP mep;
    
    private List<Handler> handlers;
    
    private boolean isOneWay;

    public boolean isOneWay() {
        return isOneWay;
    }

    public void setOneWay(boolean isOneWay) {
        this.isOneWay = isOneWay;
    }

    public MessageContext getMessageContext() {
        return messageContext;
    }

    public void setMessageContext(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    public HandlerChainProcessor.MEP getMEP() {
        return mep;
    }

    public void setMEP(HandlerChainProcessor.MEP mep) {
        this.mep = mep;
    }

    public List<Handler> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<Handler> handlers) {
        this.handlers = handlers;
    }

}
