
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
package userguide.springboot.webservices.secure;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.commons.lang3.RandomStringUtils; 

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import userguide.springboot.security.webservices.WSSecUtils;
import userguide.springboot.security.webservices.LoginDTO;
import userguide.springboot.security.webservices.RequestAndResponseValidatorFilter;
import userguide.springboot.hibernate.dao.SpringSecurityDAOImpl;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.commons.lang.StringEscapeUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginService {

    private static final Logger logger = LogManager.getLogger(LoginService.class);

    @Autowired
    SpringSecurityDAOImpl springSecurityDAOImpl;

    @Autowired
    NoOpPasswordEncoder passwordEncoder;

    @Autowired
    private WSSecUtils wssecutils;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public LoginResponse doLogin(LoginRequest request) {

        Long startTime = System.currentTimeMillis();

        String uuid = UUID.randomUUID().toString();

        String logPrefix = "LoginService.doLogin() , "
                + " , uuid: " + uuid + " , request: " + request.toString() + " , ";

        logger.warn(logPrefix + "starting ... ");
        LoginResponse response = new LoginResponse();

        try {
            if (request == null) {
                logger.error(logPrefix + "returning with failure status on null LoginRequest");
                response.setStatus("FAILED");
                return response;
            }
            if (request.getEmail() == null) {
                logger.error(logPrefix + "returning with failure status on null email");
                response.setStatus("FAILED");
                return response;
            }
            request.email = request.email.trim();

            MessageContext ctx = MessageContext.getCurrentMessageContext();
            HttpServletRequest httpServletRequest = (HttpServletRequest) ctx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
            if (httpServletRequest == null) {
                logger.error(logPrefix + "returning with failure status on null httpServletRequest");
                response.setStatus("FAILED");
                return response;
            }
            String currentUserIPAddress = null;

            if (httpServletRequest.getHeader("X-Forwarded-For") != null) {
                currentUserIPAddress = httpServletRequest.getHeader("X-Forwarded-For");
            } else {
                logger.warn(logPrefix + "cannot find X-Forwarded-For header, this field is required for proper IP auditing");
                currentUserIPAddress = httpServletRequest.getRemoteAddr();
                logger.warn(logPrefix + "found currentUserIPAddress from httpServletRequest.getRemoteAddr() :" + currentUserIPAddress);
            }
            // All data is evil!
            Validator validator = ESAPI.validator();

            String email = request.getEmail().trim();
            boolean emailstatus = validator.isValidInput("userInput", email, "Email", 100 , false);
            if (!emailstatus) {
                logger.error(logPrefix + "returning with failure status on invalid email (in quotes): '" + email +  "'");
                response.setStatus("FAILED");
                return response;
            }

            String creds = "";
            // handle unicode escaped chars that were sent that way for '@' etc
            if (request.getCredentials().contains("u00")) {
                String uu = request.getCredentials();
                String uut = uu.replaceAll("u00", "\\\\u00");
                creds = StringEscapeUtils.unescapeJava(uut); 
            } else {
                creds = request.getCredentials(); 
                if (logger.isTraceEnabled()) { 
                   logger.trace(logPrefix + "found creds: " +creds); 
                }
            }
            // passwords require special char's ... just do some minimal validation
            boolean credentialsstatus = RequestAndResponseValidatorFilter.validate(creds);
            if (!credentialsstatus || creds.length() > 100) {
                logger.error(logPrefix + "returning with failure status, credentials failed validation, credentialsstatus: " + credentialsstatus + " , length: " + creds.length());
                response.setStatus("FAILED");

                return response;
            }

            LoginDTO loginDTO = null;
            try {
                loginDTO = wssecutils.findUserByEmail(email);
            } catch (Exception ex) {
                logger.error(logPrefix + "cannot create LoginDTO from email: " + email + " , " + ex.getMessage(), ex);
                response.setStatus("FAILED");
                return response;
            }

            if (loginDTO == null) {
                logger.error(logPrefix + "returning with failure status on failed creation of LoginDTO from email: " + email);
                response.setStatus("FAILED");
                return response;
            }

            logger.warn(logPrefix + "found loginDTO: " + loginDTO.toString());

            response.setUuid(uuid);
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(email, creds);

            logger.warn(logPrefix
                    + "calling authenticate(authRequest) with username: " + email);

            boolean hasFailedLogin = false;
            String failedStr = "";
            try {
                // will throw an Exception if it fails
                DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
                daoAuthenticationProvider.setUserDetailsService(springSecurityDAOImpl);
                daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
                Authentication authResult = daoAuthenticationProvider.authenticate(authRequest);
                logger.warn(logPrefix + "authenticate(authRequest) completed successfully with username: " + email);
            } catch (Exception ex) {
                if (ex.getMessage() == null) {
                    failedStr = "Authentication Exception failed state is undefined";
                } else {
                    failedStr = ex.getMessage();
                }
                logger.error(logPrefix + "failed: " + failedStr, ex);
                hasFailedLogin = true;
            }

            if (hasFailedLogin) {
                logger.error(logPrefix + "returning with failure status on failed login");
                response.setStatus("LOGIN FAILED");
                return response;
            }


            if (!generateTokenForReturn(httpServletRequest, request, response, currentUserIPAddress, email, uuid)) {
                logger.warn(logPrefix + "generateTokenForReturn() failed, goodbye");
                response.setStatus("TOKEN GENERION FAILED");
            }

            return response;

        } catch (Exception ex) {
            logger.error(logPrefix + "failed: " + ex.getMessage(), ex);
            response.setStatus("FAILED");
            return response;
        }

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean generateTokenForReturn(HttpServletRequest httpServletRequest, LoginRequest request, LoginResponse response, String currentUserIPAddress, String email, String uuid) {

        String logPrefix = "LoginService.generateTokenForReturn() , "
                + " , uuid: " + uuid + " , ";

        try {
            String token = null;
            
            // JWT and JWE are specifications to generate and validate tokens 
            // securely, however they require a public / private key pair using
            // elliptic curve cryptography and that is beyond the scope of this
            // userguide.
            // See below:
            // https://datatracker.ietf.org/doc/html/rfc7516
            // https://datatracker.ietf.org/doc/html/rfc7519

            // this is an example only for demo purposes - do not use this for 
            // production code
            token = RandomStringUtils.randomAlphanumeric(20);

            response.setStatus("OK");
            response.setToken(token);
            
            return true;

        } catch (Exception ex) {
            logger.error(logPrefix + "failed: " + ex.getMessage(), ex);
            response.setStatus("FAILED");
            return false;
        }

    }


}
