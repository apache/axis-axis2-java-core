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

public class SignedEncryptedElements extends AbstractSecurityAssertion {

    private ArrayList xPathExpressions = new ArrayList();

    private String xPathVersion;

    /**
     * Just a flag to identify whether this holds sign element info or encr
     * elements info
     */
    private boolean signedElemets;

    public SignedEncryptedElements(boolean signedElements) {
        this.signedElemets = signedElements;
    }

    /**
     * @return Returns the xPathExpressions.
     */
    public ArrayList getXPathExpressions() {
        return xPathExpressions;
    }

    public void addXPathExpression(String expr) {
        this.xPathExpressions.add(expr);
    }

    /**
     * @return Returns the xPathVersion.
     */
    public String getXPathVersion() {
        return xPathVersion;
    }

    /**
     * @param pathVersion
     *            The xPathVersion to set.
     */
    public void setXPathVersion(String pathVersion) {
        xPathVersion = pathVersion;
    }

    /**
     * @return Returns the signedElemets.
     */
    public boolean isSignedElemets() {
        return signedElemets;
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {

        String localName = getName().getLocalPart();
        String namespaceURI = getName().getNamespaceURI();

        String prefix;
        String writerPrefix = writer.getPrefix(namespaceURI);

        if (writerPrefix == null) {
            prefix = getName().getPrefix();
            writer.setPrefix(prefix, namespaceURI);
        } else {
            prefix = writerPrefix;
        }

        // <sp:SignedElements> | <sp:EncryptedElements>
        writer.writeStartElement(prefix, localName, namespaceURI);

        if (writerPrefix == null) {
            // xmlns:sp=".."
            writer.writeNamespace(prefix, namespaceURI);
        }

        if (xPathVersion != null) {
            writer.writeAttribute(prefix, namespaceURI,
                    Constants.ATTR_XPATH_VERSION.getLocalPart(), xPathVersion);
        }

        String xpathExpression;

        for (Iterator iterator = xPathExpressions.iterator(); iterator
                .hasNext();) {
            xpathExpression = (String) iterator.next();
            // <sp:XPath ..>
            writer.writeStartElement(prefix, Constants.XPATH_.getLocalPart(),
                    namespaceURI);
            writer.writeCharacters(xpathExpression);
            writer.writeEndElement();
        }

        // </sp:SignedElements> | </sp:EncryptedElements>
        writer.writeEndElement();
    }

    public QName getName() {
        if (signedElemets) {
            return Constants.SIGNED_ELEMENTS;
        }

        return Constants.ENCRYPTED_ELEMENTS;
    }

    public PolicyComponent normalize() {
        return this;
    }
}
