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

package org.apache.ws.security.policy.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.PolicyComponent;
import org.apache.ws.security.policy.Constants;

public class X509Token extends Token {

    private boolean requireKeyIdentifierReference;
    
    private boolean requireIssuerSerialReference;
    
    private boolean requireEmbeddedTokenReference;
    
    private boolean requireThumbprintReference;
    
    private String tokenVersionAndType;
    
    private List x509Tokens = new ArrayList();
    
    /**
     * @return Returns the requireEmbeddedTokenReference.
     */
    public boolean isRequireEmbeddedTokenReference() {
        return requireEmbeddedTokenReference;
    }

    /**
     * @param requireEmbeddedTokenReference The requireEmbeddedTokenReference to set.
     */
    public void setRequireEmbeddedTokenReference(
            boolean requireEmbeddedTokenReference) {
        this.requireEmbeddedTokenReference = requireEmbeddedTokenReference;
    }

    /**
     * @return Returns the requireIssuerSerialReference.
     */
    public boolean isRequireIssuerSerialReference() {
        return requireIssuerSerialReference;
    }

    /**
     * @param requireIssuerSerialReference The requireIssuerSerialReference to set.
     */
    public void setRequireIssuerSerialReference(boolean requireIssuerSerialReference) {
        this.requireIssuerSerialReference = requireIssuerSerialReference;
    }

    /**
     * @return Returns the requireKeyIdentifierReference.
     */
    public boolean isRequireKeyIdentifierReference() {
        return requireKeyIdentifierReference;
    }

    /**
     * @param requireKeyIdentifierReference The requireKeyIdentifierReference to set.
     */
    public void setRequireKeyIdentifierReference(
            boolean requireKeyIdentifierReference) {
        this.requireKeyIdentifierReference = requireKeyIdentifierReference;
    }

    /**
     * @return Returns the requireThumbprintReference.
     */
    public boolean isRequireThumbprintReference() {
        return requireThumbprintReference;
    }

    /**
     * @param requireThumbprintReference The requireThumbprintReference to set.
     */
    public void setRequireThumbprintReference(boolean requireThumbprintReference) {
        this.requireThumbprintReference = requireThumbprintReference;
    }

    /**
     * @return Returns the tokenVersionAndType.
     */
    public String getTokenVersionAndType() {
        return tokenVersionAndType;
    }

    /**
     * @param tokenVersionAndType The tokenVersionAndType to set.
     */
    public void setTokenVersionAndType(String tokenVersionAndType) {
        this.tokenVersionAndType = tokenVersionAndType;
    }
    
    public Iterator getOptions() {
        return x509Tokens.iterator();
    }
    
    public void addOption(X509Token token) {
        x509Tokens.add(token);
    }

    public QName getName() {
        return Constants.X509_TOKEN;
    }

    public PolicyComponent normalize() {
        throw new UnsupportedOperationException();
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    
    
}
