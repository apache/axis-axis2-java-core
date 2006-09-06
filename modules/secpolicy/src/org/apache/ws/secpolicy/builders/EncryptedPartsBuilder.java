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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.ws.secpolicy.Constants;
import org.apache.ws.secpolicy.model.Header;
import org.apache.ws.secpolicy.model.SignedEncryptedParts;

public class EncryptedPartsBuilder implements AssertionBuilder {

    public static final QName HEADER = new QName(Constants.SP_NS, "Header");
    public static final QName NAME = new QName(Constants.SP_NS, "Name");
    public static final QName NAMESPACE = new QName(Constants.SP_NS, "Namespace");
    public static final QName BODY = new QName(Constants.SP_NS, "Body");
        
    public Assertion build(OMElement element, AssertionBuilderFactory factory) throws IllegalArgumentException {
        
        SignedEncryptedParts signedEncryptedParts = new SignedEncryptedParts(false);
        
        for (Iterator iterator = element.getChildElements(); iterator.hasNext();) {
            processElement((OMElement) iterator.next(), signedEncryptedParts);
        }
        
        return signedEncryptedParts;
    }
    
    public QName[] getKnownElements() {
        return new QName[] {Constants.ENCRYPTED_PARTS};
    }

    private void processElement(OMElement element, SignedEncryptedParts parent) {
        
        QName name = element.getQName();
        
        if (HEADER.equals(name)) {
            Header header = new Header();
            
            OMAttribute nameAttribute = element.getAttribute(NAME);
            header.setName(nameAttribute.getAttributeValue());
            
            OMAttribute namespaceAttribute = element.getAttribute(NAMESPACE);
            header.setNamespace(namespaceAttribute.getAttributeValue());
            
        } else if (BODY.equals(name)) {
            parent.setBody(true);            
        }        
    }
}
