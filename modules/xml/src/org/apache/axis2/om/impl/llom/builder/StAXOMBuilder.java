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

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.impl.OMContainerEx;
import org.apache.axis2.om.impl.OMNodeEx;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


/**
 * This will construct an OM without using SOAP specific classes like SOAPEnvelope, SOAPHeader, SOAPHeaderBlock and SOAPBody.
 * And this will habe the Document concept also.
 */
public class StAXOMBuilder extends StAXBuilder {
    /**
     * Field document
     */

    private boolean doDebug = false;
    private static int nsCount = 0;

    /**
     * Constructor StAXOMBuilder
     *
     * @param ombuilderFactory
     * @param parser
     */
    public StAXOMBuilder(OMFactory ombuilderFactory, XMLStreamReader parser) {
        super(ombuilderFactory, parser);
        document = ombuilderFactory.createOMDocument(this);
    }

    /**
     *
     * @param filePath - Path to the XML file
     * @throws XMLStreamException
     * @throws FileNotFoundException
     */
    public StAXOMBuilder(String filePath) throws XMLStreamException, FileNotFoundException {
       this(XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(filePath)));
    }

    /**
     *
     * @param inStream - instream which contains the XML
     * @throws XMLStreamException
     */
    public StAXOMBuilder(InputStream inStream) throws XMLStreamException {
       this(XMLInputFactory.newInstance().createXMLStreamReader(inStream));
    }

    /**
     * Constructor StAXOMBuilder
     *
     * @param parser
     */
    public StAXOMBuilder(XMLStreamReader parser) {
        super(parser);
        omfactory = OMAbstractFactory.getOMFactory();
        document = omfactory.createOMDocument(this);
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
            node = omfactory.createOMElement(elementName, null, document, this);
        } else if (lastNode.isComplete()) {
            node = omfactory.createOMElement(elementName, null,
                    lastNode.getParent(), this);
            ((OMNodeEx)lastNode).setNextOMSibling(node);
            ((OMNodeEx)node).setPreviousOMSibling(lastNode);
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
            node = omfactory.createOMComment(document, parser.getText());
        } else if (lastNode.isComplete()) {
            node = omfactory.createOMComment(lastNode.getParent(), parser.getText());
        } else {
            node = omfactory.createOMComment((OMElement) lastNode, parser.getText());
        }
        return node;
    }

    /**
     * Method createDTD
     *
     * @return
     * @throws OMException
     */
    protected OMNode createDTD() throws OMException {
        if (!parser.hasText())
            return null;
        return omfactory.createOMDocType(document, parser.getText());
    }

    /**
     * Method createPI
     * @return
     * @throws OMException
     */
    protected OMNode createPI() throws OMException {
        OMNode node;
        String target = parser.getPITarget();
        String data = parser.getPIData();
        if (lastNode == null) {
            node = omfactory.createOMProcessingInstruction(document, target, data);
        } else if (lastNode.isComplete()) {
            node = omfactory.createOMProcessingInstruction(lastNode.getParent(), target, data);
        } else if (lastNode instanceof OMText) {
            node = omfactory.createOMProcessingInstruction(lastNode.getParent(), target, data);
        } else {
            node = omfactory.createOMProcessingInstruction((OMContainer) lastNode, target, data);
        }
        return node;
    }

    protected void endElement(){
        if (lastNode.isComplete()) {
            OMElement parent = (OMElement) lastNode.getParent();
            ((OMNodeEx)parent).setComplete(true);
            lastNode = parent;
        } else {
            OMElement e = (OMElement) lastNode;
            ((OMNodeEx)e).setComplete(true);
        }

        //return lastNode;
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
            int token = parser.next();
            if (!cache) {
                return token;
            }
            switch (token) {
                case XMLStreamConstants.START_ELEMENT:
                    if(doDebug) {
                        System.out.println("START_ELEMENT: " + parser.getName() + ":" + parser.getLocalName());
                    }
                    lastNode = createOMElement();
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                    // Document has already being created.
                    
                    document.setXMLVersion(parser.getVersion());
                    document.setCharsetEncoding(parser.getEncoding());
                    document.setStandalone(parser.isStandalone() ? "yes" : "no");
                    if(doDebug) {
                        System.out.println("START_DOCUMENT: ");
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    if(doDebug) {
                        System.out.println("CHARACTERS: [" + parser.getText() + "]");
                    }
                    lastNode = createOMText(XMLStreamConstants.CHARACTERS);
                    break;
                case XMLStreamConstants.CDATA:
                    if(doDebug) {
                        System.out.println("CDATA: [" + parser.getText() + "]");
                    }
                    lastNode = createOMText(XMLStreamConstants.CDATA);
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if(doDebug) {
                        System.out.println("END_ELEMENT: " + parser.getName() + ":" + parser.getLocalName());
                    }
                    endElement();
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    if(doDebug) {
                        System.out.println("END_DOCUMENT: ");
                    }
                    done = true;
                    ((OMContainerEx)this.document).setComplete(true);
                    break;
                case XMLStreamConstants.SPACE:
                    if(doDebug) {
                        System.out.println("SPACE: [" + parser.getText() + "]");
                    }
                    lastNode = createOMText(XMLStreamConstants.SPACE);
                    break;
                case XMLStreamConstants.COMMENT:
                    if(doDebug) {
                        System.out.println("COMMENT: [" + parser.getText() + "]");
                    }
                    createComment();
                    break;
                case XMLStreamConstants.DTD:
                    if(doDebug) {
                        System.out.println("DTD: [" + parser.getText() + "]");
                    }
                    createDTD();
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    if(doDebug) {
                        System.out.println("PROCESSING_INSTRUCTION: [" + parser.getPITarget() + "][" + parser.getPIData() + "]");
                    }
                    createPI();
                    break;
                case XMLStreamConstants.ENTITY_REFERENCE:
                    if(doDebug) {
                        System.out.println("ENTITY_REFERENCE: " + parser.getLocalName() + "[" + parser.getText() + "]");
                    }
                    lastNode = createOMText(XMLStreamConstants.ENTITY_REFERENCE);
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

   /**
     * Method getDocumentElement
     *
     * @return root element
     */
    public OMElement getDocumentElement() {
        return document.getOMDocumentElement();
    }

    /**
     * Method processNamespaceData
     *
     * @param node
     * @param isSOAPElement
     */
    protected void processNamespaceData(OMElement node, boolean isSOAPElement) {
        // set the own namespace
        String namespaceURI = parser.getNamespaceURI();
        String prefix = parser.getPrefix();

        OMNamespace namespace = null;
        if (namespaceURI != null && namespaceURI.length() > 0) {

            // prefix being null means this elements has a default namespace or it has inherited
            // a default namespace from its parent
            prefix = prefix == null ? "" : prefix;
            namespace = node.findNamespace(namespaceURI, prefix);

            if (namespace == null) {
                namespace = node.declareNamespace(namespaceURI, prefix);
            }
            node.setNamespace(namespace);
        }


        int namespaceCount = parser.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            if (!parser.getNamespaceURI(i).equals(namespaceURI)){
            node.declareNamespace(parser.getNamespaceURI(i),
                    parser.getNamespacePrefix(i));
            }
        }
    }



    public void setDoDebug(boolean doDebug) {
        this.doDebug = doDebug;
    }

    protected String createPrefix(){
        return "ns"+ nsCount++;
    }

}
