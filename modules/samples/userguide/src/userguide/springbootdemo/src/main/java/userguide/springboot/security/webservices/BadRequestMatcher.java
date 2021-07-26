
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
package userguide.springboot.security.webservices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class BadRequestMatcher implements RequestMatcher {

    /** commons logging declaration. **/
    private static Log logger = LogFactory.getLog(BadRequestMatcher.class);
          
    private Set<String> encodedUrlBlacklist = new HashSet<String>();

    private Set<String> decodedUrlBlacklist = new HashSet<String>();
    
    private static final String ENCODED_PERCENT = "%25";

    private static final String PERCENT = "%";

    private List<String> FORBIDDEN_ENCODED_PERIOD = Collections.unmodifiableList(Arrays.asList("%2e", "%2E"));

    private List<String> FORBIDDEN_SEMICOLON = Collections.unmodifiableList(Arrays.asList(";", "%3b", "%3B"));

    private List<String> FORBIDDEN_FORWARDSLASH = Collections.unmodifiableList(Arrays.asList("%2f", "%2F"));

    private List<String> FORBIDDEN_BACKSLASH = Collections.unmodifiableList(Arrays.asList("\\", "%5c", "%5C"));

    private int requestDebuggingActivated;

    public BadRequestMatcher(int requestDebuggingActivated) {
        
	this.requestDebuggingActivated = requestDebuggingActivated;
        // this is a 'defense in depth' strategy as Cloudflare or another load balancer should reject this stuff
        urlBlacklistsAddAll(FORBIDDEN_SEMICOLON);
        urlBlacklistsAddAll(FORBIDDEN_FORWARDSLASH);
        urlBlacklistsAddAll(FORBIDDEN_BACKSLASH);

        this.encodedUrlBlacklist.add(ENCODED_PERCENT);
        this.encodedUrlBlacklist.addAll(FORBIDDEN_ENCODED_PERIOD);
        this.decodedUrlBlacklist.add(PERCENT);
    }
    
    private void urlBlacklistsAddAll(Collection<String> values) {
        this.encodedUrlBlacklist.addAll(values);
        this.decodedUrlBlacklist.addAll(values);
    }

    public boolean validate(HttpServletRequest request) {
        return matches(request);	    
    }        
    @Override
    public boolean matches(HttpServletRequest request) {
        String logPrefix = "BadRequestMatcher.matches , ";
	boolean foundElements = false;
        for (Enumeration en = request.getParameterNames(); en
                .hasMoreElements();) {

            foundElements = true;

            Object obj = en.nextElement();
            String value = request.getParameterValues((String) obj)[0];
            if (!isNormalized(value)) {
                logger.error(logPrefix + 
		    "found illegal String: " +value+ " , returning false because the request has parameters that are not 'normalized i.e. paths contain dir traversal or illegal chars'");
		return false;
                
            }
            if (!rejectedBlacklistedValues(value)) {
                logger.error(logPrefix + 
		    "found illegal String: " +value+ " , returning false because the request has rejected black list values");
		return false;
                
            }
            if (requestDebuggingActivated == 1) {
                logger.error(logPrefix + 
		    "on requestDebuggingActivated=1 found String: " +value);
                
            }
        }
        if (!foundElements) {
            logger.warn(logPrefix + "on requestDebuggingActivated=1 , no HTTP elements found!");
        }
        rejectedBlacklistedUrls(request);
        if (!isNormalized(request)) {
            logger.error(logPrefix + 
	        "inside BadRequestMatcher.matches, returning false because the request was not 'normalized i.e. paths contain dir traversal or illegal chars'");
            return false;
        }
        String requestUri = request.getRequestURI();
        if (!containsOnlyPrintableAsciiCharacters(requestUri)) {
            logger.error(logPrefix +
                "The requestURI was rejected because it can only contain printable ASCII characters.");
            return false;

        }
        return true;
    }
    
    private boolean containsOnlyPrintableAsciiCharacters(String uri) {
        int length = uri.length();
        for (int i = 0; i < length; i++) {
            char c = uri.charAt(i);
            if (c < '\u0020' || c > '\u007e') {
                return false;
            }
        }

        return true;
    }
    
    private boolean rejectedBlacklistedUrls(HttpServletRequest request) {
        String logPrefix = "BadRequestMatcher.rejectedBlacklistedUrls , ";
        for (String forbidden : this.encodedUrlBlacklist) {
            if (encodedUrlContains(request, forbidden)) {
                logger.error(logPrefix + 
	            "returning false, The request was rejected because the URL contained a potentially malicious String \"" + forbidden + "\"");
		return false;
            }
        }
        for (String forbidden : this.decodedUrlBlacklist) {
            if (decodedUrlContains(request, forbidden)) {
                logger.error(logPrefix + 
                    "The request was rejected because the URL contained a potentially malicious String \"" + forbidden + "\"");
		return false;
            }
        }
        return true;
    }
    
    private boolean rejectedBlacklistedValues(String value) {
        String logPrefix = "BadRequestMatcher.matches , ";
        for (String forbidden : this.encodedUrlBlacklist) {
            if (valueContains(value, forbidden)) {
                logger.error(logPrefix + "found illegal String: " +value+ " , returning false because the request has parameters that are not 'normalized i.e. paths contain dir traversal or illegal chars'");
		return false;
            }
        }    
        return true;
    }
    
    private boolean valueContains(String value, String contains) {
        return value != null && value.contains(contains);
    }

    private boolean encodedUrlContains(HttpServletRequest request, String value) {
        if (valueContains(request.getContextPath(), value)) {
            return true;
        }
        return valueContains(request.getRequestURI(), value);
    }

    private boolean decodedUrlContains(HttpServletRequest request, String value) {
        if (valueContains(request.getServletPath(), value)) {
            return true;
        }
        if (valueContains(request.getPathInfo(), value)) {
            return true;
        }
        return false;
    }
    
    /**
     * This should be done by Spring Security StrictHttpFirewall but isn't working as expected, 
     * turns out its not as detailed as we need. 
     * Instead of sub-classing it to add logging - there is none - and features, just do the important parts here
     *  
     * Checks whether a path is normalized (doesn't contain path traversal
     * sequences like "./", "/../" or "/.")
     *
     * @param path
     *            the path to test
     * @return true if the path doesn't contain any path-traversal character
     *         sequences.
     */
    private boolean isNormalized(HttpServletRequest request) {
        String logPrefix = "BadRequestMatcher.isNormalized , ";
        if (!isNormalized(request.getRequestURI())) {
            logger.error(logPrefix + "returning false on request.getRequestURI() : " + request.getRequestURI());
            return false;
        }
        if (!isNormalized(request.getContextPath())) {
            logger.error(logPrefix + "returning false on request.getContextPath() : " + request.getContextPath());
            return false;
        }
        if (!isNormalized(request.getServletPath())) {
            logger.error(logPrefix + "returning false on request.getServletPath() : " + request.getServletPath());
            return false;
        }
        if (!isNormalized(request.getPathInfo())) {
            logger.error(logPrefix + "returning false on request.getPathInfo() : " + request.getPathInfo());
            return false;
        }
        return true;
    }
    
    private boolean isNormalized(String path) {
        
        String logPrefix = "BadRequestMatcher.isNormalized(String path) , ";
        
        logger.warn(logPrefix + "evaluating path : " + path);
        
        if (path == null) {
            return true;
        }

        if (path.indexOf("//") > -1) {
            return false;
        }

        for (int j = path.length(); j > 0;) {
            int i = path.lastIndexOf('/', j - 1);
            int gap = j - i;

            if (gap == 2 && path.charAt(i + 1) == '.') {
                // ".", "/./" or "/."
                return false;
            } else if (gap == 3 && path.charAt(i + 1) == '.' && path.charAt(i + 2) == '.') {
                return false;
            }

            j = i;
        }

          return true;
      }

}
