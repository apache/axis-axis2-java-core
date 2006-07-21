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
    public final static String REQUESTED_ATTACHED_REFERENCE_LN = "RequestedAttachedReference";
    public final static String REQUESTED_UNATTACHED_REFERENCE_LN = "RequestedUnattachedReference";
    public final static String KEY_SIZE_LN = "KeySize";
    public final static String KEY_TYPE_LN = "KeyType";
    public final static String ENTROPY_LN = "Entropy";
    public final static String APPLIES_TO_LN = "AppliesTo";
    public final static String LIFETIME_LN = "Lifetime";
    
    
    public class V_05_02 { 
        //RequestTypes
        public final static String REQ_TYPE_ISSUE = WST_NS_05_02 + "/Issue";
        public final static String REQ_TYPE_VALIDATE = WST_NS_05_02 + "/Validate";
        public final static String REQ_TYPE_RENEW = WST_NS_05_02 + "/Renew";
        public final static String REQ_TYPE_CANCEL = WST_NS_05_02 + "/Cancel";
        
        //RST actions
        public final static String RST_ACTON_ISSUE = WST_NS_05_02 + "/RST/Issue";
        public final static String RST_ACTON_VALIDATE = WST_NS_05_02 + "/RST/Renew";
        public final static String RST_ACTON_RENEW = WST_NS_05_02 + "/RST/Cancel";
        public final static String RST_ACTON_CANCEL = WST_NS_05_02 + "/RST/Validate";
        public final static String RST_ACTON_SCT = WST_NS_05_02 + "/RST/SCT";
        
        //RSTR actions
        public final static String RSTR_ACTON_ISSUE = WST_NS_05_02 + "/RSTR/Issue";
        public final static String RSTR_ACTON_VALIDATE = WST_NS_05_02 + "/RSTR/Renew";
        public final static String RSTR_ACTON_RENEW = WST_NS_05_02 + "/RSTR/Cancel";
        public final static String RSTR_ACTON_CANCEL = WST_NS_05_02 + "/RSTR/Validate";
        public final static String RSTR_ACTON_SCT = WST_NS_05_02 + "/RSTR/SCT";
        //Attr values
        public final static String BIN_SEC_TYPE_NONCE = WST_NS_05_02 + "/Nonce";
        
        //Token types
        public final static String TOK_TYPE_SCT = "http://schemas.xmlsoap.org/ws/2005/02/sc/sct";
        
        //Key types
        public final static String KEY_TYPE_SYMM_KEY = WST_NS_05_02 + "/SymmetricKey";
        public final static String KEY_TYPE_PUBLIC_KEY = WST_NS_05_02 + "/PublicKey";
        public final static String KEY_TYPE_BEARER = WST_NS_05_02 + "/Bearer";
    }
    
    public class V_05_12 { 
        //RequestTypes
        public final static String REQ_TYPE_ISSUE = WST_NS_05_12 + "/Issue";
        public final static String REQ_TYPE_VALIDATE = WST_NS_05_12 + "/Validate";
        public final static String REQ_TYPE_RENEW = WST_NS_05_12 + "/Renew";
        public final static String REQ_TYPE_CANCEL = WST_NS_05_12 + "/Cancel";
        
        //RST actions
        public final static String RST_ACTON_ISSUE = WST_NS_05_12 + "/RST/Issue";
        public final static String RST_ACTON_VALIDATE = WST_NS_05_12 + "/RST/Renew";
        public final static String RST_ACTON_RENEW = WST_NS_05_12 + "/RST/Cancel";
        public final static String RST_ACTON_CANCEL = WST_NS_05_12 + "/RST/Validate";
        public final static String RST_ACTON_SCT = WST_NS_05_12 + "/RST/SCT";
        
        //RSTR actions
        public final static String RSTR_ACTON_ISSUE = WST_NS_05_12 + "/RSTR/Issue";
        public final static String RSTR_ACTON_VALIDATE = WST_NS_05_12 + "/RSTR/Renew";
        public final static String RSTR_ACTON_RENEW = WST_NS_05_12 + "/RSTR/Cancel";
        public final static String RSTR_ACTON_CANCEL = WST_NS_05_12 + "/RSTR/Validate";
        public final static String RSTR_ACTON_SCT = WST_NS_05_12 + "/RSTR/SCT";
        //Attr values
        public final static String BIN_SEC_TYPE_NONCE = WST_NS_05_12 + "/Nonce";
        
        //Token types
        public final static String TOK_TYPE_SCT = "http://schemas.xmlsoap.org/ws/2005/12/sc/sct";
        
        //Key types
        public final static String KEY_TYPE_SYMM_KEY = WST_NS_05_12 + "/SymmetricKey";
        public final static String KEY_TYPE_PUBLIC_KEY = WST_NS_05_12 + "/PublicKey";
        public final static String KEY_TYPE_BEARER = WST_NS_05_12 + "/Bearer";
    }
    
    //Token types
    public final static String TOK_TYPE_SAML_10="http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1";
 
    //Attrs
    public final static String ATTR_TYPE = "Type";
    

}
