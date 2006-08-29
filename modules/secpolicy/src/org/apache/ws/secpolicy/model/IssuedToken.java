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

import org.apache.axiom.om.OMElement;
import org.apache.neethi.PolicyComponent;
import org.apache.ws.secpolicy.Constants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Model bean for the IssuedToken assertion.
 */
public class IssuedToken extends Token {
    
    OMElement issuerEpr;
    OMElement rstTemplate;
    
    boolean requireExternalReference;
    boolean requireInternalReference;
    
    /**
     * @return Returns the issuerEpr.
     */
    public OMElement getIssuerEpr() {
        return issuerEpr;
    }

    /**
     * @param issuerEpr The issuerEpr to set.
     */
    public void setIssuerEpr(OMElement issuerEpr) {
        this.issuerEpr = issuerEpr;
    }

    /**
     * @return Returns the requireExternalReference.
     */
    public boolean isRequireExternalReference() {
        return requireExternalReference;
    }

    /**
     * @param requireExternalReference The requireExternalReference to set.
     */
    public void setRequireExternalReference(boolean requireExternalReference) {
        this.requireExternalReference = requireExternalReference;
    }

    /**
     * @return Returns the requireInternalReference.
     */
    public boolean isRequireInternalReference() {
        return requireInternalReference;
    }

    /**
     * @param requireInternalReference The requireInternalReference to set.
     */
    public void setRequireInternalReference(boolean requireInternalReference) {
        this.requireInternalReference = requireInternalReference;
    }

    /**
     * @return Returns the rstTemplate.
     */
    public OMElement getRstTemplate() {
        return rstTemplate;
    }

    /**
     * @param rstTemplate The rstTemplate to set.
     */
    public void setRstTemplate(OMElement rstTemplate) {
        this.rstTemplate = rstTemplate;
    }

    /* (non-Javadoc)
     * @see org.apache.neethi.Assertion#getName()
     */
    public QName getName() {
        return Constants.ISSUED_TOKEN;
    }

    /* (non-Javadoc)
     * @see org.apache.neethi.Assertion#normalize()
     */
    public PolicyComponent normalize() {
        // TODO TODO sanka
        throw new UnsupportedOperationException("TODO sanka");
    }

    /* (non-Javadoc)
     * @see org.apache.neethi.PolicyComponent#serialize(javax.xml.stream.XMLStreamWriter)
     */
    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        // TODO TODO sanka
        throw new UnsupportedOperationException("TODO sanka");
    }

}
