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

import java.util.ArrayList;

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
   
        HandlerResolverImpl hResolver = new HandlerResolverImpl(endpointDesc);
        ArrayList<Handler> handlers = hResolver.getHandlerChain(endpointDesc.getPortInfo());
        
        int numHandlers = handlers.size();

        javax.xml.ws.handler.MessageContext handlerMessageContext = null;
        if (numHandlers > 0) {
            handlerMessageContext = findOrCreateMessageContext(msgCtx);
        } else {
            return true;
        }

        // TODO remove this.  Handlers will have already been instantiated when
        // we start using the handlerresolver to get our list.
        //ArrayList<Handler> handlerInstances = createHandlerInstances(endpointDesc);

        HandlerChainProcessor processor = new HandlerChainProcessor(
                handlers);
        // if not one-way, expect a response
        try {
        	if (msgCtx.getMessage().isFault()) {
        		processor.processFault(handlerMessageContext,
        				HandlerChainProcessor.Direction.IN);
        	} else {
        		handlerMessageContext = processor.processChain(handlerMessageContext,
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
        	HandlerChainProcessor.convertToFaultMessage(handlerMessageContext, re);
        	addConvertedFaultMsgToCtx(msgCtx, handlerMessageContext);
        	return false;
        }
        
        if (handlerMessageContext.get(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY).equals(true)
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
            EndpointDescription endpointDesc, HandlerChainProcessor.MEP mep, boolean isOneWay) {

        //ArrayList<String> handlers = endpointDesc.getHandlerList();
        
        // TODO you may need to hard-code add some handlers until we
        // actually have useful code under EndpointDescription.getHandlerList()
        
        HandlerResolverImpl hResolver = new HandlerResolverImpl(endpointDesc);
        ArrayList<Handler> handlers = hResolver.getHandlerChain(endpointDesc.getPortInfo());
        
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
        //ArrayList<Handler> handlerInstances = createHandlerInstances(endpointDesc);

        HandlerChainProcessor processor = new HandlerChainProcessor(
                handlers);
        // if not one-way, expect a response
        try {
        	if (msgCtx.getMessage().isFault()) {
        		processor.processFault(handlerMessageContext,
        				HandlerChainProcessor.Direction.OUT);
        	} else {
        		handlerMessageContext = processor.processChain(handlerMessageContext,
        				HandlerChainProcessor.Direction.OUT,
        				mep, !isOneWay);
        	}
        } catch (RuntimeException re) {
        	/*
        	 * handler framework should only throw an exception here if
        	 * we are in the server outbound case.  Make sure the message
        	 * context and message are transformed.
        	 */
        	HandlerChainProcessor.convertToFaultMessage(handlerMessageContext, re);
        	addConvertedFaultMsgToCtx(msgCtx, handlerMessageContext);
        	return false;
        }

        if (handlerMessageContext.get(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY).equals(false)
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
    

    private static void addConvertedFaultMsgToCtx(MessageContext msgCtx, javax.xml.ws.handler.MessageContext handlerMsgCtx) {
    	try {
    		Message msg = ((MessageFactory)FactoryRegistry.getFactory(MessageFactory.class)).createFrom(((SOAPMessageContext)handlerMsgCtx).getMessage());
    		msgCtx.setMessage(msg);
    	} catch (XMLStreamException e) {
    		// TODO log it
    		throw ExceptionFactory.makeWebServiceException(e);
    	}
    }
    
    // TODO method is for TEST only.  instances will be created elsewhere
    /*
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
    */
    
}
