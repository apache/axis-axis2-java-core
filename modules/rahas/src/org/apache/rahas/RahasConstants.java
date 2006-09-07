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


public class RahasConstants {
    
    public final static int VERSION_05_02 = 1;
    public final static int VERSION_05_12 = 2;
    
    /**
     * WS-Trust 2005 Feb namespace
     */
    public final static String WST_NS_05_02 = "http://schemas.xmlsoap.org/ws/2005/02/trust";
    
    /**
     * WS-SX Namespace
     */
    public final static String WST_NS_05_12 = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";
    
    public final static String WST_PREFIX = "wst";
    
    public final static String WSP_NS = "http://schemas.xmlsoap.org/ws/2004/09/policy";
    public final static String WSP_PREFIX = "wsp";
    
    //Local names
    public final static String REQUEST_TYPE_LN = "RequestType";
    public final static String TOKEN_TYPE_LN = "TokenType";
    public final static String REQUEST_SECURITY_TOKEN_LN = "RequestSecurityToken";
    public static final String REQUESTED_PROOF_TOKEN_LN = "RequestedProofToken";
    public static final String REQUEST_SECURITY_TOKEN_RESPONSE_LN = "RequestSecurityTokenResponse";
    public static final String REQUEST_SECURITY_TOKEN_RESPONSE_COLLECTION_LN = "RequestSecurityTokenResponseCollection";
    public static final String REQUESTED_SECURITY_TOKEN_LN = "RequestedSecurityToken";
    public final static String BINARY_SECRET_LN = "BinarySecret";
    public final static String COMPUTED_KEY_ALGO_LN = "ComputedKeyAlgorithm";
    public final static String COMPUTED_KEY_LN = "ComputedKey";
    public final static String REQUESTED_ATTACHED_REFERENCE_LN = "RequestedAttachedReference";
    public final static String REQUESTED_UNATTACHED_REFERENCE_LN = "RequestedUnattachedReference";
    public final static String KEY_SIZE_LN = "KeySize";
    public final static String KEY_TYPE_LN = "KeyType";
    public final static String ENTROPY_LN = "Entropy";
    public final static String APPLIES_TO_LN = "AppliesTo";
    public final static String LIFETIME_LN = "Lifetime";
    
    
    //Key types
    public final static String KEY_TYPE_SYMM_KEY = "/SymmetricKey";
    public final static String KEY_TYPE_PUBLIC_KEY = "/PublicKey";
    public final static String KEY_TYPE_BEARER = "/Bearer";
    
    //Attr values
    public final static String BIN_SEC_TYPE_NONCE =  "/Nonce";
    
    //ComputedKey algos
    public final static String COMPUTED_KEY_PSHA1 =  "/CK/PSHA1";
    
//  RequestTypes
    public final static String REQ_TYPE_ISSUE = "/Issue";
    public final static String REQ_TYPE_VALIDATE = "/Validate";
    public final static String REQ_TYPE_RENEW = "/Renew";
    public final static String REQ_TYPE_CANCEL = "/Cancel";
    
    //RST actions
    public final static String RST_ACTON_ISSUE =  "/RST/Issue";
    public final static String RST_ACTON_VALIDATE = "/RST/Renew";
    public final static String RST_ACTON_RENEW = "/RST/Cancel";
    public final static String RST_ACTON_CANCEL = "/RST/Validate";
    public final static String RST_ACTON_SCT = "/RST/SCT";
    
    //RSTR actions
    public final static String RSTR_ACTON_ISSUE = "/RSTR/Issue";
    public final static String RSTR_ACTON_VALIDATE = "/RSTR/Renew";
    public final static String RSTR_ACTON_RENEW = "/RSTR/Cancel";
    public final static String RSTR_ACTON_CANCEL = "/RSTR/Validate";
    public final static String RSTR_ACTON_SCT = "/RSTR/SCT";

    
    public class V_05_02 { 
        
        //Token types
        public final static String TOK_TYPE_SCT = "http://schemas.xmlsoap.org/ws/2005/02/sc/sct";

    }
    
    public class V_05_12 { 
        
        //Token types
        public final static String TOK_TYPE_SCT = "http://schemas.xmlsoap.org/ws/2005/12/sc/sct";
        
    }
    
    //Token types
    public final static String TOK_TYPE_SAML_10="http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1";
 
    //Attrs
    public final static String ATTR_TYPE = "Type";
    

}
