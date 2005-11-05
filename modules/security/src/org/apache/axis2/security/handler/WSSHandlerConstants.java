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
 *  
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public interface WSSHandlerConstants {

    /**
     * Inflow security parameter
     */
    public static final String INFLOW_SECURITY = "InflowSecurity";

    /**
     * Outflow security parameter 
     */
    public static final String OUTFLOW_SECURITY = "OutflowSecurity";
    
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
	
}
