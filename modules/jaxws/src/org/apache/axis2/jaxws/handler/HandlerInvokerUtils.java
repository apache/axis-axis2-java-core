package org.apache.axis2.jaxws.handler;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.context.factory.MessageContextFactory;
import org.apache.axis2.jaxws.context.utils.ContextUtils;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.impl.EndpointLifecycleManagerImpl;

public class HandlerInvokerUtils {

    /**
     * Invoke Inbound Handlers
     * @param requestMsgCtx
     */
    public static boolean invokeInboundHandlers(MessageContext msgCtx,
            EndpointDescription endpointDesc, HandlerChainProcessor.MEP mep, boolean isOneWay) {
        List<String> handlers = endpointDesc.getHandlerList();

        // TODO MIKE TEST -- REMOVE - for testing until we get a list of objects from EndpointDescription.getHandlerList();
        /*
        if (endpointDesc.getServiceQName().getLocalPart().contains("AddNumber"))
            handlers.add("org.apache.axis2.jaxws.sample.addnumbers.AddNumbersLogicalHandler");
        */
        // TODO END MIKE TEST

        int numHandlers = handlers.size();
        javax.xml.ws.handler.MessageContext handlerMessageContext = null;

        if (numHandlers > 0) {
            handlerMessageContext = findOrCreateMessageContext(msgCtx);
        } else {
            return true;
        }

        // TODO remove this.  Handlers will have already been instantiated when
        // we start using the handlerresolver to get our list.
        ArrayList<Handler> handlerInstances = createHandlerInstances(endpointDesc);

        HandlerChainProcessor processor = new HandlerChainProcessor(
                handlerInstances);

        // if not one-way, expect a response
        if (msgCtx.getMessage().isFault()) {
                processor.processFault(handlerMessageContext,
                    HandlerChainProcessor.Direction.IN);
        } else {
                handlerMessageContext = processor.processChain(handlerMessageContext,
                                HandlerChainProcessor.Direction.IN,
                                mep,
                                !isOneWay);
        }

        if (handlerMessageContext.get(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY).equals(true)
                        && mep.equals(HandlerChainProcessor.MEP.REQUEST)) {
            // uh-oh.  We've changed directions on the server inbound handler processing,
            // This means we're now on an outbound flow, and the endpoint will not
            // be called.  Be sure to mark the context and message as such.
            try {
                Message msg = ((MessageFactory)FactoryRegistry.getFactory(MessageFactory.class)).createFrom(((SOAPMessageContext)handlerMessageContext).getMessage());
                msgCtx.setMessage(msg);
                return false;
            } catch (XMLStreamException e) {
                // TODO log it
                throw ExceptionFactory.makeWebServiceException(e);
            }
        }

        return true;
    }

    /**
     * Invoke OutboundHandlers
     * 
     * @param msgCtx
     */
    public static boolean invokeOutboundHandlers(MessageContext msgCtx,
            EndpointDescription endpointDesc, HandlerChainProcessor.MEP mep, boolean isOneWay) {
        List<String> handlers = endpointDesc.getHandlerList();

        // TODO you may need to hard-code add some handlers until we
        // actually have useful code under EndpointDescription.getHandlerList()
        int numHandlers = handlers.size();

        javax.xml.ws.handler.MessageContext handlerMessageContext = null;
        if (numHandlers > 0) {
            handlerMessageContext = findOrCreateMessageContext(msgCtx);
        } else {
            return true;
        }

        // TODO probably don't want to make the newInstances here -- use
        // RuntimeDescription instead?
        // make instances of all the handlers
        ArrayList<Handler> handlerInstances = createHandlerInstances(endpointDesc);

        HandlerChainProcessor processor = new HandlerChainProcessor(
                handlerInstances);

        // if not one-way, expect a response
        if (msgCtx.getMessage().isFault()) {
                processor.processFault(handlerMessageContext,
                    HandlerChainProcessor.Direction.OUT);
        } else {
                handlerMessageContext = processor.processChain(handlerMessageContext,
                                HandlerChainProcessor.Direction.OUT,
                                mep, !isOneWay);
        }

        if (handlerMessageContext.get(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY).equals(false)
                        && mep.equals(HandlerChainProcessor.MEP.REQUEST)) {
                // uh-oh.  We've changed directions on the client outbound handler processing,
                // This means we're now on an inbound flow, and the service will not
                // be called.  Be sure to mark the context and message as such.
                try {
                        Message msg = ((MessageFactory)FactoryRegistry.getFactory(MessageFactory.class)).createFrom(((SOAPMessageContext)handlerMessageContext).getMessage());
                        msgCtx.setMessage(msg);
                        return false;
                } catch (XMLStreamException e) {
                        // TODO log it
                        throw ExceptionFactory.makeWebServiceException(e);
                }
        }

        return true;
    }

    /**
     * Find or Create Handler Message Context
     * @param mc
     * @return javax.xml.ws.handler.MessageContext
     */
    private static javax.xml.ws.handler.MessageContext findOrCreateMessageContext(MessageContext mc) {
        // See if a soap message context is already present on the WebServiceContext
        javax.xml.ws.handler.MessageContext handlerMessageContext = null;
        ServiceContext serviceContext = mc.getAxisMessageContext().getServiceContext();
        WebServiceContext ws = (WebServiceContext)serviceContext.getProperty(EndpointLifecycleManagerImpl.WEBSERVICE_MESSAGE_CONTEXT);

        if (ws != null) {
            handlerMessageContext = ws.getMessageContext();
        }

        if (handlerMessageContext == null) {
            handlerMessageContext = createSOAPMessageContext(mc);
        }

        return handlerMessageContext; 
    }

    /**
     * @param mc
     * @return new SOAPMessageContext
     */
    private static javax.xml.ws.handler.MessageContext createSOAPMessageContext(MessageContext mc){
        SoapMessageContext soapMessageContext = (SoapMessageContext)MessageContextFactory.createSoapMessageContext(mc);
        ContextUtils.addProperties(soapMessageContext, mc);
        return soapMessageContext;
     }

    // TODO method is for TEST only.  instances will be created elsewhere
    private static ArrayList<Handler> createHandlerInstances(EndpointDescription ed) {
        // TODO remove this.  Handlers will have already been instantiated when
        // we start using the handlerresolver to get our list.

        List<String> handlers = ed.getHandlerList();
        int numHandlers = handlers.size();

        ArrayList<Handler> handlerInstances = new ArrayList<Handler>();
        try {
                for (int i = 0; i < numHandlers; i++) {
                        handlerInstances.add((Handler) Class.forName(handlers.get(i)).newInstance());
                }
        } catch (Exception e) {
                e.printStackTrace();
        }

        return handlerInstances;
    }

}


