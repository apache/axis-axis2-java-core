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
 * Model bean to capture Trust10 assertion info
 */
public class Trust10 extends AbstractSecurityAssertion {

    private boolean mustSupportClientChallenge;
    private boolean mustSupportServerChallenge;
    private boolean requireClientEntropy;
    private boolean requireServerEntropy;
    private boolean mustSupportIssuedTokens;
    
    /**
     * @return Returns the mustSupportClientChallenge.
     */
    public boolean isMustSupportClientChallenge() {
        return mustSupportClientChallenge;
    }

    /**
     * @param mustSupportClientChallenge The mustSupportClientChallenge to set.
     */
    public void setMustSupportClientChallenge(boolean mustSupportClientChallenge) {
        this.mustSupportClientChallenge = mustSupportClientChallenge;
    }

    /**
     * @return Returns the mustSupportIssuedTokens.
     */
    public boolean isMustSupportIssuedTokens() {
        return mustSupportIssuedTokens;
    }

    /**
     * @param mustSupportIssuedTokens The mustSupportIssuedTokens to set.
     */
    public void setMustSupportIssuedTokens(boolean mustSupportIssuedTokens) {
        this.mustSupportIssuedTokens = mustSupportIssuedTokens;
    }

    /**
     * @return Returns the mustSupportServerChallenge.
     */
    public boolean isMustSupportServerChallenge() {
        return mustSupportServerChallenge;
    }

    /**
     * @param mustSupportServerChallenge The mustSupportServerChallenge to set.
     */
    public void setMustSupportServerChallenge(boolean mustSupportServerChallenge) {
        this.mustSupportServerChallenge = mustSupportServerChallenge;
    }

    /**
     * @return Returns the requireClientEntropy.
     */
    public boolean isRequireClientEntropy() {
        return requireClientEntropy;
    }

    /**
     * @param requireClientEntropy The requireClientEntropy to set.
     */
    public void setRequireClientEntropy(boolean requireClientEntropy) {
        this.requireClientEntropy = requireClientEntropy;
    }

    /**
     * @return Returns the requireServerEntropy.
     */
    public boolean isRequireServerEntropy() {
        return requireServerEntropy;
    }

    /**
     * @param requireServerEntropy The requireServerEntropy to set.
     */
    public void setRequireServerEntropy(boolean requireServerEntropy) {
        this.requireServerEntropy = requireServerEntropy;
    }

    /* (non-Javadoc)
     * @see org.apache.neethi.Assertion#getName()
     */
    public QName getName() {
        return Constants.TRUST_10;
    }

    /* (non-Javadoc)
     * @see org.apache.neethi.Assertion#isOptional()
     */
    public boolean isOptional() {
        // TODO TODO Sanka
        throw new UnsupportedOperationException("TODO Sanka");
    }

    public PolicyComponent normalize() {
        return this;
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        
        String localname = Constants.TRUST_10.getLocalPart();
        String namespaceURI = Constants.TRUST_10.getNamespaceURI();
        
        String prefix = writer.getPrefix(namespaceURI);
        if (prefix == null) {
            prefix = Constants.TRUST_10.getPrefix();
            writer.setPrefix(prefix, namespaceURI);
        }
        
        // <sp:Trust10>
        writer.writeStartElement(prefix, localname, namespaceURI);
        // xmlns:sp=".."
        writer.writeNamespace(prefix, namespaceURI);
        
        String wspPrefix = writer.getPrefix(Constants.POLICY.getNamespaceURI());
        if (wspPrefix == null) {
            writer.setPrefix(wspPrefix, Constants.POLICY.getNamespaceURI());
        }
        
        // <wsp:Policy>
        writer.writeStartElement(Constants.POLICY.getPrefix(), Constants.POLICY.getLocalPart(), Constants.POLICY.getNamespaceURI());
        
        if (isMustSupportClientChallenge()) {
            // <sp:MustSupportClientChallenge />
            writer.writeStartElement(prefix, Constants.MUST_SUPPORT_CLIENT_CHALLENGE.getLocalPart(), namespaceURI);
            writer.writeEndElement();
        }
        
        if (isMustSupportServerChallenge()) {
            // <sp:MustSupportServerChallenge />
            writer.writeStartElement(prefix, Constants.MUST_SUPPORT_SERVER_CHALLENGE.getLocalPart(), namespaceURI);
            writer.writeEndElement();
        }
        
        if (isRequireClientEntropy()) {
            // <sp:RequireClientEntropy />
            writer.writeStartElement(prefix, Constants.REQUIRE_CLIENT_ENTROPY.getLocalPart(), namespaceURI);
            writer.writeEndElement();
        }
        
        
        if (isRequireServerEntropy()) {
            // <sp:RequireServerEntropy />
            writer.writeStartElement(prefix, Constants.REQUIRE_SERVER_ENTROPY.getLocalPart(), namespaceURI);
            writer.writeEndElement();
        }
        
        if (isMustSupportIssuedTokens()) {
            // <sp:MustSupportIssuedTokens />
            writer.writeStartElement(prefix, Constants.MUST_SUPPORT_ISSUED_TOKENS.getLocalPart(), namespaceURI);
            writer.writeEndElement();
        }
        
        // </wsp:Policy>
        writer.writeEndElement();
        
        
        // </sp:Trust10>
        writer.writeEndElement();
        
        
        
        
    }

    public short getType() {
        return org.apache.neethi.Constants.TYPE_ASSERTION;
    }

}
