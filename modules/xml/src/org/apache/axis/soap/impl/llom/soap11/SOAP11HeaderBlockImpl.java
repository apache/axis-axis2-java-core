package org.apache.axis.soap.impl.llom.soap11;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.soap.impl.llom.SOAPHeaderBlockImpl;
import org.apache.axis.soap.impl.llom.SOAPConstants;
import org.apache.axis.soap.impl.llom.SOAPProcessingException;
import org.apache.axis.soap.impl.llom.soap12.SOAP12Constants;
import org.apache.axis.soap.SOAPHeader;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p/>
 *
 * Eran Chinthaka (chinthaka@apache.org)
 *
 */
public class SOAP11HeaderBlockImpl extends SOAPHeaderBlockImpl {
    /**
     * @param localName
     * @param ns
     */
    public SOAP11HeaderBlockImpl(String localName, OMNamespace ns, SOAPHeader parent) throws SOAPProcessingException {
        super(localName, ns, parent);
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
    public SOAP11HeaderBlockImpl(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        super(localName, ns, parent, builder);
    }


    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP11HeaderImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Body as the parent. But received some other implementation");
        }
    }

    public void setRole(String roleURI) {
        setAttribute(SOAP11Constants.ATTR_ACTOR, roleURI, SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

    }

    public String getRole() {
        return getAttribute(SOAP11Constants.ATTR_ACTOR, SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
    }

    public void setMustUnderstand(boolean mustUnderstand) {
        setAttribute(SOAPConstants.ATTR_MUSTUNDERSTAND, mustUnderstand ? "1" : "0", SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
    }

    public void setMustUnderstand(String mustUnderstand) throws SOAPProcessingException {
        if (SOAPConstants.ATTR_MUSTUNDERSTAND_TRUE.equals(mustUnderstand) || SOAPConstants.ATTR_MUSTUNDERSTAND_FALSE.equals(mustUnderstand) || SOAPConstants.ATTR_MUSTUNDERSTAND_0.equals(mustUnderstand) || SOAPConstants.ATTR_MUSTUNDERSTAND_1.equals(mustUnderstand)) {
            setAttribute(SOAPConstants.ATTR_MUSTUNDERSTAND, mustUnderstand, SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        }else{
            throw new SOAPProcessingException("mustUndertand should be one of \"true\", \"false\", \"0\" or \"1\" ");
        }
    }

     /**
     * Returns whether the mustUnderstand attribute for this
     * <CODE>SOAPHeaderBlock</CODE> object is turned on.
     *
     * @return <CODE>true</CODE> if the mustUnderstand attribute of
     *         this <CODE>SOAPHeaderBlock</CODE> object is turned on;
     *         <CODE>false</CODE> otherwise
     */
    public boolean getMustUnderstand() {
        String mustUnderstand = "";
        if ((mustUnderstand = getAttribute(SOAPConstants.ATTR_MUSTUNDERSTAND, SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI))
                != null) {
            return SOAPConstants.ATTR_MUSTUNDERSTAND_TRUE.equalsIgnoreCase(mustUnderstand) || SOAPConstants.ATTR_MUSTUNDERSTAND_1.equalsIgnoreCase(mustUnderstand) ;
        }
        return false;

    }
}
