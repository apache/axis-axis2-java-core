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

package org.apache.axis2.policy.model;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;

import java.util.Iterator;
import java.util.List;

public class MTOMAssertionTest extends TestCase {


    public void testSymmBinding() {
        try {
            Policy p = this.getPolicy(System.getProperty("basedir", ".") +
                    "/test-resources/policy-mtom-security.xml");
            List assertions = (List)p.getAlternatives().next();

            boolean isMTOMAssertionFound = false;

            for (Iterator iter = assertions.iterator(); iter.hasNext();) {
                Assertion assertion = (Assertion)iter.next();
                if (assertion instanceof MTOMAssertion) {
                    isMTOMAssertionFound = true;
                    MTOMAssertion mtomModel = (MTOMAssertion)assertion;
                    assertEquals("MIME Serialization assertion not processed", false,
                                 mtomModel.isOptional());
                }

            }
            //The Asymm binding mean is not built in the policy processing :-(
            assertTrue("MTOM Assertion not found.", isMTOMAssertionFound);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }


    private Policy getPolicy(String filePath) throws Exception {
        StAXOMBuilder builder = new StAXOMBuilder(filePath);
        OMElement elem = builder.getDocumentElement();
        return PolicyEngine.getPolicy(elem);
    }
}
