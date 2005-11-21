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

/**
 * Constants of the WS-Trust implementation
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 *
 */
public interface Constants {
	
    static final String NS_YEAR_PREFIX = "http://schemas.xmlsoap.org/ws/2005/02/";
    public static final String WST_NS = NS_YEAR_PREFIX + "trust";
    public static final String WST_PREFIX = "wst";
    
    //local names of the token used in WS-Trust
    public interface LN {
	    public static final String TOKEN_TYPE = "TokenType";
	    public static final String REQUEST_TYPE = "RequestType";
	    public static final String KEY_TYPE = "KeyType";
	    public static final String KEY_SIZE = "KeySize";
	    public static final String LIFE_TIME = "Lifetime";
	    public static final String BASE = "Base";
	    public static final String STATUS = "Status";
	    public static final String CODE = "Code";
	    public static final String REASON = "Reason";
	    public static final String RENEWING = "Renewing";
	    public static final String RENEW_TARGET = "RenewTarget";
	    public static final String CANCEL_TARGET = "CancelTarget";
	    public static final String REQUESTED_TOKEN_CANCELLED = "RequestedTokenCancelled";
	    public static final String ALLOWPOSTDATING = "AllowPostdating";
	    public static final String BINARY_SECRET = "BinarySecret";
	    public static final String ENTROPY = "Entropy";
	    public static final String CLAIMS = "Claims";
	    public static final String COMPUTED_KEY = "ComputedKey";
	    
	    public static final String REQUEST_SECURITY_TOKEN = "RequestSecurityToken";
	    public static final String REQUEST_SECURITY_TOKEN_RESPONSE = "RequestSecurityTokenResponse";
	    public static final String REQUESTED_SECURITY_TOKEN = "RequestedSecurityToken";
	    public static final String REQUESTED_PROOF_TOKEN = "RequestedProofToken";
    }

    //Attributes
    public interface ATTR {
    	public static final String CONTEXT = "Context";
        public static final String BINARY_SECRET_TYPE = "Type";
        public static final String CLAIMS_DIALECT = "Dialect";
        public static final String RENEWING_ALLOW = "Allow";
        public static final String RENEWING_OK = "OK";
    }

    //RSTs
    public interface RST {
	    public static final String PREFIX = WST_NS + "/RST";
	    public static final String ISSUE_SECURITY_TOKEN = PREFIX + "/Issue";
	    public static final String RENEW_SECURITY_TOKEN = PREFIX + "/Renew";
	    public static final String VALIDATE_SECURITY_TOKEN = PREFIX + "/Validate";
	    public static final String CANCEL_SECURITY_TOKEN = PREFIX + "/Cancel";
    }
    
    //RSTRs
    public interface RSTR {
	    public static final String PREFIX = WST_NS + "/RSTR";
	    public static final String ISSUE_SECURITY_TOKEN = PREFIX + "/Issue";
	    public static final String RENEW_SECURITY_TOKEN = PREFIX + "/Renew";
	    public static final String VALIDATE_SECURITY_TOKEN = PREFIX + "/Validate";
	    public static final String CANCEL_SECURITY_TOKEN = PREFIX + "/Cancel";
    }
    
    // The request type is specified using following URIs as specified in the WS-Trust specification
    public interface REQ_TYPE {
	    public static final String ISSUE_SECURITY_TOKEN = WST_NS + "/Issue";
	    public static final String RENEW_SECURITY_TOKEN = WST_NS + "/Renew";
	    public static final String VALIDATE_SECURITY_TOKEN = WST_NS + "/Validate";
	    public static final String CANCEL_SECURITY_TOKEN = WST_NS + "/Cancel";    
    }
    
    //STATUS
    public interface STATUS {
	    public static final String PREFIX = WST_NS + "/status";
	    public static final String VALID = PREFIX + "/valid";
	    public static final String INVALID = PREFIX + "/invalid";
    }
    
    //Token types
    public interface TOKEN_TYPE {
    	public static final String RSTR_STATUS = RSTR.PREFIX + "/Status";
    	public final static String UNT = "http://schemas.xmlsoap.org/ws/2004/04/security/sc/unt";
    	public final static String SCT = "http://schemas.xmlsoap.org/ws/2004/04/security/sc/sct";
    }
    
    //Binary secret types
    public interface BINARY_SECRET_TYPE {
	    public static final String ASYMMETRIC_KEY = WST_NS + "/AsymmetricKey";
		public static final String SYMMETRIC_KEY = WST_NS + "/SymmetricKey";
		public static final String NONCE_VAL= WST_NS + "/Nonce";
    }
    
    //ComputedKey types
    public interface COMPUTED_KEY_TYPE {
    	public static final String PSHA1 = WST_NS + "/CK/PSHA1";	
    }
     
    public interface WSU {
        public static final String NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
        public static final String PREFIX = "wsu";
        public static final String CREATED_LN = "Created";
        public static final String EXPIRES_LN = "Expires";
        public static final String ID_ATTR = "Id";
    }
    
    public interface WSP {
        public static final String NS = "http://schemas.xmlsoap.org/ws/2004/09/policy";
        public static final String PREFIX = "wsp";	
        public static final String APPLIESTO_LN = "AppliesTo";
    }

}
