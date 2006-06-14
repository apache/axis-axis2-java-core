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

import junit.framework.TestCase;

public class TokenRequestDispatcherConfigTest extends TestCase {

    public TokenRequestDispatcherConfigTest() {
        super();
    }

    public TokenRequestDispatcherConfigTest(String arg0) {
        super(arg0);
    }

    /**
     * Testing a valid config file
     */
    public void testWithConfigFile() {
        try {
            TokenRequestDispatcherConfig config = TokenRequestDispatcherConfig
                    .load("test-resources/trust/dispatcher.config.xml");

            assertEquals("Incorrect default issuer class name",
                    "org.apache.rahas.TempIssuer", config
                            .getDefaultIssuerName());

            TokenIssuer issuer = config
                    .getIssuer("http://example.org/mySpecialToken1");

            assertEquals("Incorrect issuer for token type : "
                    + "http://example.org/mySpecialToken1", TempIssuer.class
                    .getName(), issuer.getClass().getName());

        } catch (TrustException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Testing expected faliure when the default issuer is not specified
     */
    public void testInvalidCOnfigWithMissingDefaultIssuer() {
        try {
            TokenRequestDispatcherConfig
                .load("test-resources/trust/dispatcher.config.invalid.1.xml");
            fail("This should fail since there's no default isser specified");
        } catch (TrustException e) {
            assertEquals("Incorrect error", TrustException.getMessage(
                    "defaultIssuerMissing", null), e.getMessage());
        }
    }

    /**
     * Testing expected faliure when the tokenType value is missing from a 
     * tokenType definition
     */
    public void testInvalidRequestTypeDef() {
        try {
            TokenRequestDispatcherConfig
                .load("test-resources/trust/dispatcher.config.invalid.2.xml");
            fail("This should fail since there is an invalid " +
                    "requestType definition");
        } catch (TrustException e) {
            assertEquals("Incorrect error", TrustException.getMessage(
                    "invalidTokenTypeDefinition", new String[] { "Issuer",
                            TempIssuer.class.getName() }), e.getMessage());
        }
    }
}
