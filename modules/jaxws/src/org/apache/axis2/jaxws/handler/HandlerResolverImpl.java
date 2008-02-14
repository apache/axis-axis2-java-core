/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.handler;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainType;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;
import org.apache.axis2.jaxws.description.xml.handler.HandlerType;
import org.apache.axis2.jaxws.handler.lifecycle.factory.HandlerLifecycleManager;
import org.apache.axis2.jaxws.handler.lifecycle.factory.HandlerLifecycleManagerFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.spi.handler.BaseHandlerResolver;
import org.apache.axis2.util.LoggingControl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.handler.soap.SOAPHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* 
 * This class should be created by the ServiceDelegate.
 * HandlerResolverImpl.getHandlerChain(PortInfo) will be called by the
 * InvocationContext, and the return value will be set on the Binding
 * under the BindingProvider.
 * 
 * HandlerResolverImpl.getHandlerChain(PortInfo) will be responsible for
 * starting each Handler's lifecycle according to JAX-WS spec 9.3.1
 */

public class HandlerResolverImpl extends BaseHandlerResolver {

    private static Log log = LogFactory.getLog(HandlerResolverImpl.class);
    /*
      * TODO:  is there any value/reason in caching the list we collect from the
      * ports?  It is a "live" list in the sense that we could possibly return
      * a List or ArrayList object to a service or client application, where
      * they could manipulate it.
      */

    private ServiceDescription serviceDesc;
    private Object serviceDelegateKey;
    
    public HandlerResolverImpl(ServiceDescription sd) {
        this(sd, null);
    }

    public HandlerResolverImpl(ServiceDescription sd, Object serviceDelegateKey) { 
        this.serviceDesc = sd;
        this.serviceDelegateKey = serviceDelegateKey;
    }

    public List<Handler> getHandlerChain(PortInfo portinfo) {
        // TODO:  would check and/or build cache here if implemented later
        return resolveHandlers(portinfo);
    }

