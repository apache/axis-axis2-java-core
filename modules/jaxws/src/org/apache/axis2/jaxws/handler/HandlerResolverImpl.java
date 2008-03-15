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
        List<Class> handlerClasses = null;
        // Look into the cache only if the service delegate key is null.
        if (serviceDelegateKey == null) {
            handlerClasses = serviceDesc.getHandlerChainClasses(portinfo);
        }
        if (handlerClasses == null) {
            // resolve handlers if we did not find them in the cache
            handlerClasses = resolveHandlers(portinfo);
            // Store the list of classes
            if (serviceDelegateKey == null) {
                serviceDesc.setHandlerChainClasses(portinfo, handlerClasses);
            }
        }
        if (handlerClasses.size() == 0) {
            return new ArrayList<Handler>();
        }

        ArrayList<Handler> handlers = new ArrayList<Handler>();
        // Create temporary MessageContext to pass information to HandlerLifecycleManager
        MessageContext ctx = new MessageContext();
        ctx.setEndpointDescription(serviceDesc.getEndpointDescription(portinfo.getPortName()));

        HandlerLifecycleManager hlm = createHandlerlifecycleManager();

        for (Iterator<Class> iterator = handlerClasses.iterator(); iterator.hasNext();) {
            Class aClass = iterator.next();
            //  instantiate portHandler class 
            try {
                handlers.add(hlm.createHandlerInstance(ctx, aClass));
            } catch (Exception e) {
                // TODO: should we just ignore this problem?
                // TODO: NLS log and throw
                throw ExceptionFactory.makeWebServiceException(e);
            }
            //TODO NLS
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled())
                log.debug("Successfully instantiated the class: " + aClass);

        }
        return handlers;
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
    private ArrayList<Class> resolveHandlers(PortInfo portinfo) throws WebServiceException {
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

        ArrayList<Class> handlers = new ArrayList<Class>();

        /*
         * TODO: do a better job checking that the return value matches up
         * with the PortInfo object before we add it to the chain.
         */

        // The HandlerChain annotation can be specified on:
        // - a service implementation (per JSR-181) which is on the service-provider side
        // - an SEI (per JSR-181), which can be on both the service-provider and 
        //   service-requester sides
        // - a generated Service (per JSR-224), which is on the service-requester side
        //
        // The order of precedence here is a bit counter intuitive if the HandlerChain annotation
        // is present on more than one class.  
        // - For the service-provider, JSR-181 [p. 25, Section 4.6.1]
        //   states that the service implementation's HandlerChain takes is used if it is present
        //   on both the implementation and the SEI.
        // - Following that same pattern, we conclude that a generated service HandlerChain should
        //   take precedence if the annotation is on both the Service and the SEI.
        //
        // The reasoning for this is (probably) that the SEI can be used by multiple endpoints 
        // and / or multiple Service requesters, so the endpoint implementation and the Service
        // should have the final say in what handlers are run, rather than the SEI.
        //
        // Adding Deployment Descriptors complicates this further.  A DD should have the absolute 
        // final say (such as a JSR-109 client DD).  Given that, on a service-requester if the
        // Service has a HandlerChain and the SEI has a HandlerChain and the DD specifies a 
        // HandlerChain for a port, then the DD should win.  Since DDs are implented as information
        // in a sparse composite, then that means the sparse composite wins.
        
        // Get the HandlerChains specified on the Endpoint (service-provider) or on the Service
        // (service-requester).
        handlerChainsType = serviceDesc.getHandlerChain(serviceDelegateKey);  

        // HandlerChains apply to specific Port Compoments (service-provider) or Ports (
        // (service-requesters) so find the appropriate one.
        EndpointDescription ed = null;
        if(portinfo !=null){
             ed = serviceDesc.getEndpointDescription(portinfo.getPortName());
        }
        
        // Get the HandlerChain information, if any, off the SEI (service-provider or 
        // service-requster) and check for any DD overrides.
        if (ed != null) {
            // If there was no handler chains information specifed on the endpoint (service-
            // provider) or the Service (service-requester)
            // -- OR -- 
            // If the handler chains associated with a particular instance of a service delegate
            // DOES NOT match the handler chains across all service delegates, then there was
            // sparse composite information specified for this service delegate.  Sparse composite
            // information is how Deployment Descriptor information is specified, and that 
            // overrides the annotations as described in the long-winded comment above.
            // -- THEN --
            // Use this handler chains information
            HandlerChainsType hct_includingComposite = ed.getHandlerChain(serviceDelegateKey);
            HandlerChainsType hct_noComposite = ed.getHandlerChain();
            if (handlerChainsType == null || (hct_includingComposite != hct_noComposite)) {
                handlerChainsType = hct_includingComposite;
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
                HandlerType handlerType = (HandlerType) ht.next();
                // TODO must do better job comparing the handlerType with the PortInfo param
                // to see if the current iterator handler is intended for this service.
                // TODO review: need to check for null getHandlerClass() return?
                // or will schema not allow it?
                String portHandler = handlerType.getHandlerClass().getValue();
                Class aClass;
                try {
                    aClass = loadClass(portHandler);
                } catch (Exception e) {
                    // TODO: should we just ignore this problem?
                    // TODO: NLS log and throw
                    throw ExceptionFactory.makeWebServiceException(e);
                }

                // 9.2.1.2 sort them by Logical, then SOAP
                if (LogicalHandler.class.isAssignableFrom(aClass))
                    handlers.add(aClass);
                else if (SOAPHandler.class.isAssignableFrom(aClass))
                    // instanceof ProtocolHandler
                    handlers.add(aClass);
                else if (Handler.class.isAssignableFrom(aClass)) {
                    // TODO: NLS better error message
                    throw ExceptionFactory.makeWebServiceException(Messages
                            .getMessage("handlerChainErr1", aClass.getName()));
                } else {
                    // TODO: NLS better error message
                    throw ExceptionFactory.makeWebServiceException(Messages
                            .getMessage("handlerChainErr2", aClass.getName()));
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
