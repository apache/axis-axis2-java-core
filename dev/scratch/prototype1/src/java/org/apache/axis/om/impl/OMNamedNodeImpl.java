package org.apache.axis.om.impl;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNamedNode;
import org.apache.axis.om.OMNamespace;
import org.apache.xml.utils.QName;

import java.io.PrintStream;

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
 * Date: Oct 5, 2004
 * Time: 1:17:37 PM
 */
public class OMNamedNodeImpl extends OMNodeImpl implements OMNamedNode {

    OMNamespace ns;
    String localName;

    

    public OMNamedNodeImpl(String localName, OMNamespace ns, OMElement parent) {
        super(parent);
        this.localName = localName;
        this.ns = ns;
    }

    public OMNamedNodeImpl(OMElement parent) {
        super(parent);
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public OMNamespace getNamespace() throws OMException {
        return ns;
    }

    /**
     * @param namespace
     */
    public void setNamespace(OMNamespace namespace) {
        this.ns = namespace;
    }

    public void print(PrintStream ps) throws OMException {
        if (ns != null && !ns.isDefaultNs()) {
            ps.print(ns.getPrefix());
            ps.print(':');
        }
        ps.print(localName);
    }

    public QName getQName() {
        QName qName = new QName(ns.getValue(), ns.getPrefix(), localName);
        return qName;
    }

//    /**
//     * Equals method of QName seems doesn't working the way I need in this situation.
//     * So implementing my own !!
//     *
//     * @param qName
//     * @return
//     */
//    public boolean isQNameEquals(QName qName) {
//        String thatLocalName = qName.getLocalName();
//
//        return ((thatLocalName == null && this.localName == null) || (thatLocalName != null && thatLocalName.equalsIgnoreCase(this.localName)))
//                && ns.equals(qName.getNamespaceURI(), qName.getPrefix());
//    }
//
//    private String getStringValue(String s) {
//        if (s == null) {
//            return "null";
//        }
//
//        return s;
//    }
}
