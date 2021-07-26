
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
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

@PropertySource("classpath:application.properties")
public class RequestAndResponseValidatorFilter extends OncePerRequestFilter {
 
    private static Log logger = LogFactory.getLog(RequestAndResponseValidatorFilter.class);
    
    private static ThreadLocal<Long> requestBeginTime = new ThreadLocal<>();
    private static final int DEFAULT_MAX_PAYLOAD_LENGTH = 16384;

    private int maxPayloadLength = DEFAULT_MAX_PAYLOAD_LENGTH;

    @Value("${requestDebuggingActivated}")
    private int requestDebuggingActivated;

    public RequestAndResponseValidatorFilter() {
        super();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String uuid = UUID.randomUUID().toString();
        String logPrefix = "RequestAndResponseValidatorFilter.doFilterInternal , uuid: " + uuid + " , request.getRequestURI():" + request.getRequestURI() + " , ";
        
        logger.debug(logPrefix + "starting ... ");
        
        BadRequestMatcher bad = new BadRequestMatcher(requestDebuggingActivated);
        if (!bad.validate(request)) {
            throw new ServletException("request is invalid, it contains a potentially malicious String");
        }
        
        boolean isFirstRequest = !isAsyncDispatch(request);
        HttpServletRequest requestToUse = request;

        if (isFirstRequest && !(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(request, getMaxPayloadLength());
        }

        HttpServletResponse responseToUse = response;
        if (!(response instanceof ContentCachingResponseWrapper)) {
            responseToUse = new ContentCachingResponseWrapper(response);
        }

        requestBeginTime.set(System.currentTimeMillis());

        String currentUserIPAddress = null;
        if (requestToUse.getHeader("X-Forwarded-For") != null) {
            currentUserIPAddress = requestToUse.getHeader("X-Forwarded-For");
        } else {
            logger.warn(logPrefix + "cannot find X-Forwarded-For header, this field is required for proper IP auditing");
            logger.warn(logPrefix + "Because no X-Forwarded-For header was found, setting 'currentUserIPAddress = requestToUse.getRemoteAddr()' which is typically an internal address");
            currentUserIPAddress = requestToUse.getRemoteAddr();
        }

        if (currentUserIPAddress == null || currentUserIPAddress.length() == 0 || "unknown".equalsIgnoreCase(currentUserIPAddress)) {
            logger.warn(logPrefix + "cannot find valid currentUserIPAddress");
        } else {
            logger.warn(logPrefix + "proceeding on currentUserIPAddress: " + currentUserIPAddress);
            // rate limiting and extra validation can go here
        }
        
        try {
            filterChain.doFilter(requestToUse, responseToUse);
        } finally {
            logRequest(createRequestMessage(requestToUse,uuid));
            logRequest(createResponseMessage(responseToUse,uuid));
        }
    }

    protected String createRequestMessage(HttpServletRequest request, String uuid) throws ServletException {
        
        StringBuilder msg = new StringBuilder();
        msg.append("HTTP request with uuid: " + uuid + " , ");
        msg.append(request.getMethod());
        msg.append(" uri=").append(request.getRequestURI());

        String queryString = request.getQueryString();
        if (queryString != null) {
            msg.append('?').append(queryString);
        }

        String user = request.getRemoteUser();
        if (user != null) {
            msg.append(";user=").append(user);
        }

        
        return msg.toString();
    }

    protected String createResponseMessage(HttpServletResponse resp, String uuid) throws ServletException{

        StringBuilder msg = new StringBuilder();
        msg.append("HTTP response with uuid: " + uuid + " , ");

        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(resp, ContentCachingResponseWrapper.class);
        if (responseWrapper != null) {
            byte[] buf = responseWrapper.getContentAsByteArray();
            try {
                responseWrapper.copyBodyToResponse();
            } catch (IOException e) {
                logger.error("Fail to write response body back", e);
            }
            if (buf.length > 0) {
                String payload;
                try {
                    payload = new String(buf, 0, buf.length, responseWrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException ex) {
                    payload = "[unknown]";
                }
                msg.append(";response=").append(payload);
            }
        }
        
        return msg.toString();
    }

    public static boolean validate(String msg) {
        // Input validation is inferior to output sanitation as its impossible to think of 
        // everything. See JsonHtmlXssSerializer for html encoding of the output
        
        if (msg != null && msg.toUpperCase().contains("DOCTYPE")) {
            logger.error("DOCTYPE keyword is disallowed");
            return false;
        }
	
        // reflected XSS
        if (msg != null && msg.toUpperCase().indexOf("SCRIPT") != -1) {
            logger.error("SCRIPT keyword is disallowed");
            return false;
        }
        // reflected XSS without script tag, sneaky ... <img onerror=alert(1)/>
        if (msg != null && msg.toUpperCase().indexOf("ALERT") != -1) {
            logger.error("ALERT keyword is disallowed");
            return false;
        }
                
        return true;
        
    }

    public int getMaxPayloadLength() {
        return maxPayloadLength;
    }

    protected void logRequest(String message) {

        String logPrefix = "RequestAndResponseValidatorFilter.logRequest() , ";
        long begin = requestBeginTime.get();
        long end = System.currentTimeMillis();
 
        long duration = end - begin;
        if (message != null && message.toString().toUpperCase().indexOf("CREDENTIALS") != -1) {
            logger.info(logPrefix + " , not logging credentials ... request time:" + duration);
        } else {
            logger.info(logPrefix + message + ", request time:" + duration);
        }
    }
}
