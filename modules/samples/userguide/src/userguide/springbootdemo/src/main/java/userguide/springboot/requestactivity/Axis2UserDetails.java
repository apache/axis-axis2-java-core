
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
package userguide.springboot.requestactivity;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class Axis2UserDetails extends User {

    private static final long serialVersionUID = 2041888514077783198L;
    /**  User entity which is Stored by Spring's SecurityContext per every login. */
    private Object userDomain;
    
    /**
     * @return Returns the userDomain.
     */
    public Object getUserDomain() {
        return userDomain;
    }
    
    /** 
     * Override SPRING SECURITY Constructor to inform it about the  User entity.
     * 
     * @param userDomain Authenticated  User entity
     * @param username from DB
     * @param password from DB
     * @param enabled Indicates whether the user is enabled or disabled
     * @param accountNonExpired Indicates whether the user's account has expired
     * @param credentialsNonExpired Indicates whether the user's credentials 
     *   (password) has expired.
     * @param accountNonLocked Indicates whether the user is locked or unlocked.
     * @param authorities the authorities granted to the user
     * @throws IllegalArgumentException Invalid argument was found 
     */
    public Axis2UserDetails(Object userDomain, 
            String username, String password, boolean enabled, 
            boolean accountNonExpired, boolean credentialsNonExpired, 
            boolean accountNonLocked,
            Collection<? extends GrantedAuthority> authorities)
        throws IllegalArgumentException  {
         
        super(username, password, enabled, accountNonExpired, 
                credentialsNonExpired, accountNonLocked, authorities);
        this.userDomain = userDomain;
    }
}
