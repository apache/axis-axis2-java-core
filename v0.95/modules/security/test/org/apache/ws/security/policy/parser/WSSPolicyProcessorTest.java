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

package org.apache.ws.security.policy.parser;

import junit.framework.TestCase;
import org.apache.ws.security.policy.Constants;
import org.apache.ws.security.policy.model.PolicyEngineData;
import org.apache.ws.security.policy.model.RootPolicyEngineData;
import org.apache.ws.security.policy.model.SignedEncryptedParts;
import org.apache.ws.security.policy.model.SymmetricBinding;
import org.apache.ws.security.policy.model.Wss11;

import java.util.ArrayList;
import java.util.Iterator;

public class WSSPolicyProcessorTest extends TestCase {

    public WSSPolicyProcessorTest(String name) {
        super(name);
    }

    public void testSymmetricBinding() {
        try {
            WSSPolicyProcessor processor = new WSSPolicyProcessor();
            if (!processor.setup()) {
                return;
            }
            String[] files = new String[2];
            files[0] = "test-resources/policy/SecurityPolicyBindingsSymm.xml";
            files[1] = "test-resources/policy/SecurityPolicyMsg.xml";
            processor.go(files);
            
            RootPolicyEngineData rootPolicyEngineData = (RootPolicyEngineData)processor.secProcessorContext.getPedStack().get(0);
            assertNotNull("RootPolicyEngineData missing", rootPolicyEngineData);
            
            ArrayList peds = rootPolicyEngineData.getTopLevelPEDs();
            assertEquals("Incrrect number of PolicyEngineData", 4, peds.size());
            
            Iterator pedIter = peds.iterator();
            boolean symmBindingfound = false, wss11found = false, signedPartsFound = false, encryptedPartsFound = false;
            while (pedIter.hasNext()) {
                PolicyEngineData ped = (PolicyEngineData) pedIter.next();
                if(ped instanceof SymmetricBinding) {
                    symmBindingfound = true;
                    SymmetricBinding symmetricBinding = (SymmetricBinding)ped;
                    assertEquals("Incorrect layout",Constants.LAYOUT_STRICT ,symmetricBinding.getLayout().getValue());
                } else if(ped instanceof Wss11) {
                    wss11found = true;
                    Wss11 wss11 = (Wss11)ped;
                    assertEquals("Signature confirmation must be true", true,
                            wss11.isRequireSignatureConfirmation());
                } else if(ped instanceof SignedEncryptedParts) {
                    SignedEncryptedParts parts = (SignedEncryptedParts)ped;
                    if(parts.isSignedParts()) {
                        signedPartsFound = true;
                        assertEquals(
                                "Incorrect number of headers in SignedParts",
                                2, parts.getHeaders().size());
                    } else {
                        encryptedPartsFound = true;
                        assertEquals(
                                "Incorrect number of headers in EncryptedParts",
                                1, parts.getHeaders().size());
                    }
                }
            }
            assertTrue("SignedParts missing", signedPartsFound);
            assertTrue("EncryptedParts missing", encryptedPartsFound);
            assertTrue("SymmetricBinding missing", symmBindingfound);
            assertTrue("Wss11 missing", wss11found);
            
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
