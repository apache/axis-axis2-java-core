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

/**
 * 
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class HttpsToken extends Token {

    private boolean requireClientCertificate = false;

    public boolean isRequireClientCertificate() {
        return requireClientCertificate;
    }

    public void setRequireClientCertificate(boolean requireClientCertificate) {
        this.requireClientCertificate = requireClientCertificate;
    }

    public QName getName() {
        return Constants.HTTPS_TOKEN;
    }

    public PolicyComponent normalize() {
        throw new UnsupportedOperationException();
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {

        String localname = Constants.HTTPS_TOKEN.getLocalPart();
        String namespaceURI = Constants.HTTPS_TOKEN.getNamespaceURI();

        String prefix = writer.getPrefix(namespaceURI);
        if (prefix == null) {
            prefix = Constants.HTTPS_TOKEN.getPrefix();
            writer.setPrefix(prefix, namespaceURI);
        }

        // <sp:HttpsToken
        writer.writeStartElement(prefix, localname, namespaceURI);

        // RequireClientCertificate=".."
        writer
                .writeAttribute(Constants.REQUIRE_CLIENT_CERTIFICATE
                        .getLocalPart(), Boolean
                        .toString(isRequireClientCertificate()));

        writer.writeEndElement();
        // </sp:HttpsToken>
    }
}
