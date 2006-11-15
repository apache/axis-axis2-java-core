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

package org.apache.rampart.policy.model;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;

import javax.xml.namespace.QName;

import java.util.Properties;

import junit.framework.TestCase;

public class RampartPolicyTest extends TestCase {
    
    public final static QName RAMPART_CONFIG_NAME = new QName(RampartConfig.NS,RampartConfig.RAMPART_CONFIG_LN);
    public final static QName CRYPTO_CONFIG_NAME = new QName(RampartConfig.NS,CryptoConfig.CRYPTO_LN);
    
    public void testLoadPolicy() {
        try {
            String xmlPath = "test-resources/policy/rampart-policy-1.xml";
            StAXOMBuilder builder = new StAXOMBuilder(xmlPath);
            
            OMElement elem = builder.getDocumentElement();
            
            Policy policy = PolicyEngine.getPolicy(elem);
            
            Assertion assertion = (Assertion)policy.getAssertions().get(0);
            
            assertEquals("Incorrect namespace in RampartConfig",
                    RAMPART_CONFIG_NAME.getNamespaceURI(), assertion.getName()
                            .getNamespaceURI());
            assertEquals("Incorrect localname in RampartConfig",
                    RAMPART_CONFIG_NAME.getLocalPart(), assertion.getName()
                            .getLocalPart());

            RampartConfig config = (RampartConfig) assertion;
            CryptoConfig sigCryptoConfig = config.getSigCryptoConfig();

            assertNotNull("Signature Crypto missing", sigCryptoConfig);
            
            assertEquals("Incorrect namespace in SignatureCrypto",
                    CRYPTO_CONFIG_NAME.getNamespaceURI(), sigCryptoConfig
                            .getName().getNamespaceURI());
            assertEquals("Incorrect localname in SignatureCrypto",
                    CRYPTO_CONFIG_NAME.getLocalPart(), sigCryptoConfig.getName()
                            .getLocalPart());
            
            assertEquals("Incorrect provider value",
                    "org.apache.ws.security.components.crypto.Merlin",
                    sigCryptoConfig.getProvider());
            
            Properties prop = sigCryptoConfig.getProp();
            assertEquals("Incorrect number of properties", 3, prop.size());
            
            assertEquals("Incorrect property value", "JKS", prop
                    .getProperty("keystoreType"));
            assertEquals("Incorrect property value", "/path/to/file.jks", prop
                    .getProperty("keystoreFile"));
            assertEquals("Incorrect property value", "password", prop
                    .getProperty("keystorePassword"));
            
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
    }
    
}