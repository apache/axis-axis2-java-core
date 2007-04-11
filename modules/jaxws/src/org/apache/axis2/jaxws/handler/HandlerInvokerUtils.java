/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.handler;

import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.context.factory.MessageContextFactory;
import org.apache.axis2.jaxws.context.utils.ContextUtils;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.impl.EndpointLifecycleManagerImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.List;

public class HandlerInvokerUtils {

    /**
     * Invoke Inbound Handlers
     *
     * @param requestMsgCtx
     */
    public static boolean invokeInboundHandlers(MessageContext msgCtx,
            List<Handler> handlers, EndpointDescription endpointDesc, HandlerChainProcessor.MEP mep, boolean isOneWay) {
        
        if (handlers == null)
            return true;
        
        int numHandlers = handlers.size();

        javax.xml.ws.handler.MessageContext handlerMessageContext = null;
        if (numHandlers > 0) {
            handlerMessageContext = findOrCreateMessageContext(msgCtx);
        } else {
            return true;
        }

        String bindingProto = null;
        if (mep.equals(HandlerChainProcessor.MEP.REQUEST))  // inbound request; must be on the server
            bindingProto = endpointDesc.getBindingType();
        else // inbound response; must be on the client
            bindingProto = endpointDesc.getClientBindingID();
        Protocol proto = Protocol.getProtocolForBinding(bindingProto);
        
        HandlerChainProcessor processor = new HandlerChainProcessor(handlers, proto);
        // if not one-way, expect a response
        try {
            if (msgCtx.getMessage().isFault()) {
                processor.processFault(handlerMessageContext,
                                       HandlerChainProcessor.Direction.IN);
            } else {
        		processor.processChain(handlerMessageContext,
                                                               HandlerChainProcessor.Direction.IN,
                                                               mep,
                                                               !isOneWay);
            }
        } catch (RuntimeException re) {
            /*
                * handler framework should only throw an exception here if
                * we are in the client inbound case.  Make sure the message
                * context and message are transformed.
                */
        	HandlerChainProcessor.convertToFaultMessage(handlerMessageContext, re, proto);
            addConvertedFaultMsgToCtx(msgCtx, handlerMessageContext);
            return false;
        }

        if (handlerMessageContext.get(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY)
                .equals(true)
                && mep.equals(HandlerChainProcessor.MEP.REQUEST)) {
            // uh-oh.  We've changed directions on the server inbound handler processing,
            // This means we're now on an outbound flow, and the endpoint will not
            // be called.  Be sure to mark the context and message as such.
            addConvertedFaultMsgToCtx(msgCtx, handlerMessageContext);
            return false;
        }
        return true;

    }

    /**
     * Invoke OutboundHandlers
     *
     * @param msgCtx
     */
    public static boolean invokeOutboundHandlers(MessageContext msgCtx,
            List<Handler> handlers, EndpointDescription endpointDesc, HandlerChainProcessor.MEP mep, boolean isOneWay) {
        
        if (handlers == null)
            return true;
        
        int numHandlers = handlers.size();

        javax.xml.ws.handler.MessageContext handlerMessageContext = null;
        if (numHandlers > 0) {
            handlerMessageContext = findOrCreateMessageContext(msgCtx);
        } else {
            return true;
        }
        
        String bindingProto = null;
        if (mep.equals(HandlerChainProcessor.MEP.REQUEST))  // outbound request; must be on the client
            bindingProto = endpointDesc.getClientBindingID();
        else // outbound response; must be on the server
            bindingProto = endpointDesc.getBindingType();
        Protocol proto = Protocol.getProtocolForBinding(bindingProto);
        
        HandlerChainProcessor processor = new HandlerChainProcessor(handlers, proto);
        // if not one-way, expect a response
        try {
            if (msgCtx.getMessage().isFault()) {
                processor.processFault(handlerMessageContext,
                                       HandlerChainProcessor.Direction.OUT);
            } else {
        		processor.processChain(handlerMessageContext,
                                                               HandlerChainProcessor.Direction.OUT,
                                                               mep, !isOneWay);
            }
        } catch (RuntimeException re) {
            /*
                * handler framework should only throw an exception here if
                * we are in the server outbound case.  Make sure the message
                * context and message are transformed.
                */
        	HandlerChainProcessor.convertToFaultMessage(handlerMessageContext, re, proto);
            addConvertedFaultMsgToCtx(msgCtx, handlerMessageContext);
            return false;
        }

        if (handlerMessageContext.get(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY)
                .equals(false)
                && mep.equals(HandlerChainProcessor.MEP.REQUEST)) {
            // uh-oh.  We've changed directions on the client outbound handler processing,
            // This means we're now on an inbound flow, and the service will not
            // be called.  Be sure to mark the context and message as such.
            addConvertedFaultMsgToCtx(msgCtx, handlerMessageContext);
            return false;
        }
        return true;

    }

    /**
     * Find or Create Handler Message Context
     *
     * @param mc
     * @return javax.xml.ws.handler.MessageContext
     */
    private static javax.xml.ws.handler.MessageContext findOrCreateMessageContext(
            MessageContext mc) {
        // See if a soap message context is already present on the WebServiceContext
        javax.xml.ws.handler.MessageContext handlerMessageContext = null;
        ServiceContext serviceContext = mc.getAxisMessageContext().getServiceContext();
        // there's no such thing as a serviceContext on the client? -- that was my experience, anyway
        if (serviceContext != null) {
            WebServiceContext ws = (WebServiceContext)serviceContext.getProperty(EndpointLifecycleManagerImpl.WEBSERVICE_MESSAGE_CONTEXT);
            if (ws != null) {
                handlerMessageContext = ws.getMessageContext();
            }
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
    private static javax.xml.ws.handler.MessageContext createSOAPMessageContext(MessageContext mc) {
        SoapMessageContext soapMessageContext =
                (SoapMessageContext)MessageContextFactory.createSoapMessageContext(mc);
        ContextUtils.addProperties(soapMessageContext, mc);
        return soapMessageContext;
    }


    private static void addConvertedFaultMsgToCtx(MessageContext msgCtx,
                                                  javax.xml.ws.handler.MessageContext handlerMsgCtx) {
        try {
            Message msg = ((MessageFactory)FactoryRegistry.getFactory(MessageFactory.class))
                    .createFrom(((SOAPMessageContext)handlerMsgCtx).getMessage());
            msgCtx.setMessage(msg);
        } catch (XMLStreamException e) {
            // TODO log it
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
    
}
