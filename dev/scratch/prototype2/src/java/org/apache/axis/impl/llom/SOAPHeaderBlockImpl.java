package org.apache.axis.impl.llom;

import org.apache.axis.om.*;

import javax.xml.namespace.QName;
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
public class SOAPHeaderBlockImpl extends OMElementImpl implements SOAPHeaderBlock {

    /**
     * @param localName
     * @param ns
     */
    public SOAPHeaderBlockImpl(String localName, OMNamespace ns) {
        super(localName, ns);
    }

    public SOAPHeaderBlockImpl(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
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
     * @see #getActor() getActor()
     */
    public void setActor(String actorURI) {
        setAttribute(OMConstants.ATTR_ACTOR, actorURI);
    }

    /**
     * @param attributeName
     * @param attrValue
     */
    private void setAttribute(String attributeName, String attrValue) {
        Iterator attrIter = this.getAttributeWithQName(new QName(OMConstants.SOAP_ENVELOPE_NAMESPACE_URI, attributeName));
        if (attrIter.hasNext()) {
            ((OMAttribute) attrIter.next()).setValue(attrValue);
        } else {
            OMAttribute attribute = new OMAttributeImpl(attributeName, new OMNamespaceImpl(OMConstants.SOAP_ENVELOPE_NAMESPACE_URI, OMConstants.SOAPENVELOPE_NAMESPACE_PREFIX), attrValue, this);
            this.insertAttribute(attribute);
        }
    }

    /**
     * Returns the uri of the actor associated with this <CODE>
     * SOAPHeaderBlock</CODE> object.
     *
     * @return a <CODE>String</CODE> giving the URI of the
     *         actor
     * @see #setActor(String) setActor(java.lang.String)
     */
    public String getActor() {
        return getAttribute(OMConstants.ATTR_ACTOR);
    }

    private String getAttribute(String attrName) {
        Iterator attrIter = this.getAttributeWithQName(new QName(OMConstants.SOAP_ENVELOPE_NAMESPACE_URI, attrName));
        if (attrIter.hasNext()) {
            return ((OMAttribute) attrIter.next()).getValue();
        }
        return null;
    }

    /**
     * Sets the mustUnderstand attribute for this <CODE>
     * SOAPHeaderBlock</CODE> object to be on or off.
     * <p/>
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
        setAttribute(OMConstants.ATTR_MUSTUNDERSTAND, mustUnderstand ? "true" : "false");
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
        if ((mustUnderstand = getAttribute(OMConstants.ATTR_MUSTUNDERSTAND)) != null) {
            return mustUnderstand.equalsIgnoreCase("true");
        }
        return false;
    }
}
