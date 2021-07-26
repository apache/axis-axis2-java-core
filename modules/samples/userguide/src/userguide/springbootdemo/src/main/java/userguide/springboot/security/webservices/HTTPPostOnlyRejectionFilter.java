
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
import org.springframework.security.web.RedirectStrategy;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class HTTPPostOnlyRejectionFilter extends OncePerRequestFilter {
 
    private static Log logger = LogFactory.getLog(HTTPPostOnlyRejectionFilter.class);
    
    private final RedirectStrategy redirectStrategy = new NoRedirectStrategy();

    public HTTPPostOnlyRejectionFilter() {
        super();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String uuid = UUID.randomUUID().toString();
        String logPrefix = "HTTPPostOnlyRejectionFilter.doFilterInternal , uuid: " + uuid + " , ";
        
        logger.trace(logPrefix + "starting ... ");
        
        if (!request.getMethod().equals("POST")) {

	    String ip = "unknown";
            if (request.getHeader("X-Forwarded-For") != null) { 
	        ip = request.getHeader("X-Forwarded-For");
            }
            logger.trace(logPrefix +
                "this is not a POST request, ignoring with an HTTP 200 response, " +
                " , on IP from X-Forwarded-For: " + request.getRequestURI() + 
                " , request.getRequestURI() : " + request.getRequestURI() + 
		" , request.getMethod() : " + request.getMethod());

            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print("HTTP requests that are not POST are ignored");
            response.getWriter().flush();
            this.redirectStrategy.sendRedirect((HttpServletRequest) request, (HttpServletResponse) response, "/");

        } else {
            filterChain.doFilter(request, response);
        }
    }

    protected class NoRedirectStrategy implements RedirectStrategy {

        @Override
        public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url)
                throws IOException {
            // do nothing
        }

    }
}
