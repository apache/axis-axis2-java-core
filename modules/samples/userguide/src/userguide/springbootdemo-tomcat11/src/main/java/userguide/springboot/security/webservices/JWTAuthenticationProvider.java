
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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import userguide.springboot.requestactivity.Axis2UserDetails;

@Component
public class JWTAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private static final Logger log = LogManager.getLogger(JWTAuthenticationProvider.class);
    
    @Autowired
    private WSSecUtils wssecutils;

    @Override
    public boolean supports(Class<?> authentication) {
        return (JWTAuthenticationToken.class.isAssignableFrom(authentication));
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        String logPrefix = "JWTAuthenticationProvider.retrieveUser() , username: " +username+ " , ";
        JWTAuthenticationToken jwtAuthenticationToken = (JWTAuthenticationToken) authentication;
        String token = jwtAuthenticationToken.getToken();

        try {
            JWTUserDTO parsedUser = wssecutils.getParsedUser(token);
    
            if (parsedUser == null) {
                throw new JWTTokenMalformedException("JWT token is not valid, cannot find user");
            }
            logger.warn(logPrefix + "found parsedUser: " + parsedUser.toString());
            String uuid = parsedUser.getUuid();
            if (uuid == null) {
                throw new JWTTokenMalformedException("JWT token is not valid, cannot find uuid");
            }
            if (parsedUser.getUsername() == null) {
                throw new JWTTokenMalformedException("JWT token is not valid, cannot find email");
            }
            logger.warn(logPrefix + "found uuid from token: " + uuid);

            LoginDTO persistedUser = null;
            try {
                persistedUser = wssecutils.findUserByEmail(parsedUser.getUsername());
            } catch (Exception ex) {
                logger.error(logPrefix + "cannot create LoginDTO from email: " + parsedUser.getUsername() + " , " + ex.getMessage(), ex);
                throw new JWTTokenMalformedException("JWT token is not valid, cannot create LoginDTO from email: " + parsedUser.getUsername());
            }
            if (persistedUser == null) {
                logger.error(logPrefix + "returning with failure status on failed creation of LoginDTO from email: " + parsedUser.getUsername());
                throw new JWTTokenMalformedException("JWT token is not valid, LoginDTO is null from email: " + parsedUser.getUsername());
            }
            List<GrantedAuthority> authorityList = AuthorityUtils.commaSeparatedStringToAuthorityList(parsedUser.getRole());

            Boolean isNonLocked;

            if (persistedUser.getAccountNonLocked()) {
                isNonLocked = true;
            } else {
                isNonLocked = false;
            }

            Axis2UserDetails userDetails = new Axis2UserDetails(persistedUser, parsedUser.getUsername(), token, persistedUser.getEnabled(), persistedUser.getAccountNonExpired(), persistedUser.getCredentialsNonExpired(), isNonLocked, authorityList);

            return userDetails;
        
        } catch (Exception ex) {
            logger.error(logPrefix + "failed: " + ex.getMessage(), ex);
            throw new JWTTokenMalformedException("unexpected error parsing token");
        }
            
    }

}
