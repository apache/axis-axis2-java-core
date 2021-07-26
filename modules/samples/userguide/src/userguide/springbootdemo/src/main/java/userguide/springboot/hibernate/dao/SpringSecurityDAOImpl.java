
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
package userguide.springboot.hibernate.dao;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;

import userguide.springboot.requestactivity.Axis2UserDetails;
import userguide.springboot.security.webservices.WSSecUtils;
import userguide.springboot.security.webservices.LoginDTO;

@Service
public class SpringSecurityDAOImpl implements UserDetailsService {

    private static final Logger logger = LogManager.getLogger(SpringSecurityDAOImpl.class);
    
    @Autowired
    private WSSecUtils wssecutils;

    /** Everyone needs this role. **/
    public static final String ROLE_USER = "ROLE_USER";

    /**
     * Spring Security invokes this method to get the credentials from the DB.
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {

        String logPrefix = "SpringSecurityDAOImpl.loadUserByUsername() , ";
        Axis2UserDetails userDetails = null;
        
        logger.debug("user attempting Spring Security login: " + username);
        if (username == null || username.equals("")) {
            throw new BadCredentialsException("user login FAILED: username empty or null.");
        }    
        LoginDTO persistedUser = null;
        try {
            persistedUser = wssecutils.findUserByEmail(username);
        } catch (Exception ex) {
            logger.error(logPrefix + "cannot create LoginDTO from email: " + username + " , " + ex.getMessage(), ex);
        }
        if (persistedUser == null) {
            throw new BadCredentialsException("Can't find username: " + username);
        }    

        Set<String> roles = new HashSet<String>();
        // adding permissions - put Roles here
        // Every user must have the ROLE_USER to navigate the application:
        if (!roles.contains(ROLE_USER)) {
            roles.add(ROLE_USER);
        }
        Iterator<String> it = roles.iterator();

        Collection<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();

        int xx = 0;
        while (it.hasNext()) {
            String role = it.next();
            GrantedAuthority authority = new SimpleGrantedAuthority(role);
            authorities.add(authority);
            if (logger.isDebugEnabled()) {
                logger.debug("user: " + username + ", "
                             + "authorities : " + (xx - 1) + ", value:"
                             + authority.toString());
            }
        }

        // Give these fields to Spring Security so it can compare with credentials passed in via the login page
        userDetails = new Axis2UserDetails(persistedUser,
                // username == email 
                persistedUser.getEmail().toLowerCase(),
                persistedUser.getPassword(),
                persistedUser.getEnabled(),
                persistedUser.getAccountNonExpired(),
                persistedUser.getCredentialsNonExpired(),
                persistedUser.getAccountNonLocked(),
                authorities);
            
    
        return userDetails;

    }
    
}
