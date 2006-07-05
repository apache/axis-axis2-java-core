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

public class Constants {
    
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
    
    public static String NS = WST_NS_05_02;
    
    public static void setVersion(String ns) {
        NS = ns;
    }

    //Local names
    public final static String REQUEST_TYPE_LN = "RequestType";
    public final static String TOKEN_TYPE_LN = "TokenType";
    public final static String REQUEST_SECURITY_TOKEN_LN = "RequestSecurityToken";
    public static final String REQUESTED_PROOF_TOKEN_LN = "RequestedProofToken";
    public static final String REQUEST_SECURITY_TOKEN_RESPONSE_LN = "RequestSecurityTokenResponse";
    public static final String REQUESTED_SECURITY_TOKEN_LN = "RequestedSecurityToken";
    public final static String BINARY_SECRET_LN = "BinarySecret";
    public final static String REQUESTED_ATTACHED_REFERENCE_LN = "RequestedAttachedReference";
    public final static String REQUESTED_UNATTACHED_REFERENCE_LN = "RequestedUnattachedReference";
    public final static String KEY_SIZE_LN = "KeySize";
    public final static String ENTROPY_LN = "Entropy";
    public final static String APPLIES_TO_LN = "AppliesTo";
    public final static String LIFETIME_LN = "Lifetime";
    
    //RequestTypes
    public final static String REQ_TYPE_ISSUE = NS + "/Issue";
    public final static String REQ_TYPE_VALIDATE = NS + "/Validate";
    public final static String REQ_TYPE_RENEW = NS + "/Renew";
    public final static String REQ_TYPE_CANCEL = NS + "/Cancel";

    //Token types
    public final static String TOK_TYPE_SCT = "http://schemas.xmlsoap.org/ws/2005/02/sc/sct";
    public final static String TOK_TYPE_SAML_10="http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1";
    
    
    //RST actions
    public final static String RST_ACTON_ISSUE = NS + "/RST/Issue";
    public final static String RST_ACTON_VALIDATE = NS + "/RST/Renew";
    public final static String RST_ACTON_RENEW = NS + "/RST/Cancel";
    public final static String RST_ACTON_CANCEL = NS + "/RST/Validate";
    public final static String RST_ACTON_SCT = NS + "/RST/SCT";
    
    //RSTR actions
    public final static String RSTR_ACTON_ISSUE = NS + "/RSTR/Issue";
    public final static String RSTR_ACTON_VALIDATE = NS + "/RSTR/Renew";
    public final static String RSTR_ACTON_RENEW = NS + "/RSTR/Cancel";
    public final static String RSTR_ACTON_CANCEL = NS + "/RSTR/Validate";
    public final static String RSTR_ACTON_SCT = NS + "/RSTR/SCT";
    
    //Attrs
    public final static String ATTR_TYPE = "Type";
    
    //Attr values
    public final static String BIN_SEC_TYPE_NONCE = NS + "/Nonce";
}
