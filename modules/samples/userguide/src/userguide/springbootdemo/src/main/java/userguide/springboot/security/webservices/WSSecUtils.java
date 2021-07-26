
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

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class WSSecUtils {

    private static final Logger logger = LogManager.getLogger(WSSecUtils.class);
    
    protected JWTUserDTO getParsedUser(String token) throws Exception {
        String logPrefix = "WSSecUtils.getParsedUser() , ";

        // JWT and JWE are specifications to generate and validate tokens 
        // securely, however they require a public / private key pair using
        // elliptic curve cryptography and that is beyond the scope of this
        // userguide.
        // See below:
        // https://datatracker.ietf.org/doc/html/rfc7516
        // https://datatracker.ietf.org/doc/html/rfc7519

        // token generated via RandomStringUtils.randomAlphanumeric(20);
        // Do not use this for production code. 
        if (token == null || token.length() != 20) {
            throw new Exception("Invalid Token");
        }
        try {
            // All of this info is available in the JWT spec
            // however that is beyond the scope of this userguide
            JWTUserDTO user = new JWTUserDTO();
            user.setUsername("java-dev@axis.apache.org");
            user.setRole("ROLE_USER");
            // JWT ID that could be from the "Claimset" i.e.
            // jwt.getJWTClaimsSet().getJWTID());
            user.setUuid(UUID.randomUUID().toString());
                       
            return user;
        
        } catch (Exception ex) {
            logger.error(logPrefix + "failed: " + ex.getMessage(), ex);
            throw new JWTTokenMalformedException("unexpected error parsing token");
        }
        
    }

    public final LoginDTO findUserByEmail(String email) {

        String logPrefix = "WSSecUtils.findUserByEmail() , " ;

        if (email != null && email.equals("java-dev@axis.apache.org")) {
            LoginDTO persistedUser = new LoginDTO("java-dev@axis.apache.org", "userguide", true, true, true, true);
            return persistedUser;
        }

        logger.error(logPrefix + "Unknown email: " + email);

        return null;

    }

}
