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

package org.apache.rampart.handler;

/**
 * Constants specific to the Axis2 security module
 */
public class WSSHandlerConstants {

    private WSSHandlerConstants() {
    }
    
    /**
     * Name of the .mar file
     */
    public final static String SECURITY_MODULE_NAME = "rampart";
    
   /**
     * Inflow security parameter
     */
    public static final String INFLOW_SECURITY = "InflowSecurity";
    
    public static final String INFLOW_SECURITY_SERVER = "InflowSecurity-server";
    public static final String INFLOW_SECURITY_CLIENT = "InflowSecurity-client";
    
    /**
     * Outflow security parameter 
     */
    public static final String OUTFLOW_SECURITY = "OutflowSecurity";
    
    public static final String OUTFLOW_SECURITY_SERVER = "OutflowSecurity-server";
    public static final String OUTFLOW_SECURITY_CLIENT = "OutflowSecurity-client";
    
    
    /**
     * Inflow security parameter of a client to talk to an STS 
     * when sec conv is used
     */
    public final static String STS_INFLOW_SECURITY = "STSInflowSecurity"; 

    /**
     * Outflow security parameter of a client to talk to an STS 
     * when sec conv is used
     */
    public final static String STS_OUTFLOW_SECURITY = "STSOutflowSecurity"; 

    
    public static final String ACTION = "action";
    
    public static final String ACTION_ITEMS = "items";
    

    /**
     *  Repetition count
     */
	public static final String SENDER_REPEAT_COUNT = "senderRepeatCount";

	/**
	 * The current repetition
	 */
	public static final String CURRENT_REPETITON = "currentRepetition";

	/**
	 * This is used to indicate the XPath expression used to indicate the
	 * Elements whose first child (must be a text node) is to be optimized  
	 */
	public static final String OPTIMIZE_PARTS = "optimizeParts";
	
	public static final String PRESERVE_ORIGINAL_ENV = "preserveOriginalEnvelope";
	
	
	/*
	 * These are useful in configuring using the OutflowConfiguration 
	 * and InflowConfiguration 
	 * The set of possible key identifiers
	 */
	
	public static final String BST_DIRECT_REFERENCE = "DirectReference";
	
	public static final String ISSUER_SERIAL = "IssuerSerial";
	
	public static final String X509_KEY_IDENTIFIER = "X509KeyIdentifier";
	
	public static final String SKI_KEY_IDENTIFIER = "SKIKeyIdentifier";
	
	public static final String EMBEDDED_KEYNAME = "EmbeddedKeyName";
	
	public static final String THUMBPRINT_IDENTIFIER = "Thumbprint";
	
    
    public final static String SIGN_ALL_HEADERS = "signAllHeaders";
    public final static String SIGN_BODY = "signBody";
    public final static String ENCRYPT_BODY = "encryptBody";
    
    /**
     * Key to be used to set a flag in msg ctx to enable/disable using doom
     */
    public final static String DISABLE_DOOM = "useDoom";
    
    
    ///
    /// WS-SecureConversation constants
    ///
    
    
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
    
    //TODO: Get these constants from the WS-Trust impl's constants
    public final static String RST_ACTON_SCT = "http://schemas.xmlsoap.org/ws/2005/02/trust/RST/SCT";
    public final static String RSTR_ACTON_SCT = "http://schemas.xmlsoap.org/ws/2005/02/trust/RSTR/SCT";
    public final static String RSTR_ACTON_ISSUE = "http://schemas.xmlsoap.org/ws/2005/02/trust/RSTR/Issue";
    
    public final static String TOK_TYPE_SCT = "http://schemas.xmlsoap.org/ws/2005/02/sc/sct";
    
    public final static String WST_NS = "http://schemas.xmlsoap.org/ws/2005/02/trust";
    public static final String REQUEST_SECURITY_TOKEN_RESPONSE_LN = "RequestSecurityTokenResponse";
    
}
