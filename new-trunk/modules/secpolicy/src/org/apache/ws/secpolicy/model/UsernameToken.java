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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.PolicyComponent;
import org.apache.ws.secpolicy.Constants;

public class UsernameToken extends Token {

    private boolean useUTProfile10 = false;

    private boolean useUTProfile11 = false;

    /**
     * @return Returns the useUTProfile11.
     */
    public boolean isUseUTProfile11() {
        return useUTProfile11;
    }

    /**
     * @param useUTProfile11
     *            The useUTProfile11 to set.
     */
    public void setUseUTProfile11(boolean useUTProfile11) {
        this.useUTProfile11 = useUTProfile11;
    }

    public boolean isUseUTProfile10() {
        return useUTProfile10;
    }

    public void setUseUTProfile10(boolean useUTProfile10) {
        this.useUTProfile10 = useUTProfile10;
    }

    public QName getName() {
        return Constants.USERNAME_TOKEN;
    }

    public PolicyComponent normalize() {
        throw new UnsupportedOperationException();
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String localname = Constants.USERNAME_TOKEN.getLocalPart();
        String namespaceURI = Constants.USERNAME_TOKEN.getNamespaceURI();

        String prefix = writer.getPrefix(namespaceURI);
        if (prefix == null) {
            prefix = Constants.USERNAME_TOKEN.getPrefix();
            writer.setPrefix(prefix, namespaceURI);
        }

        // <sp:UsernameToken
        writer.writeStartElement(prefix, localname, namespaceURI);

        writer.writeNamespace(prefix, namespaceURI);

        String inclusion = getInclusion();
        if (inclusion != null) {
            writer.writeAttribute(prefix, namespaceURI, Constants.INCLUDE_TOKEN
                    .getLocalPart(), inclusion);
        }

        if (isUseUTProfile10() || isUseUTProfile11()) {
            String pPrefix = writer.getPrefix(Constants.POLICY
                    .getNamespaceURI());
            if (pPrefix == null) {
                writer.setPrefix(Constants.POLICY.getPrefix(), Constants.POLICY
                        .getNamespaceURI());
            }

            // <wsp:Policy>
            writer.writeStartElement(prefix, Constants.POLICY.getLocalPart(),
                    Constants.POLICY.getNamespaceURI());

            // CHECKME
            if (isUseUTProfile10()) {
                // <sp:WssUsernameToken10 />
                writer.writeStartElement(prefix, Constants.WSS_USERNAME_TOKEN10
                        .getLocalPart(), namespaceURI);
            } else {
                // <sp:WssUsernameToken11 />
                writer.writeStartElement(prefix, Constants.WSS_USERNAME_TOKEN11
                        .getLocalPart(), namespaceURI);
            }
            writer.writeEndElement();

            // </wsp:Policy>
            writer.writeEndElement();

        }

        writer.writeEndElement();
        // </sp:UsernameToken>

    }
}
