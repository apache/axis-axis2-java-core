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
package org.apache.axis2.om.impl.llom.builder;

import org.apache.axis2.om.*;
import org.apache.axis2.om.impl.llom.OMDocument;
import org.apache.axis2.soap.SOAPEnvelope;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

/**
 * This will construct an OM without using SOAP specific classes like SOAPEnvelope, SOAPHeader, SOAPHeaderBlock and SOAPBody.
 * And this will habe the Document concept also.
 */
public class StAXOMBuilder extends StAXBuilder{
    /**
     * Field document
     */
    protected OMDocument document;

     /**
     * Constructor StAXOMBuilder
     *
     * @param ombuilderFactory
     * @param parser
     */
    public StAXOMBuilder(OMFactory ombuilderFactory, XMLStreamReader parser) {
        super(ombuilderFactory, parser);
        document = new OMDocument(this);
        omfactory = OMAbstractFactory.getOMFactory();
    }

    /**
     * Constructor StAXOMBuilder
     *
     * @param parser
     */
    public StAXOMBuilder(XMLStreamReader parser) {
        super(parser);
        document = new OMDocument(this);
        omfactory = OMAbstractFactory.getOMFactory();
    }

    /**
     * Method createOMElement
     *
     * @return
     * @throws OMException
     */
    protected OMNode createOMElement() throws OMException {
        OMElement node;
        String elementName = parser.getLocalName();
        if (lastNode == null) {
            node = omfactory.createOMElement(elementName, null, null, this);
            document.setRootElement(node);
            document.addChild(node);
        } else if (lastNode.isComplete()) {
            node = omfactory.createOMElement(elementName, null,
                    lastNode.getParent(), this);
            lastNode.setNextSibling(node);
            node.setPreviousSibling(lastNode);
        } else {
            OMElement e = (OMElement) lastNode;
            node = omfactory.createOMElement(elementName, null,
                    (OMElement) lastNode, this);
            e.setFirstChild(node);
        }

        // create the namespaces
        processNamespaceData(node, false);

        // fill in the attributes
        processAttributes(node);
        return node;
    }

    /**
     * Method createOMText
     *
     * @return
     * @throws OMException
     */
    protected OMNode createComment() throws OMException {
        OMNode node;
        if (lastNode == null) {
            node = omfactory.createText(parser.getText());
            document.addChild(node);
        } else if (lastNode.isComplete()) {
            node = omfactory.createText((OMElement)lastNode.getParent(), parser.getText());
            lastNode.setNextSibling(node);
            node.setPreviousSibling(lastNode);
        } else {
            OMElement e = (OMElement) lastNode;
            node = omfactory.createText(e, parser.getText());
            e.setFirstChild(node);
        }
        node.setType(OMNode.COMMENT_NODE);
        return node;
    }

    /**
     * Method createDTD
     *
     * @return
     * @throws OMException
     */
    protected OMNode createDTD() throws OMException {
        if(!parser.hasText())
            return null;
        OMNode node = omfactory.createText(parser.getText());
        document.addChild(node);
        node.setType(OMNode.DTD_NODE);
        return node;
    }

    protected OMNode createPI() throws OMException {
        OMNode node;
        String target = parser.getPITarget();
        String data = parser.getPIData();
        if (lastNode == null) {
            node = omfactory.createText("<?" + target + " " + data + "?>");
            node.setType(OMNode.PI_NODE);
            document.addChild(node);
        } else if (lastNode.isComplete()) {
            node = omfactory.createText((OMElement)lastNode.getParent(), "<?" + target + " " + data + "?>");
            node.setType(OMNode.PI_NODE);
            lastNode.setNextSibling(node);
            node.setPreviousSibling(lastNode);
        } else if (lastNode instanceof OMText) {
            node = omfactory.createText("<?" + target + " " + data + "?>");
            node.setType(OMNode.PI_NODE);
            lastNode.getParent().addChild(node);
        } else {
            OMElement e = (OMElement) lastNode;
            node = omfactory.createText(e, "<?" + target + " " + data + "?>");
            node.setType(OMNode.PI_NODE);
            e.setFirstChild(node);
        }
        return node;
    }

    /**
     * Method getOMEnvelope
     *
     * @return
     * @throws OMException
     */
    public SOAPEnvelope getOMEnvelope() throws OMException {
        throw new UnsupportedOperationException();    // TODO implement this
    }

    /**
     * Method next
     *
     * @return
     * @throws OMException
     */
    public int next() throws OMException {
        try {
            if (done) {
                throw new OMException();
            }
            ///////////////////////////////////
//            int token = parser.getEventType();
            //////////////////////////////////

            int token = parser.next();
            if (!cache) {
                return token;
            }
            switch (token) {
                case XMLStreamConstants.START_ELEMENT:
                    lastNode = createOMElement();
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                    //Don't do anything in the start document event
                    //We've already assumed that start document has passed!
                    //document = new OMDocument(this);
                    break;
                case XMLStreamConstants.CHARACTERS:
                    lastNode = createOMText();
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (lastNode.isComplete()) {
                        OMElement parent = (OMElement)lastNode.getParent();
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
                case XMLStreamConstants.COMMENT:
                    createComment();
                    break;
                case XMLStreamConstants.DTD:
                    createDTD();
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    createPI();
                    break;
                default :
                    throw new OMException();
            }
            ////////////////////
           // if (!done) parser.next();
            ///////////////////
            return token;
        } catch (OMException e) {
            throw e;
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    /**
     * Method getDocumentElement
     *
     * @return
     */
    public OMElement getDocumentElement() {
        return document.getRootElement();
    }

    /**
     * Method processNamespaceData
     *
     * @param node
     * @param isSOAPElement
     */
    protected void processNamespaceData(OMElement node, boolean isSOAPElement) {
          int namespaceCount = parser.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            node.declareNamespace(parser.getNamespaceURI(i),
                    parser.getNamespacePrefix(i));
        }

        // set the own namespace
        String namespaceURI = parser.getNamespaceURI();
        String prefix = parser.getPrefix();
        OMNamespace namespace = null;
        if (!"".equals(namespaceURI)) {
            if (prefix == null) {
                // this means, this elements has a default namespace or it has inherited a default namespace from its parent
                namespace = node.findNamespace(namespaceURI, "");
                if (namespace == null) {
                    namespace = node.declareNamespace(namespaceURI, "");
                }
            } else {
                namespace = node.findNamespace(namespaceURI, prefix);
                if(namespace == null){
                    node.setNamespace(omfactory.createOMNamespace(namespaceURI, prefix));
                }else{
                    node.setNamespace(namespace);
                }
            }

        } else {
            // What to do if namespace URI is not available
        }
    }
//        int namespaceCount = parser.getNamespaceCount();
//        for (int i = 0; i < namespaceCount; i++) {
//            node.declareNamespace(parser.getNamespaceURI(i),
//                    parser.getNamespacePrefix(i));
//        }
//
//        // set the own namespace
//        OMNamespace namespace =
//                node.findNamespace(parser.getNamespaceURI(),
//                        parser.getPrefix());
//        node.setNamespace(namespace);
//    }
}
