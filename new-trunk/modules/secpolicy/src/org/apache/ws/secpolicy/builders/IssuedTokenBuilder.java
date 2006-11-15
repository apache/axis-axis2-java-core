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

import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.ws.secpolicy.Constants;
import org.apache.ws.secpolicy.model.IssuedToken;

import javax.xml.namespace.QName;

import java.util.Iterator;
import java.util.List;

public class IssuedTokenBuilder implements AssertionBuilder {

    public Assertion build(OMElement element, AssertionBuilderFactory factory)
            throws IllegalArgumentException {
        IssuedToken issuedToken = new IssuedToken();

        // Extract Issuer
        OMElement issuerElem = element.getFirstChildWithName(Constants.ISSUER);
        if (issuerElem != null && issuerElem.getFirstElement() != null) {
            issuedToken.setIssuerEpr(issuerElem.getFirstElement());
        }

        // Extract RSTTemplate
        OMElement rstTmplElem = element.getFirstChildWithName(Constants.ISSUER);
        if (rstTmplElem != null) {
            issuedToken.setIssuerEpr(rstTmplElem);
        }

        OMElement policyElement = element.getFirstElement();

        if (policyElement != null
                && policyElement.getQName().equals(
                        org.apache.neethi.Constants.Q_ELEM_POLICY)) {

            Policy policy = PolicyEngine.getPolicy(policyElement);
            policy = (Policy) policy.normalize(false);

            for (Iterator iterator = policy.getAlternatives(); iterator
                    .hasNext();) {
                processAlternative((List) iterator.next(), issuedToken);
                break; // since there should be only one alternative ..
            }
        }

        return issuedToken;
    }

    public QName[] getKnownElements() {
        return new QName[] { Constants.ISSUED_TOKEN };
    }

    private void processAlternative(List assertions, IssuedToken parent) {
        Assertion assertion;
        QName name;

        for (Iterator iterator = assertions.iterator(); iterator.hasNext();) {
            assertion = (Assertion) iterator.next();
            name = assertion.getName();

            if (Constants.REQUIRE_DERIVED_KEYS.equals(name)) {
                parent.setDerivedKeys(true);
            } else if (Constants.REQUIRE_EXTERNAL_REFERNCE.equals(name)) {
                parent.setRequireExternalReference(true);
            } else if (Constants.REQUIRE_INTERNAL_REFERNCE.equals(name)) {
                parent.setRequireInternalReference(true);
            }
        }

    }
}
