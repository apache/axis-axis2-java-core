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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.ws.secpolicy.Constants;
import org.apache.ws.secpolicy.model.Trust10;

public class Trust10Builder implements AssertionBuilder {

    public Assertion build(OMElement element, AssertionBuilderFactory factory)
            throws IllegalArgumentException {

        element = element.getFirstChildWithName(Constants.POLICY);

        if (element == null) {
            throw new IllegalArgumentException(
                    "Trust10 assertion doesn't contain any Policy");
        }

        Trust10 trust10 = new Trust10();

        if (element
                .getFirstChildWithName(Constants.MUST_SUPPORT_CLIENT_CHALLENGE) != null) {
            trust10.setMustSupportClientChallenge(true);
        }

        if (element
                .getFirstChildWithName(Constants.MUST_SUPPORT_SERVER_CHALLENGE) != null) {
            trust10.setMustSupportServerChallenge(true);
        }

        if (element.getFirstChildWithName(Constants.REQUIRE_CLIENT_ENTROPY) != null) {
            trust10.setRequireClientEntropy(true);
        }

        if (element.getFirstChildWithName(Constants.REQUIRE_SERVER_ENTROPY) != null) {
            trust10.setRequireServerEntropy(true);
        }

        if (element.getFirstChildWithName(Constants.MUST_SUPPORT_ISSUED_TOKENS) != null) {
            trust10.setMustSupportIssuedTokens(true);
        }

        return trust10;
    }

    public QName[] getKnownElements() {
        return new QName[] {Constants.TRUST_10};
    }

}