    /*
      * The list of handlers (rather, list of class names) is already
      * available per port.  Ports are stored under the ServiceDelegate
      * as PortData objects.
      *
	 * The resolveHandlers method is responsible for instantiating each Handler,
	 * running the annotated PostConstruct method, resolving the list,
	 * and returning it.  We do not sort here.
      */
    private ArrayList<Handler> resolveHandlers(PortInfo portinfo) throws WebServiceException {
        /*

            A sample XML file for the handler-chains:
            
            <jws:handler-chains xmlns:jws="http://java.sun.com/xml/ns/javaee">
                <jws:handler-chain>
                    <jws:protocol-bindings>##XML_HTTP</jws:protocol-bindings>
                    <jws:handler>
                        <jws:handler-name>MyHandler</jws:handler-name>
                        <jws:handler-class>org.apache.axis2.jaxws.MyHandler</jws:handler-class>
                    </jws:handler>
                </jws:handler-chain>
                <jws:handler-chain>
                    <jws:port-name-pattern>jws:Foo*</jws:port-name-pattern>
                    <jws:handler>
                        <jws:handler-name>MyHandler</jws:handler-name>
                        <jws:handler-class>org.apache.axis2.jaxws.MyHandler</jws:handler-class>
                    </jws:handler>
                </jws:handler-chain>
                <jws:handler-chain>
                    <jws:service-name-pattern>jws:Bar</jws:service-name-pattern>
                    <jws:handler>
                        <jws:handler-name>MyHandler</jws:handler-name>
                        <jws:handler-class>org.apache.axis2.jaxws.MyHandler</jws:handler-class>
                    </jws:handler>
                </jws:handler-chain>
            </jws:handler-chains>
            
            Couple of things I'm not sure about...
            1)  if the protocol-binding, port-name-pattern, and service-name-pattern all
                match the PortInfo object, does MyHandler get added three times?  Probably would get added 3 times.
            2)  I assume the asterisk "*" is a wildcard.  Can the asterisk only occur on the local part of the qname?
            3)  Can there be more than one service-name-pattern or port-name-pattern, just like for protocol-bindings?
            4)  How many protocol-bindings are there?  ##XML_HTTP ##SOAP11_HTTP ##SOAP12_HTTP ##SOAP11_HTTP_MTOM ##SOAP12_HTTP_MTOM
                They are separated by spaces
         */

        // our implementation already has a reference to the EndpointDescription,
        // which is where one might get the portinfo object.  We still have the 
        // passed-in variable, however, due to the spec

        ArrayList<Handler> handlers = new ArrayList<Handler>();

        /*
         * TODO: do a better job checking that the return value matches up
         * with the PortInfo object before we add it to the chain.
         */
        
        handlerChainsType = serviceDesc.getHandlerChain(serviceDelegateKey);  
        // if there's a handlerChain on the serviceDesc, it means the WSDL defined an import for a HandlerChain.
        // the spec indicates that if a handlerchain also appears on the SEI on the client.
        EndpointDescription ed = null;
        if(portinfo !=null){
             ed = serviceDesc.getEndpointDescription(portinfo.getPortName());
        }
        
        if (ed != null) {
            HandlerChainsType handlerCT_fromEndpointDesc = ed.getHandlerChain();
            if (handlerChainsType == null) {
                handlerChainsType = handlerCT_fromEndpointDesc;
            } 
        } else {
            // There is no EndpointDescription that matches the portInfo specified so 
            // return the empty list of handlers since there are no ports that match
            if (log.isDebugEnabled()) {
                log.debug("The PortInfo object did not match any ports; returning an empty list of handlers." 
                        + "  PortInfo QName: " + portinfo.getPortName());
            }
            return handlers;
        }

        Iterator it = handlerChainsType == null ? null : handlerChainsType.getHandlerChain().iterator();

        while ((it != null) && (it.hasNext())) {
            HandlerChainType handlerChainType = ((HandlerChainType)it.next());
            
            // if !match, continue (to next chain)
            if (!(chainResolvesToPort(handlerChainType, portinfo)))
                continue;
            
            List<HandlerType> handlerTypeList = handlerChainType.getHandler();
            Iterator ht = handlerTypeList.iterator();
            while (ht.hasNext()) {
                
                HandlerType handlerType = (HandlerType)ht.next();
                
                // TODO must do better job comparing the handlerType with the PortInfo param
                // to see if the current iterator handler is intended for this service.

                // TODO review: need to check for null getHandlerClass() return?
                // or will schema not allow it?
                String portHandler = handlerType.getHandlerClass().getValue();
                Handler handler;
                // Create temporary MessageContext to pass information to HandlerLifecycleManager
                MessageContext ctx = new MessageContext();
                ctx.setEndpointDescription(ed);
                
                HandlerLifecycleManager hlm = createHandlerlifecycleManager();
                    
                //  instantiate portHandler class 
                try {
                    handler = hlm.createHandlerInstance(ctx, loadClass(portHandler));
                } catch (Exception e) {
                    // TODO: should we just ignore this problem?
                    // TODO: NLS log and throw
                    throw ExceptionFactory.makeWebServiceException(e);
                }
                
                //TODO NLS
                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled())
                    log.debug("Successfully instantiated the class: " + handler.getClass());
                
                // 9.2.1.2 sort them by Logical, then SOAP
                if (LogicalHandler.class.isAssignableFrom(handler.getClass()))
                    handlers.add((LogicalHandler) handler);
                else if (SOAPHandler.class.isAssignableFrom(handler.getClass()))
                    // instanceof ProtocolHandler
                    handlers.add((SOAPHandler) handler);
                else if (Handler.class.isAssignableFrom(handler.getClass())) {
                    // TODO: NLS better error message
                    throw ExceptionFactory.makeWebServiceException(Messages
                            .getMessage("handlerChainErr1", handler
                                    .getClass().getName()));
                } else {
                    // TODO: NLS better error message
                    throw ExceptionFactory.makeWebServiceException(Messages
                            .getMessage("handlerChainErr2", handler
                                    .getClass().getName()));
                }
            }
        }

        return handlers;
    }

    private HandlerLifecycleManager createHandlerlifecycleManager() {
        HandlerLifecycleManagerFactory elmf = (HandlerLifecycleManagerFactory)FactoryRegistry
                .getFactory(HandlerLifecycleManagerFactory.class);
        return elmf.createHandlerLifecycleManager();
    }
}
