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

package org.apache.axis2.security.rahas;

/**
 * Constants of the Rahas handlers
 */
public interface RahasHandlerConstants {

    /**
     * Key to hold the <code>TokenStore</code> to store the 
     * <code>SecurityContextToken</code>s 
     * 
     * @see org.apache.axis2.security.trust.TokenStorage
     */
    public final static String TOKEN_STORE_KEY = "tokenStore";

    /**
     * Key to hod the map of security context identifiers against the 
     * service epr addresses (service scope) or wsa:Action values (operation 
     * scope).
     */
    public final static String CONTEXT_MAP_KEY = "contextMap";
    
    /**
     * The <code>java.util.Properties</code> object holding the properties 
     * of a <code>org.apache.ws.security.components.crypto.Crypto</code> impl.
     * 
     * This should ONLY be used when the CRYPTO_CLASS_KEY is specified.
     * 
     * @see org.apache.ws.security.components.crypto.Crypto
     */
    public final static String CRYPTO_PROPERTIES_KEY = "cryptoPropertiesRef";
    
    /**
     * The class that implements 
     * <code>org.apache.ws.security.components.crypto.Crypto</code>.
     */
    public final static String CRYPTO_CLASS_KEY = "cryptoClass";
    
}
