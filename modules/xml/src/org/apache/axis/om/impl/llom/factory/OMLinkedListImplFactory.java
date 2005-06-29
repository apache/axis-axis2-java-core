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
package org.apache.axis.om.impl.llom.factory;

import org.apache.axis.om.*;
import org.apache.axis.om.impl.llom.OMElementImpl;
import org.apache.axis.om.impl.llom.OMNamespaceImpl;
import org.apache.axis.om.impl.llom.OMTextImpl;
import org.apache.axis.om.impl.llom.OMAttributeImpl;

import javax.xml.namespace.QName;
import javax.activation.DataHandler;

/**
 * Class OMLinkedListImplFactory
 */
public class OMLinkedListImplFactory implements OMFactory {
    /**
     * Field MAX_TO_POOL
     */
    public static final int MAX_TO_POOL = 100;

    /**
     * Method createOMElement
     *
     * @param localName
     * @param ns
     * @return
     */
    public OMElement createOMElement(String localName, OMNamespace ns) {
        OMElementImpl element = new OMElementImpl(localName, ns);
        return element;
    }

    /**
     * Method createOMElement
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public OMElement createOMElement(String localName, OMNamespace ns,
                                     OMContainer parent,
                                     OMXMLParserWrapper builder) {
        OMElementImpl element = new OMElementImpl(localName, ns, parent,
                builder);
        return element;
    }

    /**
     * Method createOMElement
     *
     * @param localName
     * @param namespaceURI
     * @param namespacePrefix
     * @return
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
     * @return
     */
    public OMNamespace createOMNamespace(String uri, String prefix) {
        return new OMNamespaceImpl(uri, prefix);
    }

    /**
     * Method createText
     *
     * @param parent
     * @param text
     * @return
     */
    public OMText createText(OMElement parent, String text) {
        OMTextImpl textNode = new OMTextImpl(parent, text);
        return textNode;
    }

    /**
     * Method createText
     *
     * @param s
     * @return
     */
    public OMText createText(String s) {
        OMTextImpl textNode = new OMTextImpl(s);
    ;
        return textNode;
    }

    public OMText createText(String s, String mimeType, boolean optimize) {
        return new OMTextImpl(s, mimeType, optimize);
    }

    public OMText createText(DataHandler dataHandler, boolean optimize) {
        return new OMTextImpl(dataHandler, optimize);
    }

    public OMText createText(OMElement parent, String s, String mimeType, boolean optimize) {
        return new OMTextImpl(parent, s, mimeType, optimize);
    }

    public OMAttribute createOMAttribute(String localName, OMNamespace ns, String value) {
          return new OMAttributeImpl(localName, ns, value);
      }


}
