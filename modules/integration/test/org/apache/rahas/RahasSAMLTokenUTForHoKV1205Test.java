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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.Base64;
import org.apache.rampart.handler.config.InflowConfiguration;
import org.apache.rampart.handler.config.OutflowConfiguration;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.util.WSSecurityUtil;
import org.opensaml.XML;

import javax.xml.namespace.QName;

public class RahasSAMLTokenUTForHoKV1205Test extends TestClient {

    byte[] clientEntr;
    
    /**
     * @param name
     */
    public RahasSAMLTokenUTForHoKV1205Test(String name) {
        super(name);
    }

    public OMElement getRequest() {
        try {
            OMElement rstElem = TrustUtil.createRequestSecurityTokenElement(RahasConstants.VERSION_05_12);
            OMElement reqTypeElem = TrustUtil.createRequestTypeElement(RahasConstants.VERSION_05_12, rstElem);
            OMElement tokenTypeElem = TrustUtil.createTokenTypeElement(RahasConstants.VERSION_05_12, rstElem);
            reqTypeElem.setText(RahasConstants.V_05_12.REQ_TYPE_ISSUE);
            tokenTypeElem.setText(RahasConstants.TOK_TYPE_SAML_10);
            
            TrustUtil.createAppliesToElement(rstElem,
                    "http://localhost:5555/axis2/services/SecureService");
            TrustUtil.createKeyTypeElement(RahasConstants.VERSION_05_12,
                    rstElem, RahasConstants.KEY_TYPE_SYMM_KEY);
            TrustUtil.createKeySizeElement(RahasConstants.VERSION_05_12, rstElem, 256);
            
            byte[] nonce = WSSecurityUtil.generateNonce(16);
            clientEntr = nonce;
            OMElement entrElem = TrustUtil.createEntropyElement(RahasConstants.VERSION_05_12, rstElem);
            TrustUtil.createBinarySecretElement(RahasConstants.VERSION_05_12, entrElem, RahasConstants.V_05_12.BIN_SEC_TYPE_NONCE).setText(Base64.encode(nonce));
            TrustUtil.createComputedKeyAlgorithm(RahasConstants.VERSION_05_12,rstElem, RahasConstants.V_05_12.COMPUTED_KEY_PSHA1);
            
            return rstElem;
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OutflowConfiguration getClientOutflowConfiguration() {
        OutflowConfiguration ofc = new OutflowConfiguration();

        ofc.setActionItems("UsernameToken Timestamp");
        ofc.setUser("joe");
        ofc.setPasswordType(WSConstants.PW_TEXT);
        ofc.setPasswordCallbackClass(PWCallback.class.getName());
        return ofc;
    }

    public InflowConfiguration getClientInflowConfiguration() {
        InflowConfiguration ifc = new InflowConfiguration();

        ifc.setActionItems("Timestamp");
        
        return ifc;
    }

    public String getServiceRepo() {
        return "rahas_service_repo_3";
    }

    public String getRequestAction() {
        return RahasConstants.V_05_12.RST_ACTON_ISSUE;
    }

    public void validateRsponse(OMElement resp) {
        OMElement rstr = resp.getFirstChildWithName(new QName(RahasConstants.WST_NS_05_12, RahasConstants.REQUEST_SECURITY_TOKEN_RESPONSE_LN));
        assertNotNull("RequestedSecurityTokenResponse missing", rstr);
        OMElement rst = rstr.getFirstChildWithName(new QName(RahasConstants.WST_NS_05_12, RahasConstants.REQUESTED_SECURITY_TOKEN_LN));
        assertNotNull("RequestedSecurityToken missing", rst);
        
        OMElement elem = rst.getFirstChildWithName(new QName(XML.SAML_NS, "Assertion"));
        assertNotNull("Missing SAML Assertoin", elem);
        
        OMElement attrStmtElem = elem.getFirstChildWithName(new QName(XML.SAML_NS, "AttributeStatement"));
        OMElement kiElem = attrStmtElem.getFirstChildWithName(new QName(XML.SAML_NS,"Subject")).getFirstChildWithName(new QName(XML.SAML_NS,"SubjectConfirmation")).getFirstChildWithName(new QName("http://www.w3.org/2000/09/xmldsig#", "KeyInfo"));
        OMElement encrKey = kiElem.getFirstChildWithName(new QName("http://www.w3.org/2001/04/xmlenc#", "EncryptedKey"));
//        
//        String cipherValue = encrKey.getFirstChildWithName(new QName("http://www.w3.org/2001/04/xmlenc#", "CipherData")).getFirstChildWithName(new QName("http://www.w3.org/2001/04/xmlenc#", "CipherValue")).getText();
//        System.out.println(cipherValue);
//
//        
//
//        String serviceEntropyValue = rstr.getFirstChildWithName(new QName(RahasConstants.WST_NS_05_12, RahasConstants.ENTROPY_LN)).getFirstChildWithName(new QName(RahasConstants.WST_NS_05_12, RahasConstants.BINARY_SECRET_LN)).getText();
//        
//        byte[] serviceEntr = Base64.decode(serviceEntropyValue);
//        
//        try {
//            P_SHA1 p_sha1 = new P_SHA1();
//            
//            KeyStore ks = KeyStore.getInstance("JKS");
//            FileInputStream ksfis = new FileInputStream("/home/ruchith/workspace/sx-interop-aug-06/config/rahas-sts.jks");
//            BufferedInputStream ksbufin = new BufferedInputStream(ksfis);
//            ks.load(ksbufin, "password".toCharArray()); // Populate the keystore
//          
//            Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPPADDING", "BC");
//            cipher.init(Cipher.ENCRYPT_MODE, ks.getKey("bob", "password".toCharArray()));
//            System.out.println("\n\nCipherDataValue:\n" + Base64.encode(cipher.doFinal(serviceEntr)));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

}
