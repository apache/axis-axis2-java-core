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

package org.apache.axis2.security.handler;

/**
 * Constants specific to the Axis2 security module
 */
public interface WSSHandlerConstants {

    /**
     * Name of the .mar file
     */
    public final static String SECURITY_MODULE_NAME = "security";
    
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
}
