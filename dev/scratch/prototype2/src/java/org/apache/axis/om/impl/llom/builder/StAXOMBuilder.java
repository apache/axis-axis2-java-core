package org.apache.axis.om.impl.llom.builder;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.impl.llom.OMDocument;

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
 */

/** 
 * This will construct an OM without using SOAP specific classes like SOAPEnvelope, SOAPHeader, SOAPHeaderBlock and SOAPBody.
 * And this will habe the Document concept also.
 */
public class StAXOMBuilder extends StAXBuilder implements OMXMLParserWrapper {
    protected OMDocument document;
    protected OMFactory omFactory;

    public StAXOMBuilder(OMFactory ombuilderFactory, XMLStreamReader parser) {
        super(ombuilderFactory, parser);
        document = new OMDocument(this);
		omfactory = OMFactory.newInstance();
    }

    public StAXOMBuilder(XMLStreamReader parser) {
        super(parser);
        document = new OMDocument(this);
		omfactory = OMFactory.newInstance();
    }

    protected OMNode createOMElement() throws OMException {
        OMElement node;
        String elementName = parser.getLocalName();

        if (lastNode == null) {
            node = omfactory.createOMElement(elementName, null, null, this);
            document.setRootElement(node);
        } else if (lastNode.isComplete()) {
            node = omfactory.createOMElement(elementName, null, lastNode.getParent(), this);
            lastNode.setNextSibling(node);
            node.setPreviousSibling(lastNode);
        } else {
            OMElement e = (OMElement) lastNode;
            node = omfactory.createOMElement(elementName, null, (OMElement) lastNode, this);
            e.setFirstChild(node);
        }

        //create the namespaces
        processNamespaceData(node, false);

        //fill in the attributes
        processAttributes(node);

        return node;
    }

    public SOAPEnvelope getOMEnvelope() throws OMException {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    public int next() throws OMException {
        try {

            if (done)
                throw new OMException();

            int token = parser.next();

            if (!cache) {
                return token;
            }

            switch (token) {
                case XMLStreamConstants.START_ELEMENT:
                    lastNode = createOMElement();
                    break;

                case XMLStreamConstants.START_DOCUMENT:
                    document = new OMDocument(this);
                    break;

                case XMLStreamConstants.CHARACTERS:
                    lastNode = createOMText();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (lastNode.isComplete()) {
                        OMElement parent = lastNode.getParent();
                        parent.setComplete(true);
                        lastNode = parent;
                    } else {
                        OMElement e = (OMElement) lastNode;
                        e.setComplete(true);
                    }
                    break;

                case XMLStreamConstants.END_DOCUMENT:
                    done = true;

                    break;
                case XMLStreamConstants.SPACE:
                    next();
                    break;

                default :
                    throw new OMException();
            }
            return token;
        } catch (OMException e) {
            throw e;
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    public OMElement getDocumentElement() {
        return document.getRootElement();
    }

    protected void processNamespaceData(OMElement node, boolean isSOAPElement) {
        int namespaceCount = parser.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            node.declareNamespace(parser.getNamespaceURI(i), parser.getNamespacePrefix(i));
        }

        //set the own namespace
        OMNamespace namespace = node.findInScopeNamespace(parser.getNamespaceURI(), parser.getPrefix());
        node.setNamespace(namespace);
    }
}
