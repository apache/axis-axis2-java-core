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

package org.apache.axis2.om.impl.llom.factory;

import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMComment;
import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMDocType;
import org.apache.axis2.om.OMDocument;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMProcessingInstruction;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.OMAttributeImpl;
import org.apache.axis2.om.impl.llom.OMCommentImpl;
import org.apache.axis2.om.impl.llom.OMDocTypeImpl;
import org.apache.axis2.om.impl.llom.OMDocumentImpl;
import org.apache.axis2.om.impl.llom.OMElementImpl;
import org.apache.axis2.om.impl.llom.OMNamespaceImpl;
import org.apache.axis2.om.impl.llom.OMProcessingInstructionImpl;
import org.apache.axis2.om.impl.llom.OMTextImpl;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * Class OMLinkedListImplFactory
 */
public class OMLinkedListImplFactory implements OMFactory {

    private static final String uriAndPrefixSeparator = ";";
    /**
     * This is a map of namespaces with the namespace URI as the key and
     * Namespace object itself as the value
     */
    protected Map namespaceTable = new HashMap(5);

    /**
     * Method createOMElement
     *
     * @param localName
     * @param ns
     * @return element
     */
    public OMElement createOMElement(String localName, OMNamespace ns) {
        return new OMElementImpl(localName, ns);
    }

    public OMElement createOMElement(String localName, OMNamespace ns, OMContainer parent) {
        return new OMElementImpl(localName, ns, parent);
    }

    /**
     * Method createOMElement
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return element
     */
    public OMElement createOMElement(String localName, OMNamespace ns,
                                     OMContainer parent,
                                     OMXMLParserWrapper builder) {
        return new OMElementImpl(localName, ns, parent,
                builder);
    }

    /**
     * Method createOMElement
     *
     * @param localName
     * @param namespaceURI
     * @param namespacePrefix
     * @return element
     */
    public OMElement createOMElement(String localName, String namespaceURI,
                                     String namespacePrefix) {
        return this.createOMElement(localName,
                this.createOMNamespace(namespaceURI,
                        namespacePrefix));
    }

    /**
     * Method createOMElement
     *
     * @param qname
     * @param parent
     * @return
     * @throws OMException
     */
    public OMElement createOMElement(QName qname, OMContainer parent)
            throws OMException {
        return new OMElementImpl(qname, parent);
    }

    /**
     * Method createOMNamespace
     *
     * @param uri
     * @param prefix
     * @return namespace
     */
    public OMNamespace createOMNamespace(String uri, String prefix) {
        String key = uri + uriAndPrefixSeparator + prefix;
        OMNamespace existingNamespaceObject = (OMNamespace) namespaceTable.get(key);
        if (existingNamespaceObject == null) {
            existingNamespaceObject = new OMNamespaceImpl(uri, prefix);
            namespaceTable.put(key, existingNamespaceObject);
        }
        return existingNamespaceObject;
    }

    /**
     * Method createText
     *
     * @param parent
     * @param text
     * @return text
     */
    public OMText createText(OMElement parent, String text) {
        return new OMTextImpl(parent, text);
    }

    public OMText createText(OMElement parent, String text, int type) {
        return new OMTextImpl(parent, text, type);
    }

    /**
     * Method createText
     *
     * @param s
     * @return text
     */
    public OMText createText(String s) {
        return new OMTextImpl(s);
    }

    public OMText createText(String s, int type) {
        return new OMTextImpl(s, type);
    }

    /**
     * create Text
     *
     * @param s
     * @param mimeType
     * @param optimize
     * @return text
     */
    public OMText createText(String s, String mimeType, boolean optimize) {
        return new OMTextImpl(s, mimeType, optimize);
    }

    /**
     * create text
     *
     * @param dataHandler
     * @param optimize
     * @return text
     */
    public OMText createText(Object dataHandler, boolean optimize) {
        return new OMTextImpl(dataHandler, optimize);
    }

    public OMText createText(String contentID, OMElement parent,
                             OMXMLParserWrapper builder) {
        return new OMTextImpl(contentID, parent, builder);
    }

    /**
     * create text
     *
     * @param parent
     * @param s
     * @param mimeType
     * @param optimize
     * @return text
     */
    public OMText createText(OMElement parent,
                             String s,
                             String mimeType,
                             boolean optimize) {
        return new OMTextImpl(parent, s, mimeType, optimize);
    }

    /**
     * create attribute
     *
     * @param localName
     * @param ns
     * @param value
     * @return attribute
     */
    public OMAttribute createOMAttribute(String localName,
                                         OMNamespace ns,
                                         String value) {
        return new OMAttributeImpl(localName, ns, value);
    }

    /**
     * create DocType/DTD
     *
     * @param parent
     * @param content
     * @return doctype
     */
    public OMDocType createOMDocType(OMContainer parent, String content) {
        return new OMDocTypeImpl(parent, content);
    }

    /**
     * create a PI
     *
     * @param parent
     * @param piTarget
     * @param piData
     * @return pi
     */
    public OMProcessingInstruction createOMProcessingInstruction(OMContainer parent, String piTarget, String piData) {
        return new OMProcessingInstructionImpl(parent, piTarget, piData);
    }

    /**
     * create a comment
     *
     * @param parent
     * @param content
     * @return comment
     */
    public OMComment createOMComment(OMContainer parent, String content) {
        return new OMCommentImpl(parent, content);
    }

    /* (non-Javadoc)
    * @see org.apache.axis2.om.OMFactory#createOMDocument()
    */
    public OMDocument createOMDocument() {
        return new OMDocumentImpl();
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.om.OMFactory#createOMDocument(org.apache.axis2.om.OMXMLParserWrapper)
      */
    public OMDocument createOMDocument(OMXMLParserWrapper builder) {
        return new OMDocumentImpl(builder);
    }
}
