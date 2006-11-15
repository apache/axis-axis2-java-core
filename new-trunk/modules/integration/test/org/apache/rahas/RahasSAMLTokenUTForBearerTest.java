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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.neethi.Policy;
import org.apache.rampart.handler.config.InflowConfiguration;
import org.apache.rampart.handler.config.OutflowConfiguration;
import org.apache.ws.secpolicy.Constants;
import org.opensaml.XML;

import javax.xml.namespace.QName;

/**
 *
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class RahasSAMLTokenUTForBearerTest extends TestClient {

    public RahasSAMLTokenUTForBearerTest(String name) {
        super(name);
    }

    public OMElement getRequest() {
        try {
            OMElement rstElem = TrustUtil.createRequestSecurityTokenElement(RahasConstants.VERSION_05_02);
            TrustUtil.createRequestTypeElement(RahasConstants.VERSION_05_02, rstElem, RahasConstants.REQ_TYPE_ISSUE);
            OMElement tokenTypeElem = TrustUtil.createTokenTypeElement(RahasConstants.VERSION_05_02, rstElem);
            tokenTypeElem.setText(RahasConstants.TOK_TYPE_SAML_10);
            
            TrustUtil.createAppliesToElement(rstElem, "http://localhost:5555/axis2/services/SecureService", this.getWSANamespace());
            TrustUtil.createKeyTypeElement(RahasConstants.VERSION_05_02,
                    rstElem, RahasConstants.KEY_TYPE_BEARER);
            TrustUtil.createKeySizeElement(RahasConstants.VERSION_05_02, rstElem, 256);
            
            return rstElem;
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OutflowConfiguration getClientOutflowConfiguration() {
        OutflowConfiguration ofc = new OutflowConfiguration();

        ofc.setActionItems("UsernameToken Timestamp");
        ofc.setUser("joe");
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

    public String getRequestAction() throws TrustException {
        return TrustUtil.getActionValue(RahasConstants.VERSION_05_02, RahasConstants.RST_ACTION_ISSUE);
    }

    public void validateRsponse(OMElement resp) {
        OMElement rst = resp.getFirstChildWithName(new QName(RahasConstants.WST_NS_05_02,
                                                             RahasConstants.IssuanceBindingLocalNames.
                                                                     REQUESTED_SECURITY_TOKEN));
        assertNotNull("RequestedSecurityToken missing", rst);
        OMElement elem = rst.getFirstChildWithName(new QName(XML.SAML_NS, "Assertion"));
        assertNotNull("Missing SAML Assertoin", elem);
    }

    /* (non-Javadoc)
     * @see org.apache.rahas.TestClient#getServicePolicy()
     */
    public Policy getServicePolicy() throws Exception {
        return this.getPolicy("test-resources/rahas/policy/service-policy-transport-binding.xml");
    }

    /* (non-Javadoc)
     * @see org.apache.rahas.TestClient#getSTSPolicy()
     */
    public Policy getSTSPolicy() throws Exception {
        return this.getPolicy("test-resources/rahas/policy/sts-policy-transport-binding.xml");
    }

    /* (non-Javadoc)
     * @see org.apache.rahas.TestClient#getRSTTemplate()
     */
    public OMElement getRSTTemplate() throws TrustException {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement elem = factory.createOMElement(Constants.RST_TEMPLATE.getLocalPart(), factory.createOMNamespace(Constants.RST_TEMPLATE.getNamespaceURI(),"wsp"));
        
        TrustUtil.createTokenTypeElement(RahasConstants.VERSION_05_02, elem).setText(RahasConstants.TOK_TYPE_SAML_10);
        TrustUtil.createKeyTypeElement(RahasConstants.VERSION_05_02, elem, RahasConstants.KEY_TYPE_BEARER);
        
        return elem;
    }
    
    public int getTrstVersion() {
        return RahasConstants.VERSION_05_02;
    }
}
