package org.apache.axis.impl.llom;

import org.apache.axis.om.*;
import org.apache.axis.impl.llom.OMBodyElementImpl;

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
 * Time: 4:29:00 PM
 */
public class OMBodyImpl extends OMElementImpl implements OMBody {



    /**
     * @param envelope
     */
    public OMBodyImpl(OMEnvelope envelope) {
        super(envelope);
    }

    public OMBodyImpl(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        super(localName, ns, parent, builder);
    }

    /**
     * Creates a new <code>OMFault</code> object and adds it to
     * this <code>OMBody</code> object.
     *
     * @return the new <code>OMFault</code> object
     * @throws org.apache.axis.om.OMException if there is a SOAP error
     */
    public OMFault addFault() throws OMException {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    /**
     * Indicates whether a <code>OMFault</code> object exists in
     * this <code>OMBody</code> object.
     *
     * @return <code>true</code> if a <code>OMFault</code> object exists in
     *         this <code>OMBody</code> object; <code>false</code>
     *         otherwise
     */
    public boolean hasFault() {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    /**
     * Returns the <code>OMFault</code> object in this <code>OMBody</code>
     * object.
     *
     * @return the <code>OMFault</code> object in this <code>OMBody</code>
     *         object
     */
    public OMFault getFault() {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    /**
     * Creates a new <code>OMBodyBlock</code> object with the
     * specified name and adds it to this <code>OMBody</code> object.
     *
     * @param element a <code>OMNamedNode</code> object with the name for the new
     *                <code>OMBodyBlock</code> object
     * @return the new <code>OMBodyBlock</code> object
     * @throws org.apache.axis.om.OMException if a SOAP error occurs
     */
    public OMBodyBlock addBodyElement(String localName, OMNamespace ns) throws OMException {
        OMBodyBlock omBodyElement = new OMBodyElementImpl(localName, ns);
        this.addChild(omBodyElement);
        omBodyElement.setComplete(true);
        return omBodyElement;
    }

    /**
     * @param omFault
     * @throws org.apache.axis.om.OMException
     */
    public void addFault(OMFault omFault) throws OMException {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    /**
     * Adds the root node of the DOM <code>Document</code> to this
     * <code>OMBody</code> object.
     * <p/>
     * Calling this method invalidates the <code>document</code> parameter. The
     * client application should discard all references to this
     * <code>Document</code> and its contents upon calling
     * <code>addDocument</code>. The behavior of an application that continues
     * to use such references is undefined.
     *
     * @param element the <code>Document</code> object whose root node will be
     *                added to this <code>OMBody</code>
     * @return the <code>OMBodyBlock</code> that represents the root node
     *         that was added
     * @throws org.apache.axis.om.OMException if the <code>Document</code> cannot be added
     */
    public OMBodyBlock addDocument(OMElement element) throws OMException {
        throw new UnsupportedOperationException(); //TODO implement this
    }
}
