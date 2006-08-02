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
import org.apache.rahas.PWCallback;
import org.apache.rampart.handler.config.InflowConfiguration;
import org.apache.rampart.handler.config.OutflowConfiguration;
import org.opensaml.XML;

import javax.xml.namespace.QName;

/**
 * RahasSAMLTokenTest with the WS-SX namespaces
 */
public class RahasSAMLTokenV1205Test extends TestClient {

    /**
     * @param name
     */
    public RahasSAMLTokenV1205Test(String name) {
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
            
            return rstElem;
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public OutflowConfiguration getClientOutflowConfiguration() {
        OutflowConfiguration ofc = new OutflowConfiguration();

        ofc.setActionItems("Signature Encrypt Timestamp");
        ofc.setUser("alice");
        ofc.setSignaturePropFile("rahas-sec.properties");
        ofc.setPasswordCallbackClass(PWCallback.class.getName());
        return ofc;
    }

    public InflowConfiguration getClientInflowConfiguration() {
        InflowConfiguration ifc = new InflowConfiguration();

        ifc.setActionItems("Signature Encrypt Timestamp");
        ifc.setPasswordCallbackClass(PWCallback.class.getName());
        ifc.setSignaturePropFile("rahas-sec.properties");
        
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
