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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    private static boolean doesPatternMatch(QName portInfoQName, QName pattern) {
        if (pattern == null)
            return true;
        validatePattern(pattern);
        // build up the strings according to the regular expression defined at http://java.sun.com/xml/ns/javaee/javaee_web_services_1_2.xsd
        // use the prefix, not the literal namespace
        String portInfoPrefix = portInfoQName.getNamespaceURI(); //Prefix();
        String portInfoLocalPart = portInfoQName.getLocalPart();
        String portInfoString = ((portInfoPrefix == null) || (portInfoPrefix.equals(""))) ? "" : portInfoPrefix + ":";
        portInfoString += portInfoLocalPart;
        
        String patternStringPrefix = pattern.getNamespaceURI(); //Prefix();
        String patternInfoLocalPart = pattern.getLocalPart();
        String patternString = ((patternStringPrefix == null) || (patternStringPrefix.equals(""))) ? "" : patternStringPrefix + ":";
        patternString += patternInfoLocalPart;
        
        // now match the portInfoQName to the user pattern
        // But first, convert the user pattern to a regular expression.  Remember, the only non-QName character allowed is "*", which
        // is a wildcard, with obvious restrictions on what characters can match (for example, a .java filename cannot contain perentheses).
        // We'll just use part of the above reg ex to form the appropriate restrictions on the user-specified "*" character:
        Pattern userp = Pattern.compile(patternString.replace("*", "(\\w|\\.|-|_)*"));
        Matcher userm = userp.matcher(portInfoString);
        boolean match = userm.matches();
        if (log.isDebugEnabled()) {
            if (!match) {
                log.debug("Pattern match failed: \"" + portInfoString + "\" does not match \"" + patternString + "\"");
            } else {
                log.debug("Pattern match succeeded: \"" + portInfoString + "\" matches \"" + patternString + "\"");
            }
        }
        return match;
        
    }
    
    
    private static void validatePattern(QName pattern) {
        String patternStringPrefix = pattern.getPrefix();
        String patternInfoLocalPart = pattern.getLocalPart();
        String patternString = ((patternStringPrefix == null) || (patternStringPrefix.equals(""))) ? "" : patternStringPrefix + ":";
        patternString += patternInfoLocalPart;

        /*
         * Below pattern is ported from:  http://java.sun.com/xml/ns/javaee/javaee_web_services_1_2.xsd
         * Schema regular expressions are defined differently from Java regular expressions which are different from Perl regular
         * expressions.  I've converted the pattern defined in the above linked schema to its Java equivalent, as best as I can.
         * 
         * Schema reg ex:  "\*|([\i-[:]][\c-[:]]*:)?[\i-[:]][\c-[:]]*\*?"
         * Java reg ex:  "\\*|((\\w|_)(\\w|\\.|-|_)*:)?(\\w|_)(\\w|\\.|-|_)*\\*?"
         */

        // first, confirm the defined pattern is legal
        Pattern p = Pattern.compile("\\*|((\\w|_)(\\w|\\.|-|_)*:)?(\\w|_)(\\w|\\.|-|_)*\\*?");
        Matcher m = p.matcher(patternString);
        if (!m.matches()) {
            // pattern defined by user in handler chain xml file is illegal -- report it but continue
            log.warn("Pattern defined by user is illegal:  \"" + patternString + "\" does not match regular expression in schema http://java.sun.com/xml/ns/javaee/javaee_web_services_1_2.xsd.  Pattern matching should now be considered \"best-effort.\"");
        }
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
