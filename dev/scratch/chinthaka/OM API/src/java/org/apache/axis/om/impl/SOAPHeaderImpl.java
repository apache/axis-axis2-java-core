package org.apache.axis.om.impl;

import org.apache.axis.om.*;
import org.apache.axis.om.soap.SOAPEnvelope;
import org.apache.axis.om.soap.SOAPHeader;
import org.apache.axis.om.soap.SOAPHeaderElement;
import org.apache.axis.om.impl.util.SOAPConstants;
import org.apache.xml.utils.QName;

import java.util.Iterator;
import java.util.HashMap;

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
 * Time: 2:45:24 PM
 */
public class SOAPHeaderImpl extends OMElementImpl implements SOAPHeader {


    /**
     * @param envelope
     */
    public SOAPHeaderImpl(SOAPEnvelope envelope) {
        super(envelope);
    }

    public SOAPHeaderImpl(String localName, OMNamespace ns, OMElement parent, OMXmlPullParserWrapper builder) {
        super(localName, ns, parent, builder);
    }

    /**
     * Creates a new <CODE>SOAPHeaderElement</CODE> object
     * initialized with the specified name and adds it to this
     * <CODE>SOAPHeader</CODE> object.
     *
     * @return the new <CODE>SOAPHeaderElement</CODE> object that
     *         was inserted into this <CODE>SOAPHeader</CODE>
     *         object
     * @throws org.apache.axis.om.OMException if a SOAP error occurs
     */
    public SOAPHeaderElement addHeaderElement(String localName, OMNamespace ns) throws OMException {
        SOAPHeaderElement soapHeaderElement = new SOAPHeaderElementImpl(localName, ns);
        this.addChild(soapHeaderElement);
        return soapHeaderElement;
    }

    /**
     * Returns a list of all the <CODE>SOAPHeaderElement</CODE>
     * objects in this <CODE>SOAPHeader</CODE> object that have the
     * the specified actor. An actor is a global attribute that
     * indicates the intermediate parties to whom the message should
     * be sent. An actor receives the message and then sends it to
     * the next actor. The default actor is the ultimate intended
     * recipient for the message, so if no actor attribute is
     * included in a <CODE>SOAPHeader</CODE> object, the message is
     * sent to its ultimate destination.
     *
     * @param actor a <CODE>String</CODE> giving the
     *              URI of the actor for which to search
     * @return an <CODE>Iterator</CODE> object over all the <CODE>
     *         SOAPHeaderElement</CODE> objects that contain the
     *         specified actor
     * @see #extractHeaderElements(String) extractHeaderElements(java.lang.String)
     */
    public Iterator examineHeaderElements(String actor) {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    /**
     * Returns a list of all the <CODE>SOAPHeaderElement</CODE>
     * objects in this <CODE>SOAPHeader</CODE> object that have
     * the the specified actor and detaches them from this <CODE>
     * SOAPHeader</CODE> object.
     * <p/>
     * <P>This method allows an actor to process only the parts of
     * the <CODE>SOAPHeader</CODE> object that apply to it and to
     * remove them before passing the message on to the next
     * actor.
     *
     * @param actor a <CODE>String</CODE> giving the
     *              URI of the actor for which to search
     * @return an <CODE>Iterator</CODE> object over all the <CODE>
     *         SOAPHeaderElement</CODE> objects that contain the
     *         specified actor
     * @see #examineHeaderElements(String) examineHeaderElements(java.lang.String)
     */
    public Iterator extractHeaderElements(String actor) {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderElement</code> objects in this <code>SOAPHeader</code>
     * object that have the specified actor and that have a MustUnderstand
     * attribute whose value is equivalent to <code>true</code>.
     *
     * @param actor a <code>String</code> giving the URI of the actor for which
     *              to search
     * @return an <code>Iterator</code> object over all the
     *         <code>SOAPHeaderElement</code> objects that contain the
     *         specified actor and are marked as MustUnderstand
     */
    public Iterator examineMustUnderstandHeaderElements(String actor) {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderElement</code> objects in this <code>SOAPHeader</code>
     * object.
     *
     * Not that this will return elements containing the QName (http://schemas.xmlsoap.org/soap/envelope/, Header)
     *
     * @return an <code>Iterator</code> object over all the
     *         <code>SOAPHeaderElement</code> objects contained by this
     *         <code>SOAPHeader</code>
     */
    public Iterator examineAllHeaderElements() {
        return this.getChildrenWithName(null);
    }

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderElement</code> objects in this <code>SOAPHeader </code>
     * object and detaches them from this <code>SOAPHeader</code> object.
     *
     * @return an <code>Iterator</code> object over all the
     *         <code>SOAPHeaderElement</code> objects contained by this
     *         <code>SOAPHeader</code>
     */
    public Iterator extractAllHeaderElements() {
        throw new UnsupportedOperationException(); //TODO implement this
    }
}
