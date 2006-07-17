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
import org.apache.axis2.security.sc.PWCallback;
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

    /* (non-Javadoc)
     * @see org.apache.rahas.TestClient#getRequest()
     */
    public OMElement getRequest() {
        try {
            OMElement rstElem = TrustUtil.createRequestSecurityTokenElement(RahasConstants.VERSION_05_12);
            OMElement reqTypeElem = TrustUtil.createRequestTypeElement(RahasConstants.VERSION_05_12, rstElem);
            OMElement tokenTypeElem = TrustUtil.createTokenTypeElement(RahasConstants.VERSION_05_12, rstElem);
            reqTypeElem.setText(RahasConstants.V_05_12.REQ_TYPE_ISSUE);
            tokenTypeElem.setText(RahasConstants.TOK_TYPE_SAML_10);
            
            OMElement appliesToElem = TrustUtil.createAppliesToElement(rstElem);
            appliesToElem.setText("http://localhost:5555/axis2/services/SecureService");
            
            return rstElem;
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public OutflowConfiguration getClientOutflowConfiguration() {
        OutflowConfiguration ofc = new OutflowConfiguration();

        ofc.setActionItems("Timestamp Signature");
        ofc.setUser("alice");
        ofc.setSignaturePropFile("sec.properties");
        ofc.setPasswordCallbackClass(PWCallback.class.getName());
        return ofc;
    }

    public InflowConfiguration getClientInflowConfiguration() {
        InflowConfiguration ifc = new InflowConfiguration();

        ifc.setActionItems("Timestamp Signature");
        ifc.setPasswordCallbackClass(PWCallback.class.getName());
        ifc.setSignaturePropFile("sec.properties");
        
        return ifc;
    }

    /* (non-Javadoc)
     * @see org.apache.rahas.TestClient#getServiceRepo()
     */
    public String getServiceRepo() {
        return "rahas_service_repo_1";
    }
    
    public void validateRsponse(OMElement resp) {
        OMElement rst = resp.getFirstChildWithName(new QName(RahasConstants.WST_NS_05_12, RahasConstants.REQUESTED_SECURITY_TOKEN_LN));
        assertNotNull("RequestedSecurityToken missing", rst);
        OMElement elem = rst.getFirstChildWithName(new QName(XML.SAML_NS, "Assertion"));
        assertNotNull("Missing SAML Assertoin", elem);
    }

    public String getRequestAction() {
        return RahasConstants.V_05_12.RST_ACTON_ISSUE;
    }

}
