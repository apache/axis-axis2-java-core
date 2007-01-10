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

package org.apache.ws.secpolicy.model;

import org.apache.neethi.PolicyComponent;
import org.apache.ws.secpolicy.Constants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Model class of SecurityContextToken assertion
 */
public class SecurityContextToken extends Token {

    boolean requireExternalUriRef;
    
    boolean sc10SecurityContextToken;
    
    /**
     * @return Returns the requireExternalUriRef.
     */
    public boolean isRequireExternalUriRef() {
        return requireExternalUriRef;
    }

    /**
     * @param requireExternalUriRef The requireExternalUriRef to set.
     */
    public void setRequireExternalUriRef(boolean requireExternalUriRef) {
        this.requireExternalUriRef = requireExternalUriRef;
    }

    /**
     * @return Returns the sc10SecurityContextToken.
     */
    public boolean isSc10SecurityContextToken() {
        return sc10SecurityContextToken;
    }

    /**
     * @param sc10SecurityContextToken The sc10SecurityContextToken to set.
     */
    public void setSc10SecurityContextToken(boolean sc10SecurityContextToken) {
        this.sc10SecurityContextToken = sc10SecurityContextToken;
    }

    /* (non-Javadoc)
     * @see org.apache.neethi.Assertion#getName()
     */
    public QName getName() {
        return Constants.SECURITY_CONTEXT_TOKEN;
    }

    /* (non-Javadoc)
     * @see org.apache.neethi.Assertion#normalize()
     */
    public PolicyComponent normalize() {
        // TODO TODO Sanka
        throw new UnsupportedOperationException("TODO Sanka");
    }

    /* (non-Javadoc)
     * @see org.apache.neethi.PolicyComponent#serialize(javax.xml.stream.XMLStreamWriter)
     */
    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        // TODO TODO Sanka
        throw new UnsupportedOperationException("TODO Sanka");
    }

}
