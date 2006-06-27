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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.util.StreamWrapper;
import org.apache.rahas.types.RequestSecurityTokenType;
import org.apache.rampart.handler.config.InflowConfiguration;
import org.apache.rampart.handler.config.OutflowConfiguration;
import org.apache.rampart.util.Axis2Util;
import org.opensaml.XML;

import javax.xml.namespace.QName;

public class RahasSAMLTokenTest extends TestClient {


    /**
     * @param name
     */
    public RahasSAMLTokenTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see org.apache.rahas.TestClient#getClientOutflowConfiguration()
     */
    public OutflowConfiguration getClientOutflowConfiguration() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.rahas.TestClient#getClientInflowConfiguration()
     */
    public InflowConfiguration getClientInflowConfiguration() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.rahas.TestClient#getServiceRepo()
     */
    public String getServiceRepo() {
        return "rahas_service_repo_1";
    }

    public OMElement getRequest() {

        RequestSecurityTokenType rst = new RequestSecurityTokenType();
        try {
            rst.setRequestType(new URI(org.apache.rahas.Constants.REQ_TYPE_ISSUE));
            rst.setTokenType(new URI(org.apache.rahas.Constants.TOK_TYPE_SAML_10));
            rst.setContext(new URI("http://get.optional.attrs.working"));
            
            Axis2Util.useDOOM(false);
            StAXOMBuilder builder = new StAXOMBuilder(new StreamWrapper(rst
                    .getPullParser(new QName(org.apache.rahas.Constants.WST_NS,
                            org.apache.rahas.Constants.REQUEST_SECURITY_TOKEN_LN))));

            OMElement rstElem = builder.getDocumentElement();
            
            rstElem.build();
            rstElem = (OMElement)rstElem.detach();
            return rstElem;
            
        } catch (Exception e) {
            throw  new RuntimeException(e);    
        }
    }
    
    public void validateRsponse(OMElement resp) {
        System.out.println(resp);
        OMElement elem = resp.getFirstChildWithName(new QName(XML.SAML_NS, "Assertion"));
        assertNotNull("Missing SAML Assertoin", elem);
    }


}
