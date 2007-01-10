/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.ws.secpolicy.builders;

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.ws.secpolicy.Constants;
import org.apache.ws.secpolicy.model.X509Token;

public class X509TokenBuilder implements AssertionBuilder {

    public Assertion build(OMElement element, AssertionBuilderFactory factory)
            throws IllegalArgumentException {
        X509Token x509Token = new X509Token();

        OMElement policyElement = element.getFirstElement();
        
        //Process token inclusion
        OMAttribute  includeAttr = element.getAttribute(Constants.INCLUDE_TOKEN);
        if(includeAttr != null) {
            x509Token.setInclusion(includeAttr.getAttributeValue());
        }

        if (policyElement != null) {
            
            if (policyElement.getFirstChildWithName(Constants.REQUIRE_DERIVED_KEYS) != null) {
                x509Token.setDerivedKeys(true);
            }
            
            Policy policy = PolicyEngine.getPolicy(element.getFirstElement());
            policy = (Policy) policy.normalize(false);

            for (Iterator iterator = policy.getAlternatives(); iterator
                    .hasNext();) {
                processAlternative((List) iterator.next(), x509Token);
                
                /*
                 * since there should be only one alternative
                 */
                break;
            }
        }
        return x509Token;
    }

    private void processAlternative(List assertions, X509Token parent) {
                Assertion assertion;
        QName name;

        for (Iterator iterator = assertions.iterator(); iterator.hasNext();) {
            assertion = (Assertion) iterator.next();
            name = assertion.getName();

            if (Constants.REQUIRE_KEY_IDENTIFIRE_REFERENCE.equals(name)) {
                parent.setRequireKeyIdentifierReference(true);

            } else if (Constants.REQUIRE_ISSUER_SERIAL_REFERENCE.equals(name)) {
                parent.setRequireIssuerSerialReference(true);

            } else if (Constants.REQUIRE_EMBEDDED_TOKEN_REFERENCE.equals(name)) {
                parent.setRequireEmbeddedTokenReference(true);

            } else if (Constants.REQUIRE_THUMBPRINT_REFERENCE.equals(name)) {
                parent.setRequireThumbprintReference(true);

            } else if (Constants.WSS_X509_V1_TOKEN_10.equals(name)) {
                parent.setTokenVersionAndType(Constants.WSS_X509_V1_TOKEN10);

            } else if (Constants.WSS_X509_V1_TOKEN_11.equals(name)) {
                parent.setTokenVersionAndType(Constants.WSS_X509_V1_TOKEN11);

            } else if (Constants.WSS_X509_V3_TOKEN_10.equals(name)) {
                parent.setTokenVersionAndType(Constants.WSS_X509_V3_TOKEN10);

            } else if (Constants.WSS_X509_V3_TOKEN_11.equals(name)) {
                parent.setTokenVersionAndType(Constants.WSS_X509_V3_TOKEN11);

            } else if (Constants.WSS_X509_PKCS7_TOKEN_10.equals(name)) {
                parent
                        .setTokenVersionAndType(Constants.WSS_X509_PKCS7_TOKEN10);

            } else if (Constants.WSS_X509_PKCS7_TOKEN_11.equals(name)) {
                parent
                        .setTokenVersionAndType(Constants.WSS_X509_PKCS7_TOKEN11);

            } else if (Constants.WSS_X509_PKI_PATH_V1_TOKEN_10.equals(name)) {
                parent
                        .setTokenVersionAndType(Constants.WSS_X509_PKI_PATH_V1_TOKEN10);

            } else if (Constants.WSS_X509_PKI_PATH_V1_TOKEN_11.equals(name)) {
                parent
                        .setTokenVersionAndType(Constants.WSS_X509_PKI_PATH_V1_TOKEN11);
            }
        }
    }

    public QName[] getKnownElements() {
        return new QName[] {Constants.X509_TOKEN};
    }
}
