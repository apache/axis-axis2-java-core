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

package org.apache.axis2.soap.impl.dom.soap12;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axis2.soap.impl.dom.SOAPHeaderBlockImpl;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.apache.ws.commons.soap.SOAPConstants;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.SOAPHeader;
import org.apache.ws.commons.soap.SOAPProcessingException;

public class SOAP12HeaderBlockImpl extends SOAPHeaderBlockImpl {
    /**
     * @param localName
     * @param ns
     */
    public SOAP12HeaderBlockImpl(String localName,
                                 OMNamespace ns,
                                 SOAPHeader parent,
                                 SOAPFactory factory) throws SOAPProcessingException {
        super(localName, ns, parent, factory);
        checkParent(parent);
    }

    /**
     * Constructor SOAPHeaderBlockImpl
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     */
    public SOAP12HeaderBlockImpl(String localName,
                                 OMNamespace ns,
                                 SOAPHeader parent,
                                 OMXMLParserWrapper builder,
                                 SOAPFactory factory) {
        super(localName, ns, parent, builder, factory);

    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP12HeaderImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP Body as the parent. But received some other implementation");
        }
    }

    public void setRole(String roleURI) {
        setAttribute(SOAP12Constants.SOAP_ROLE,
                roleURI,
                SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
    }

    public String getRole() {
        return getAttribute(SOAP12Constants.SOAP_ROLE,
                SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

    }

    public void setMustUnderstand(boolean mustUnderstand) {
        setAttribute(SOAPConstants.ATTR_MUSTUNDERSTAND,
                mustUnderstand ? "1" : "0",
                SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

    }

    public void setMustUnderstand(String mustUnderstand) throws SOAPProcessingException {
        if (SOAPConstants.ATTR_MUSTUNDERSTAND_TRUE.equals(mustUnderstand) ||
                SOAPConstants.ATTR_MUSTUNDERSTAND_FALSE.equals(mustUnderstand) ||
                SOAPConstants.ATTR_MUSTUNDERSTAND_0.equals(mustUnderstand) ||
                SOAPConstants.ATTR_MUSTUNDERSTAND_1.equals(mustUnderstand)) {
            setAttribute(SOAPConstants.ATTR_MUSTUNDERSTAND,
                    mustUnderstand,
                    SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        } else {
            throw new SOAPProcessingException(
                    "mustUndertand should be one of \"true\", \"false\", \"0\" or \"1\" ");
        }
    }

    public boolean getMustUnderstand() throws SOAPProcessingException {
        String mustUnderstand = "";
        if ((mustUnderstand =
                getAttribute(SOAPConstants.ATTR_MUSTUNDERSTAND,
                        SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI))
                != null) {
            if (SOAPConstants.ATTR_MUSTUNDERSTAND_TRUE.equalsIgnoreCase(
                    mustUnderstand) ||
                    SOAPConstants.ATTR_MUSTUNDERSTAND_1.equalsIgnoreCase(
                            mustUnderstand)) {
                return true;
            } else if (SOAPConstants.ATTR_MUSTUNDERSTAND_FALSE.equalsIgnoreCase(
                    mustUnderstand) ||
                    SOAPConstants.ATTR_MUSTUNDERSTAND_0.equalsIgnoreCase(
                            mustUnderstand)) {
                return false;
            } else {
                throw new SOAPProcessingException(
                        "Invalid value found in mustUnderstand value of " +
                        this.getLocalName() +
                        " header block");
            }
        }
        return false;

    }
}
