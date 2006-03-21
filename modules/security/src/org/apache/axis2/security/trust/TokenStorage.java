/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.security.trust;

import java.util.ArrayList;

/**
 * The storage interface to store security tokens and
 * manipulate them  
 */
public interface TokenStorage {
    
    /**
     * Add the given token to the list.
     * @param token The token to be added
     * @throws TrustException
     */
    public void add(Token token) throws TrustException;
    
    /**
     * Update an existing token.
     * @param token
     * @throws TrustException
     */
    public void update(Token token) throws TrustException;
    
    /**
     * Return the list of all token identifiers.
     * @return
     * @throws TrustException
     */
    public String[] gettokenIdentifiers() throws TrustException;

    /**
     * Return the list of <code>EXPIRED</code> tokens.
     * If there are no <code>EXPIRED</code> tokens <code>null</code> will be 
     * returned
     * @return
     * @throws TrustException
     */
    public ArrayList getExpiredTokens() throws TrustException;
    
    /**
     * Return the list of ISSUED and RENEWED tokens.
     * @return
     * @throws TrustException
     */
    public ArrayList getValidTokens() throws TrustException;
    
    /**
     * Return the list of RENEWED tokens.
     * @return
     * @throws TrustException
     */
    public ArrayList getRenewedTokens() throws TrustException;
    
    /**
     * Return the list of CANCELLED tokens
     * @return
     * @throws TrustException
     */
    public ArrayList getCancelledTokens() throws TrustException;
    
}
