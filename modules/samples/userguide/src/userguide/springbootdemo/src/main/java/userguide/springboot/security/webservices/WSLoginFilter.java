
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

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.GenericFilterBean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@PropertySource("classpath:application.properties")
public class WSLoginFilter extends GenericFilterBean  {
    
    @Value("${requestDebuggingActivated}")
    private int requestDebuggingActivated;

    @Override
    public void doFilter(
        ServletRequest request, 
        ServletResponse response,
        FilterChain chain) throws IOException, ServletException {
              
        final String logPrefix = "WSLoginFilter.doFilter() , requestDebuggingActivated: " + requestDebuggingActivated + " , ";

        logger.debug(logPrefix + "starting ... ");

        HttpServletRequest requestToUse = (HttpServletRequest) request;
        HttpServletResponse responseToUse = (HttpServletResponse) response;

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
            logger.warn(logPrefix + "IP validation and rate limiting can go here, on currentUserIPAddress: " + currentUserIPAddress);
        }

        if (requestDebuggingActivated == 1) {
	    boolean foundElements = false;
            for (Enumeration en = requestToUse.getParameterNames(); en
                    .hasMoreElements();) {
    
                foundElements = true;

                Object obj = en.nextElement();
                String value = request.getParameterValues((String) obj)[0];
                logger.warn(logPrefix + "on requestDebuggingActivated=1 found String: " +value);
                    
            }
            if (!foundElements) {
                logger.warn(logPrefix + "on requestDebuggingActivated=1 , no HTTP elements found!");
            }
        }

        chain.doFilter(request, response);
    }

}
