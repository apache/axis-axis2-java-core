
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
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Validator;

public class JWTAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    
    @Autowired
    private WSSecUtils wssecutils;

    public JWTAuthenticationFilter() {
        super("/**");
    }

    @Override
    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        return true;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String logPrefix = "JWTAuthenticationFilter.attemptAuthentication() , ";

        // if this fails it will throw a fatal error, don't catch it since it could be evil data
        String authToken = getBearerToken(request);

        JWTAuthenticationToken authRequest = new JWTAuthenticationToken(authToken);

        return getAuthenticationManager().authenticate(authRequest);
    }

    public String getBearerToken(HttpServletRequest request) throws AuthenticationException {
        String logPrefix = "JWTAuthenticationFilter.getBearerToken() , ";
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new JWTTokenMissingException("No JWT token found in request headers");
        }
        Validator validator = ESAPI.validator();
        boolean headerstatus = validator.isValidInput("userInput", header, "HTTPHeaderValue", 1000 , false);
        if (!headerstatus) {
            logger.error(logPrefix + "returning with failure status on invalid header: " + header);
            throw new JWTTokenMissingException("invalid header");
        }

        String authToken = header.substring(7);

        return authToken;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
            
        String logPrefix = "JWTAuthenticationFilter.successfulAuthentication() , ";

        String authToken = getBearerToken(request);
        JWTUserDTO parsedUser = null; 
        try{
            parsedUser = wssecutils.getParsedUser(authToken);
        } catch (Exception ex) {
            logger.error(logPrefix +  ex.getMessage(), ex );
        }

        if (parsedUser == null) {
            throw new ServletException("JWT token is not valid, cannot find user");
        }
        String uuid = parsedUser.getUuid();
        if (uuid == null) {
            throw new ServletException("JWT token is not valid, cannot find uuid");
        }
        logger.warn(logPrefix + "found uuid from token: " + uuid);
        String usernameFromToken = parsedUser.getUsername();
        if (usernameFromToken == null) {
            throw new ServletException("JWT token is not valid, cannot find username");
        }
        usernameFromToken = usernameFromToken.trim();

        String currentUserIPAddress = null;

	String newuuid = UUID.randomUUID().toString();

        // As this authentication is in the HTTP header, after success we need to continue 
        // the request normally and return the response as if the resource was not secured at all

        // set some vars that may be helpful to the webservices
        request.setAttribute("email", usernameFromToken);
        request.setAttribute("uuid", newuuid);
        request.setAttribute("currentUserIPAddress", currentUserIPAddress);

        SecurityContextHolder.getContext().setAuthentication(authResult);
   
        chain.doFilter(request, response);
    }
 
}
