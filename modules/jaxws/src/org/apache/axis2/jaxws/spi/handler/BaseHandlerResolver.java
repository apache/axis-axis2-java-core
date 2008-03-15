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
package org.apache.axis2.jaxws.spi.handler;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.impl.DescriptionUtils;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainType;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import java.io.InputStream;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class can be subclassed to produce different implementations of {@link HandlerResolver}
 *
 */
public abstract class BaseHandlerResolver implements HandlerResolver {

    private static Log log = LogFactory.getLog(BaseHandlerResolver.class);

    // TODO should probably use constants defined elsewhere
    protected static final Map<String, String> protocolBindingsMap =
        new HashMap<String, String>(5);

    protected HandlerChainsType handlerChainsType;
    
    protected BaseHandlerResolver() {
    }
    
    protected BaseHandlerResolver(String file) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String className = this.getClass().getName();
        InputStream is =
            DescriptionUtils.openHandlerConfigStream(file, className, classLoader);
        
        if(is == null) {
            log.warn("Unable to load handlers from file: " + file);                    
        } else {
            handlerChainsType = DescriptionUtils.loadHandlerChains(is, classLoader);
        }
    }
    
    protected static boolean chainResolvesToPort(HandlerChainType hct, PortInfo portinfo) {
        
        List<String> protocolBindings = hct.getProtocolBindings();
        if (protocolBindings != null) {
            boolean match = true;
            for (Iterator<String> it = protocolBindings.iterator() ; it.hasNext();) {
                match = false;  // default to false in the protocol bindings until we find a match
                String protocolBinding = it.next();
                protocolBinding = protocolBinding.startsWith("##") ? protocolBindingsMap.get(protocolBinding) : protocolBinding;
                // if the protocolBindingsMap returns null, it would mean someone has some nonsense ##binding
                if ((protocolBinding != null) && (protocolBinding.equals(portinfo.getBindingID()))) {
                    match = true;
                    break;
                }
            }
            if (match == false) {
                // we've checked all the protocolBindings, but didn't find a match, no need to continue
                return match;
            }
        }

        /*
         * need to figure out how to get the namespace declaration out of the port-name-pattern and service-name-pattern
         */
        
        if (!doesPatternMatch(portinfo.getPortName(), hct.getPortNamePattern())) {
                // we've checked the port-name-pattern, and didn't find a match, no need to continue
                return false;
        }
        
        if (!doesPatternMatch(portinfo.getServiceName(), hct.getServiceNamePattern())) {
                // we've checked the service-name-pattern, and didn't find a match, no need to continue
                return false;
        }

        return true;
    }
    
    protected static Class loadClass(String clazz) throws ClassNotFoundException {
        try {
            return forName(clazz, true, getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }
    
    /*
     * A comparison routing to check service-name-pattern and port-name-pattern.  These patterns may be of
     * the form:
     * 
     * 1)  namespace:localpart
     * 2)  namespace:localpart*
     * 3)  namespace:*    (not sure about this one)
     * 4)  *   (which is equivalent to not specifying a pattern, therefore always matching)
     * 
     * I've not seen any examples where the wildcard may be placed mid-string or on the namespace, such as:
     * 
     * namespace:local*part
     * *:localpart
     * 
     */
    private static boolean doesPatternMatch(QName portInfoQName, QName pattern) {
        if (pattern == null)
            return true;
        String portInfoString = portInfoQName.toString();
        String patternString = pattern.toString();
        if (patternString.equals("*"))
            return true;
        if (patternString.contains("*")) {
            patternString = patternString.substring(0, patternString.length() - 1);
            return portInfoString.startsWith(patternString);
        }
        return portInfoString.equals(patternString);
        
    }

    /**
     * Return the class for this name
     *
     * @return Class
     */
    private static Class forName(final String className, final boolean initialize,
                                 final ClassLoader classLoader) throws ClassNotFoundException {
        // NOTE: This method must remain protected because it uses AccessController
        Class cl = null;
        try {
            cl = (Class)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            try{
                                if (log.isDebugEnabled()) {
                                    log.debug("HandlerResolverImpl attempting to load Class: "+className);
                                }
                                return Class.forName(className, initialize, classLoader);
                            } catch (Throwable e) {
                                // TODO Should the exception be swallowed ?
                                if (log.isDebugEnabled()) {
                                    log.debug("HandlerResolverImpl cannot load the following class Throwable Exception Occured: " + className);
                                }
                                throw new ClassNotFoundException("HandlerResolverImpl cannot load the following class Throwable Exception Occured:" + className);
                            }
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw (ClassNotFoundException)e.getException();
        }

        return cl;
    }


    /** @return ClassLoader */
    private static ClassLoader getContextClassLoader() {
        // NOTE: This method must remain private because it uses AccessController
        ClassLoader cl = null;
        try {
            cl = (ClassLoader)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Thread.currentThread().getContextClassLoader();
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }

        return cl;
    }
    
    static {
        protocolBindingsMap.put("##SOAP11_HTTP",        "http://schemas.xmlsoap.org/wsdl/soap/http");
        protocolBindingsMap.put("##SOAP11_HTTP_MTOM",   "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true");
        protocolBindingsMap.put("##SOAP12_HTTP",        "http://www.w3.org/2003/05/soap/bindings/HTTP/");
        protocolBindingsMap.put("##SOAP12_HTTP_MTOM",   "http://www.w3.org/2003/05/soap/bindings/HTTP/?mtom=true");
        protocolBindingsMap.put("##XML_HTTP",           "http://www.w3.org/2004/08/wsdl/http");
    }
}
