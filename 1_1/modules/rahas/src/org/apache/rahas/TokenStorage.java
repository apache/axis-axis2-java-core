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

package org.apache.rahas;


/**
 * The storage interface to store security tokens and
 * manipulate them  
 */
public interface TokenStorage {
    
    String TOKEN_STORAGE_KEY = "org.apache.rahas.TokenStorage";
    
    /**
     * Add the given token to the list.
     * @param token The token to be added
     * @throws TrustException
     */
    void add(Token token) throws TrustException;
    
    /**
     * Update an existing token.
     * @param token
     * @throws TrustException
     */
    void update(Token token) throws TrustException;
    
    /**
     * Return the list of all token identifiers.
     * @return As array of token identifiers
     * @throws TrustException
     */
    String[] getTokenIdentifiers() throws TrustException;

    /**
     * Return the list of <code>EXPIRED</code> tokens.
     * If there are no <code>EXPIRED</code> tokens <code>null</code> will be 
     * returned
     * @return An array of expired <code>Tokens</code>
     * @throws TrustException
     */
    Token[] getExpiredTokens() throws TrustException;
    
    /**
     * Return the list of ISSUED and RENEWED tokens.
     * @return An array of ISSUED and RENEWED <code>Tokens</code>.
     * @throws TrustException
     */
    Token[] getValidTokens() throws TrustException;
    
    /**
     * Return the list of RENEWED tokens.
     * @return An array of RENEWED <code>Tokens</code>
     * @throws TrustException
     */
    Token[] getRenewedTokens() throws TrustException;
    
    /**
     * Return the list of CANCELLED tokens
     * @return An array of CANCELLED <code>Tokens</code>
     * @throws TrustException
     */
    Token[] getCancelledTokens() throws TrustException;
    
    /**
     * Returns the <code>Token</code> of the given id
     * @param id
     * @return The requested <code>Token</code> identified by the give id
     * @throws TrustException
     */
    Token getToken(String id) throws TrustException;
    
}
