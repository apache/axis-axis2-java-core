package org.apache.axis.om.impl;

import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.soap.SOAPHeaderElement;
import org.apache.axis.om.impl.util.SOAPConstants;
import org.apache.xml.utils.QName;

import java.util.Iterator;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Nov 2, 2004
 * Time: 3:19:20 PM
 */
public class SOAPHeaderElementImpl extends OMElementImpl implements SOAPHeaderElement {

    /**
     * @param localName
     * @param ns
     */
    public SOAPHeaderElementImpl(String localName, OMNamespace ns) {
        super(localName, ns);
    }

    public SOAPHeaderElementImpl(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        super(localName, ns, parent, builder);
    }

    /**
     * Sets the actor associated with this <CODE>
     * SOAPHeaderElement</CODE> object to the specified actor. The
     * default value of an actor is: <CODE>
     * SOAPConstants.URI_SOAP_ACTOR_NEXT</CODE>
     *
     * @param actorURI a <CODE>String</CODE> giving
     *                 the URI of the actor to set
     * @throws IllegalArgumentException if
     *                                  there is a problem in setting the actor.
     * @see #getActor() getActor()
     */
    public void setActor(String actorURI) {
        setAttribute(SOAPConstants.attrActor, actorURI);
    }

    /**
     *
     * @param attributeName
     * @param attrValue
     */
    private void setAttribute(String attributeName, String attrValue) {
        Iterator attrIter = this.getAttributeWithQName(new QName(SOAPConstants.soapEnvelopeNamespaceURI, attributeName));
        if (attrIter.hasNext()) {
            ((OMAttribute) attrIter.next()).setValue(attrValue);
        } else {
            OMAttribute attribute = new OMAttributeImpl(attributeName, new OMNamespaceImpl(SOAPConstants.soapEnvelopeNamespaceURI, SOAPConstants.soapEnvelopeNamespacePrefix), attrValue, this);
            this.insertAttribute(attribute);
        }
    }

    /**
     * Returns the uri of the actor associated with this <CODE>
     * SOAPHeaderElement</CODE> object.
     *
     * @return a <CODE>String</CODE> giving the URI of the
     *         actor
     * @see #setActor(String) setActor(java.lang.String)
     */
    public String getActor() {
        return getAttribute(SOAPConstants.attrActor);
    }

    private String getAttribute(String attrName) {
        Iterator attrIter = this.getAttributeWithQName(new QName(SOAPConstants.soapEnvelopeNamespaceURI, attrName));
        if (attrIter.hasNext()) {
            return ((OMAttribute) attrIter.next()).getValue();
        }
        return null;
    }

    /**
     * Sets the mustUnderstand attribute for this <CODE>
     * SOAPHeaderElement</CODE> object to be on or off.
     * <p/>
     * <P>If the mustUnderstand attribute is on, the actor who
     * receives the <CODE>SOAPHeaderElement</CODE> must process it
     * correctly. This ensures, for example, that if the <CODE>
     * SOAPHeaderElement</CODE> object modifies the message, that
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
        setAttribute(SOAPConstants.attrMustUnderstand, mustUnderstand ? "true" : "false");
    }

    /**
     * Returns whether the mustUnderstand attribute for this
     * <CODE>SOAPHeaderElement</CODE> object is turned on.
     *
     * @return <CODE>true</CODE> if the mustUnderstand attribute of
     *         this <CODE>SOAPHeaderElement</CODE> object is turned on;
     *         <CODE>false</CODE> otherwise
     */
    public boolean getMustUnderstand() {
        String mustUnderstand = "";
        if( (mustUnderstand = getAttribute(SOAPConstants.attrMustUnderstand)) != null){
            return mustUnderstand.equalsIgnoreCase("true");
        }
        return false;
    }
}
