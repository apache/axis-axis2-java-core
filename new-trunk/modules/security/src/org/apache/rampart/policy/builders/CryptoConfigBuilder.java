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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.RampartConfig;

import javax.xml.namespace.QName;

import java.util.Iterator;
import java.util.Properties;

public class CryptoConfigBuilder implements AssertionBuilder {

    public Assertion build(OMElement element, AssertionBuilderFactory factory)
            throws IllegalArgumentException {
        
        CryptoConfig cryptoCofig = new CryptoConfig();
        
        OMAttribute attribute = element.getAttribute(new QName(CryptoConfig.PROVIDER_ATTR));
        cryptoCofig.setProvider(attribute.getAttributeValue().trim());
        
        Properties properties = new Properties();

        OMElement childElement;
        OMAttribute name;
        String value;

        for (Iterator iterator = element.getChildElements(); iterator.hasNext();) {
            /*
             * In this senario we could have used
             * element.getChildrenWithQName(USER); Unfortunately we can't do
             * that due to a bug in this method. TODO Need to get it fixed
             */

            childElement = (OMElement) iterator.next();

            QName prop = new QName(RampartConfig.NS, CryptoConfig.PROPERTY_LN);
            
            if (prop.equals(childElement.getQName())) {
                name = childElement.getAttribute(new QName(CryptoConfig.PROPERTY_NAME_ATTR));
                value = childElement.getText();

                properties.put(name.getAttributeValue(), value.trim());
            }

        }

        cryptoCofig.setProp(properties);
        return cryptoCofig;
    }

    public QName[] getKnownElements() {
        return new QName[] {new QName(RampartConfig.NS, CryptoConfig.CRYPTO_LN)};
    }

}
