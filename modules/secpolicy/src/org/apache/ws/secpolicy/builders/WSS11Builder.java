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

import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.ws.secpolicy.Constants;
import org.apache.ws.secpolicy.model.Wss11;

public class WSS11Builder implements AssertionBuilder {

    public Assertion build(OMElement element, AssertionBuilderFactory factory)
            throws IllegalArgumentException {
        Wss11 wss11 = new Wss11();

        Policy policy = PolicyEngine.getPolicy(element.getFirstElement());
        policy = (Policy) policy.normalize(false);

        for (Iterator iterator = policy.getAlternatives(); iterator.hasNext();) {
            processAlternative((List) iterator.next(), wss11);
            /*
             * since there should be only one alternative
             */
            break;
        }

        return wss11;
    }

    public QName[] getKnownElements() {
        return new QName[] {Constants.WSS11};
    }

    private void processAlternative(List assertions, Wss11 parent) {
        
        Assertion assertion;
        QName name;

        for (Iterator iterator = assertions.iterator(); iterator.hasNext();) {
            assertion = (Assertion) iterator.next();
            name = assertion.getName();

            if (Constants.MUST_SUPPORT_REF_KEY_IDENTIFIER.equals(name)) {
                parent.setMustSupportRefKeyIdentifier(true);

            } else if (Constants.MUST_SUPPORT_REF_ISSUER_SERIAL.equals(name)) {
                parent.setMustSupportRefIssuerSerial(true);

            } else if (Constants.MUST_SUPPORT_REF_EXTERNAL_URI.equals(name)) {
                parent.setMustSupportRefExternalURI(true);

            } else if (Constants.MUST_SUPPORT_REF_EMBEDDED_TOKEN.equals(name)) {
                parent.setMustSupportRefEmbeddedToken(true);
                
            } else if (Constants.MUST_SUPPORT_REF_THUMBPRINT.equals(name)) {
                parent.setMustSupportRefThumbprint(true);
                
            } else if (Constants.MUST_SUPPORT_REF_ENCRYPTED_KEY.equals(name)) {
                parent.setMustSupportRefEncryptedKey(true);
                
            } else if (Constants.REQUIRE_SIGNATURE_CONFIRMATION.equals(name)) {
                parent.setRequireSignatureConfirmation(true);
            }
        }
    }
}
