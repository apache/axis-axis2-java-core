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

    private OMElement issuerEpr;

    private OMElement rstTemplate;

    boolean requireExternalReference;

    boolean requireInternalReference;

    /**
     * @return Returns the issuerEpr.
     */
    public OMElement getIssuerEpr() {
        return issuerEpr;
    }

    /**
     * @param issuerEpr
     *            The issuerEpr to set.
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
     * @param requireExternalReference
     *            The requireExternalReference to set.
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
     * @param requireInternalReference
     *            The requireInternalReference to set.
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
     * @param rstTemplate
     *            The rstTemplate to set.
     */
    public void setRstTemplate(OMElement rstTemplate) {
        this.rstTemplate = rstTemplate;
    }

    public QName getName() {
        return Constants.ISSUED_TOKEN;
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String localname = Constants.ISSUED_TOKEN.getLocalPart();
        String namespaceURI = Constants.ISSUED_TOKEN.getNamespaceURI();

        String prefix;
        String writerPrefix = writer.getPrefix(namespaceURI);

        if (writerPrefix == null) {
            prefix = Constants.ISSUED_TOKEN.getPrefix();
            writer.setPrefix(prefix, namespaceURI);

        } else {
            prefix = writerPrefix;
        }

        // <sp:IssuedToken>
        writer.writeStartElement(prefix, localname, namespaceURI);

        if (writerPrefix == null) {
            writer.writeNamespace(prefix, namespaceURI);
        }

        String inclusion = getInclusion();
        if (inclusion != null) {
            writer.writeAttribute(prefix, namespaceURI,
                    Constants.ATTR_INCLUDE_TOKEN, inclusion);
        }

        if (issuerEpr != null) {
            writer.writeStartElement(prefix, Constants.ISSUER.getLocalPart(),
                    namespaceURI);
            issuerEpr.serialize(writer);
            writer.writeEndElement();
        }

        if (rstTemplate != null) {
            // <sp:RequestSecurityTokenTemplate>
            writer.writeStartElement(prefix,
                    Constants.REQUEST_SECURITY_TOKEN_TEMPLATE.getLocalPart(),
                    namespaceURI);

            rstTemplate.serialize(writer);

            // </sp:RequestSecurityTokenTemplate>
            writer.writeEndElement();
        }

        String policyLocalName = Constants.PROTECTION_TOKEN.getLocalPart();
        String policyNamespaceURI = Constants.PROTECTION_TOKEN
                .getNamespaceURI();

        String wspPrefix;

        String wspWriterPrefix = writer.getPrefix(policyNamespaceURI);

        if (wspWriterPrefix == null) {
            wspPrefix = Constants.PROTECTION_TOKEN.getPrefix();
            writer.setPrefix(wspPrefix, policyNamespaceURI);
        } else {
            wspPrefix = wspWriterPrefix;
        }

        if (isRequireExternalReference() || isRequireInternalReference()) {

            // <wsp:Policy>
            writer.writeStartElement(wspPrefix, policyLocalName,
                    policyNamespaceURI);

            if (wspWriterPrefix == null) {
                // xmlns:wsp=".."
                writer.writeNamespace(wspPrefix, policyNamespaceURI);
            }

            if (isRequireExternalReference()) {
                // <sp:RequireExternalReference />
                writer.writeEmptyElement(prefix, Constants.REQUIRE_EXTERNAL_REFERNCE.getLocalPart(), namespaceURI);
            }
            
            if (isRequireInternalReference()) {
                // <sp:RequireInternalReference />
                writer.writeEmptyElement(prefix, Constants.REQUIRE_INTERNAL_REFERNCE.getLocalPart(), namespaceURI);
            }
            
            // <wsp:Policy>
            writer.writeEndElement();
        }

        // </sp:IssuedToken>
        writer.writeEndElement();
    }

}
