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
    
    private static boolean doesPatternMatch(QName portInfoQName, QName pattern) {
        if (pattern == null)
            return true;
        String portInfoString = portInfoQName.toString();
        String patternString = pattern.toString();
        if (patternString.equals("*"))
            return true;
        if (patternString.contains("*")) {
            return match(patternString, portInfoString, false);
        }
        return portInfoString.equals(patternString);
        
    }
    
    /**
     * Matches a string against a pattern. The pattern contains two special
     * characters:
     * '*' which means zero or more characters,
     *
     * @param pattern the (non-null) pattern to match against
     * @param str     the (non-null) string that must be matched against the
     *                pattern
     * @param isCaseSensitive
     *
     * @return <code>true</code> when the string matches against the pattern,
     *         <code>false</code> otherwise.
     */
    private static boolean match(String pattern, String str,
                                   boolean isCaseSensitive) {

        char[] patArr = pattern.toCharArray();
        char[] strArr = str.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;
        char ch;
        boolean containsStar = false;

        for (int i = 0; i < patArr.length; i++) {
            if (patArr[i] == '*') {
                containsStar = true;
                break;
            }
        }
        if (!containsStar) {

            // No '*'s, so we make a shortcut
            if (patIdxEnd != strIdxEnd) {
                return false;        // Pattern and string do not have the same size
            }
            for (int i = 0; i <= patIdxEnd; i++) {
                ch = patArr[i];
                if (isCaseSensitive && (ch != strArr[i])) {
                    return false;    // Character mismatch
                }
                if (!isCaseSensitive
                        && (Character.toUpperCase(ch)
                        != Character.toUpperCase(strArr[i]))) {
                    return false;    // Character mismatch
                }
            }
            return true;             // String matches against pattern
        }
        if (patIdxEnd == 0) {
            return true;    // Pattern contains only '*', which matches anything
        }

        // Process characters before first star
        while ((ch = patArr[patIdxStart]) != '*'
                && (strIdxStart <= strIdxEnd)) {
            if (isCaseSensitive && (ch != strArr[strIdxStart])) {
                return false;    // Character mismatch
            }
            if (!isCaseSensitive
                    && (Character.toUpperCase(ch)
                    != Character.toUpperCase(strArr[strIdxStart]))) {
                return false;    // Character mismatch
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {

            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // Process characters after last star
        while ((ch = patArr[patIdxEnd]) != '*' && (strIdxStart <= strIdxEnd)) {
            if (isCaseSensitive && (ch != strArr[strIdxEnd])) {
                return false;    // Character mismatch
            }
            if (!isCaseSensitive
                    && (Character.toUpperCase(ch)
                    != Character.toUpperCase(strArr[strIdxEnd]))) {
                return false;    // Character mismatch
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {

            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // process pattern between stars. padIdxStart and patIdxEnd point
        // always to a '*'.
        while ((patIdxStart != patIdxEnd) && (strIdxStart <= strIdxEnd)) {
            int patIdxTmp = -1;

            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (patArr[i] == '*') {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart + 1) {

                // Two stars next to each other, skip the first one.
                patIdxStart++;
                continue;
            }

            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;

            strLoop:
            for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    ch = patArr[patIdxStart + j + 1];
                    if (isCaseSensitive
                            && (ch != strArr[strIdxStart + i + j])) {
                        continue strLoop;
                    }
                    if (!isCaseSensitive && (Character
                            .toUpperCase(ch) != Character
                            .toUpperCase(strArr[strIdxStart + i + j]))) {
                        continue strLoop;
                    }
                }
                foundIdx = strIdxStart + i;
                break;
            }
            if (foundIdx == -1) {
                return false;
            }
            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        // All characters in the string are used. Check if only '*'s are left
        // in the pattern. If so, we succeeded. Otherwise failure.
        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (patArr[i] != '*') {
                return false;
            }
        }
        return true;
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
