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
package org.apache.axis2.saaj;

import org.apache.ws.commons.om.OMNamespace;
import org.apache.axis2.doom.dom.ElementImpl;
import org.apache.axis2.doom.dom.NamespaceImpl;
import org.apache.ws.commons.soap.SOAPHeaderBlock;
import org.apache.axis.soap.impl.dom.soap11.SOAP11HeaderBlockImpl;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class SOAPHeaderImpl extends SOAPElementImpl implements SOAPHeader {

    private org.apache.ws.commons.soap.SOAPHeader omSOAPHeader;

    /**
     * Constructor
     *
     * @param header
     */
    public SOAPHeaderImpl(org.apache.ws.commons.soap.SOAPHeader header) {
        super((ElementImpl) header);
        omSOAPHeader = header;
    }

    /**
     * Creates a new <CODE>SOAPHeaderElement</CODE> object
     * initialized with the specified name and adds it to this
     * <CODE>SOAPHeader</CODE> object.
     *
     * @param name a <CODE>Name</CODE> object with
     *             the name of the new <CODE>SOAPHeaderElement</CODE>
     *             object
     * @return the new <CODE>SOAPHeaderElement</CODE> object that
     *         was inserted into this <CODE>SOAPHeader</CODE>
     *         object
     * @throws SOAPException if a SOAP error occurs
     */
    public SOAPHeaderElement addHeaderElement(Name name) throws SOAPException {
        OMNamespace ns = new NamespaceImpl(name.getURI(), name.getPrefix());
        SOAPHeaderBlock headerBlock = new SOAP11HeaderBlockImpl(name.getLocalName(), ns, omSOAPHeader);
        return new SOAPHeaderElementImpl(headerBlock);
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
     * @see #extractHeaderElements(java.lang.String) extractHeaderElements(java.lang.String)
     */
    public Iterator examineHeaderElements(String actor) {
        Collection elements = new ArrayList();
        for (Iterator iterator = omSOAPHeader.examineHeaderBlocks(actor); iterator.hasNext();) {
            elements.add(new SOAPHeaderElementImpl((SOAPHeaderBlock) iterator.next()));
        }
        return elements.iterator();
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
     * @see #examineHeaderElements(java.lang.String) examineHeaderElements(java.lang.String)
     */
    public Iterator extractHeaderElements(String actor) {
        Collection elements = new ArrayList();
        for (Iterator iterator = omSOAPHeader.extractHeaderBlocks(actor); iterator.hasNext();) {
            elements.add(new SOAPHeaderElementImpl((SOAPHeaderBlock) iterator.next()));
        }
        return elements.iterator();
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
        Collection elements = new ArrayList();
        for (Iterator iterator = omSOAPHeader.examineMustUnderstandHeaderBlocks(actor); iterator.hasNext();)
        {
            elements.add(new SOAPHeaderElementImpl((SOAPHeaderBlock) iterator.next()));
        }
        return elements.iterator();
    }

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderElement</code> objects in this <code>SOAPHeader</code>
     * object.
     *
     * @return an <code>Iterator</code> object over all the
     *         <code>SOAPHeaderElement</code> objects contained by this
     *         <code>SOAPHeader</code>
     */
    public Iterator examineAllHeaderElements() {
        Collection elements = new ArrayList();
        for (Iterator iterator = omSOAPHeader.examineAllHeaderBlocks(); iterator.hasNext();) {
            elements.add(new SOAPHeaderElementImpl((SOAPHeaderBlock) iterator.next()));
        }
        return elements.iterator();
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
        Collection elements = new ArrayList();
        for (Iterator iterator = omSOAPHeader.extractAllHeaderBlocks(); iterator.hasNext();) {
            elements.add(new SOAPHeaderElementImpl((SOAPHeaderBlock) iterator.next()));
        }
        return elements.iterator();
    }
}