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
package org.apache.rampart.policy.builders;

import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.RampartConfig;

import javax.xml.namespace.QName;

public class RampartConfigBuilder implements AssertionBuilder {

    public Assertion build(OMElement element, AssertionBuilderFactory factory)
            throws IllegalArgumentException {

        RampartConfig rampartConfig = new RampartConfig();

        OMElement childElement;

        childElement = element.getFirstChildWithName(new QName(
                RampartConfig.NS, RampartConfig.USER_LN));
        if (childElement != null) {
            rampartConfig.setUser(childElement.getText().trim());
        }

        childElement = element.getFirstChildWithName(new QName(
                RampartConfig.NS, RampartConfig.ENCRYPTION_USER_LN));
        if (childElement != null) {
            rampartConfig.setEncryptionUser(childElement.getText().trim());
        }

        childElement = element.getFirstChildWithName(new QName(
                RampartConfig.NS, RampartConfig.PW_CB_CLASS_LN));
        if (childElement != null) {
            rampartConfig.setPwCbClass(childElement.getText().trim());
        }

        childElement = element.getFirstChildWithName(new QName(
                RampartConfig.NS, RampartConfig.SIG_CRYPTO_LN));
        if (childElement != null) {
            rampartConfig.setSigCryptoConfig((CryptoConfig) factory
                    .build(childElement.getFirstElement()));
        }

        return rampartConfig;
    }

    public QName[] getKnownElements() {
        return new QName[] {new QName(RampartConfig.NS, RampartConfig.RAMPART_CONFIG_LN)};
    }

}
