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
import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.rampart.handler.config.InflowConfiguration;
import org.apache.rampart.handler.config.OutflowConfiguration;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.utils.EncryptionConstants;
import org.opensaml.XML;

import javax.xml.namespace.QName;

public class RahasSAMLTokenCertForHoKV1205Test extends TestClient {


    public RahasSAMLTokenCertForHoKV1205Test(String name) {
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
                    "http://207.200.37.116/Ping/Scenario4");
            TrustUtil.createKeyTypeElement(RahasConstants.VERSION_05_12,
                    rstElem, RahasConstants.KEY_TYPE_PUBLIC_KEY);
            TrustUtil.createKeySizeElement(RahasConstants.VERSION_05_12, rstElem, 256);
            
            
            return rstElem;
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public OutflowConfiguration getClientOutflowConfiguration() {
        OutflowConfiguration ofc = new OutflowConfiguration();

//        ofc.setActionItems("Timestamp Signature Encrypt");
        ofc.setActionItems("Signature Encrypt Timestamp");
        ofc.setUser("alice");
        ofc.setEncryptionUser("ip");
        ofc.setSignaturePropFile("rahas-sec.properties");
        ofc.setSignatureKeyIdentifier(WSSHandlerConstants.BST_DIRECT_REFERENCE);
        ofc.setEncryptionKeyIdentifier(WSSHandlerConstants.SKI_KEY_IDENTIFIER);
        ofc.setEncryptionKeyTransportAlgorithm(XMLCipher.RSA_OAEP);
//        ofc.setEncryptionSymAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256);
        ofc.setPasswordCallbackClass(PWCallback.class.getName());
        ofc.setEnableSignatureConfirmation(false);
//        ofc.setSignatureParts("{Element}{http://schemas.xmlsoap.org/soap/envelope/}Body;" +
//                                "{Element}{" + RahasConstants.WSA_NS + "}To;" +
//                                "{Element}{" + RahasConstants.WSA_NS + "}ReplyTo;" +
//                                "{Element}{" + RahasConstants.WSA_NS + "}MessageID;" +
//                                "{Element}{" + RahasConstants.WSA_NS + "}Action;" +
//                                "{Element}{" + WSConstants.WSU_NS + "}Timestamp");
        
        return ofc;
    }

    public InflowConfiguration getClientInflowConfiguration() {
        InflowConfiguration ifc = new InflowConfiguration();

        ifc.setActionItems("Signature Encrypt Timestamp");
        ifc.setPasswordCallbackClass(PWCallback.class.getName());
        ifc.setSignaturePropFile("rahas-sec.properties");
        ifc.setEnableSignatureConfirmation(false);
        
        return ifc;
    }

    public String getServiceRepo() {
        return "rahas_service_repo_1";
    }
    
    public void validateRsponse(OMElement resp) {
        OMElement rstr = resp.getFirstChildWithName(new QName(RahasConstants.WST_NS_05_12, RahasConstants.REQUEST_SECURITY_TOKEN_RESPONSE_LN));
        assertNotNull("RequestedSecurityToken missing", rstr);
        OMElement rst = rstr.getFirstChildWithName(new QName(RahasConstants.WST_NS_05_12, RahasConstants.REQUESTED_SECURITY_TOKEN_LN));
        assertNotNull("RequestedSecurityToken missing", rst);
        OMElement elem = rst.getFirstChildWithName(new QName(XML.SAML_NS, "Assertion"));
        assertNotNull("Missing SAML Assertoin", elem);
        
    }

    public String getRequestAction() {
        return RahasConstants.V_05_12.RST_ACTON_ISSUE;
    }


}
