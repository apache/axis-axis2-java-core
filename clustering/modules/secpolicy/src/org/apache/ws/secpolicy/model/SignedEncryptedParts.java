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

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.PolicyComponent;
import org.apache.ws.secpolicy.Constants;

public class SignedEncryptedParts extends AbstractSecurityAssertion {

    private boolean body;
    
    private ArrayList headers = new ArrayList();
    
    private boolean signedParts;
    
    public SignedEncryptedParts(boolean signedParts) {
        this.signedParts = signedParts;
    }

    /**
     * @return Returns the body.
     */
    public boolean isBody() {
        return body;
    }

    /**
     * @param body The body to set.
     */
    public void setBody(boolean body) {
        this.body = body;
    }

    /**
     * @return Returns the headers.
     */
    public ArrayList getHeaders() {
        return this.headers;
    }

    /**
     * @param headers The headers to set.
     */
    public void addHeader(Header header) {
        this.headers.add(header);
    }

    /**
     * @return Returns the signedParts.
     */
    public boolean isSignedParts() {
        return signedParts;
    }

    public QName getName() {
        if (signedParts) {
            return Constants.SIGNED_PARTS;
        }
        return Constants.ENCRYPTED_PARTS;
    }

    public PolicyComponent normalize() {
        return this;
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String localName = getName().getLocalPart();
        String namespaceURI = getName().getNamespaceURI();

        String prefix = writer.getPrefix(namespaceURI);

        if (prefix == null) {
            prefix = getName().getPrefix();
            writer.setPrefix(prefix, namespaceURI);
        }
            
        // <sp:SignedParts> | <sp:EncryptedParts> 
        writer.writeStartElement(prefix, localName, namespaceURI);
        
        // xmlns:sp=".."
        writer.writeNamespace(prefix, namespaceURI);
        
        if (isBody()) {
            // <sp:Body />
            // FIXME : move 'Body' to Constants
            writer.writeStartElement(prefix, "Body", namespaceURI);
            writer.writeEndElement();
        }
        
        Header header;        
        for (Iterator iterator = headers.iterator(); iterator.hasNext();) {
            header = (Header) iterator.next();
            // <sp:Header Name=".." Namespace=".." />
            // FIXME move 'Header' to Constants
            writer.writeStartElement(prefix, "Header", namespaceURI);
            
            writer.writeAttribute("Name", header.getName());
            writer.writeAttribute("Namespace", header.getNamespace());
            
            writer.writeEndElement();
        }
        
        // </sp:SignedParts> | </sp:EncryptedParts>
        writer.writeEndElement();
    }    
    
    
}
