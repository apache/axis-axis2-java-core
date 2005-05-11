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
package org.apache.axis.om.impl.llom.soap;

import javax.xml.namespace.QName;

import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.SOAPHeaderBlock;
import org.apache.axis.om.impl.llom.soap.soap11.SOAP11Constants;
import org.apache.axis.om.impl.llom.soap.soap11.SOAP11Constants;
import org.apache.axis.om.impl.llom.soap.SOAPConstants;
import org.apache.axis.om.impl.llom.OMElementImpl;
import org.apache.axis.om.impl.llom.OMAttributeImpl;
import org.apache.axis.om.impl.llom.OMNamespaceImpl;

/**
 * Class SOAPHeaderBlockImpl
 */
public class SOAPHeaderBlockImpl extends OMElementImpl
        implements SOAPHeaderBlock {

    private boolean processed = false;
    /**
     * @param localName
     * @param ns
     */
    public SOAPHeaderBlockImpl(String localName, OMNamespace ns) {
        super(localName, ns);
    }

    /**
     * Constructor SOAPHeaderBlockImpl
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     */
    public SOAPHeaderBlockImpl(String localName, OMNamespace ns,
                               OMElement parent, OMXMLParserWrapper builder) {
        super(localName, ns, parent, builder);
    }

    /**
     * Sets the actor associated with this <CODE>
     * SOAPHeaderBlock</CODE> object to the specified actor. The
     * default value of an actor is: <CODE>
     * OMConstants.URI_SOAP_ACTOR_NEXT</CODE>
     *
     * @param actorURI a <CODE>String</CODE> giving
     *                 the URI of the actor to set
     * @throws IllegalArgumentException if
     *                                  there is a problem in setting the actor.
     * @see #getRole() getRole()
     */
    public void setRole(String actorURI) {
        setAttribute(SOAPConstants.ATTR_ACTOR, actorURI);
    }

    /**
     * @param attributeName
     * @param attrValue
     */
    private void setAttribute(String attributeName, String attrValue) {
        OMAttribute omAttribute = this.getFirstAttribute(
                new QName(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI, attributeName));
        if (omAttribute != null) {
            omAttribute.setValue(attrValue);
        } else {
            OMAttribute attribute = new OMAttributeImpl(
                    attributeName,
                    new OMNamespaceImpl(
                            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                            SOAPConstants.SOAP_DEFAULT_NAMESPACE_PREFIX), attrValue);
            this.addAttribute(attribute);
        }
    }

    /**
     * Returns the uri of the actor associated with this <CODE>
     * SOAPHeaderBlock</CODE> object.
     *
     * @return a <CODE>String</CODE> giving the URI of the
     *         actor
     * @see #setRole(String) setRole(java.lang.String)
     */
    public String getRole() {
        return getAttribute(SOAPConstants.ATTR_ACTOR);
    }

    /**
     * Method getAttribute
     *
     * @param attrName
     * @return
     */
    private String getAttribute(String attrName) {
        OMAttribute omAttribute = this.getFirstAttribute(
                new QName(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI, attrName));
        return (omAttribute != null)
                ? omAttribute.getValue()
                : null;
    }

    /**
     * Sets the mustUnderstand attribute for this <CODE>
     * SOAPHeaderBlock</CODE> object to be on or off.
     * <P>If the mustUnderstand attribute is on, the actor who
     * receives the <CODE>SOAPHeaderBlock</CODE> must process it
     * correctly. This ensures, for example, that if the <CODE>
     * SOAPHeaderBlock</CODE> object modifies the message, that
     * the message is being modified correctly.</P>
     *
     * @param mustUnderstand <CODE>true</CODE> to
     *                       set the mustUnderstand attribute on; <CODE>false</CODE>
     *                       to turn if off
     * @throws IllegalArgumentException if
     *                                  there is a problem in setting the actor.
     * @see #getMustUnderstand() getMustUnderstand()
     */
    public void setMustUnderstand(boolean mustUnderstand) {
        setAttribute(SOAPConstants.ATTR_MUSTUNDERSTAND, mustUnderstand
                        ? "true"
                        : "false");
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
        if ((mustUnderstand = getAttribute(SOAPConstants.ATTR_MUSTUNDERSTAND))
                != null) {
            return mustUnderstand.equalsIgnoreCase("true");
        }
        return false;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed() {
        processed = true;
    }
}
