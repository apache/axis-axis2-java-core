package org.apache.axis.impl.llom.builder;

import org.apache.axis.impl.llom.OMElementImpl;
import org.apache.axis.impl.llom.exception.OMBuilderException;
import org.apache.axis.om.*;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

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
 *
 * @author Axis team
 *         Date: Nov 18, 2004
 *         Time: 2:30:17 PM
 *         <p/>
 *         Note - OM navigator has been removed to simplify the build process
 */
public class StAXSOAPModelBuilder extends StAXBuilder {

    private SOAPEnvelope envelope;
    private boolean headerPresent = false;
    private boolean bodyPresent = false;

    /**
     * element level 1 = envelope level
     * element level 2 = Header or Body level
     * element level 3 = HeaderElement or BodyElement level
     */
    private int elementLevel = 0;

    public StAXSOAPModelBuilder(OMFactory ombuilderFactory, XMLStreamReader parser) {
        super(ombuilderFactory, parser);
    }

    public StAXSOAPModelBuilder(XMLStreamReader parser) {
        super(parser);
    }

    public SOAPEnvelope getOMEnvelope() throws OMException {
        while (envelope == null && !done) {
            next();
        }
        return envelope;
    }

    protected OMNode createOMElement() throws OMException {
        OMElement node;
        String elementName = parser.getLocalName();

        if (lastNode == null) {
            node = constructNode(null, elementName, true);
        } else if (lastNode.isComplete()) {
            node = constructNode(lastNode.getParent(), elementName, false);
            lastNode.setNextSibling(node);
            node.setPreviousSibling(lastNode);
        } else {
            OMElement e = (OMElement) lastNode;
            node = constructNode((OMElement) lastNode, elementName, false);
            e.setFirstChild(node);
        }



        //fill in the attributes
        processAttributes(node);

        return node;
    }

    private OMElement constructNode(OMElement parent, String elementName, boolean isEnvelope) {

        OMElement element = null;
        if (isEnvelope) {

            envelope = ombuilderFactory.createSOAPEnvelope(elementName, null, null, this);
            element = (OMElementImpl) envelope;
            processNamespaceData(element, true);

        } else if (elementLevel == 2) {
            // this is either a header or a body
            if (elementName.equals(OMConstants.HEADER_LOCAL_NAME)) {
                if (headerPresent) {
                    throw new OMBuilderException("Multiple headers encountered!");
                }
                if (bodyPresent) {
                    throw new OMBuilderException("Header Body wrong order!");
                }
                headerPresent = true;
                element = ombuilderFactory.createSOAPHeader(elementName, null, parent, this);
                processNamespaceData(element, true);
            } else if (elementName.equals(OMConstants.BODY_LOCAL_NAME)) {
                if (bodyPresent) {
                    throw new OMBuilderException("Multiple body elements encountered");
                }
                bodyPresent = true;
                element = ombuilderFactory.createSOAPBody(elementName, null, parent, this);
                processNamespaceData(element, true);
            } else {
                throw new OMBuilderException(elementName + " is not supported here. Envelope can not have elements other than Header and Body.");
            }

        } else if (elementLevel == 3 && parent.getLocalName().equalsIgnoreCase(OMConstants.HEADER_LOCAL_NAME)) {
            // this is a headerblock
            element = ombuilderFactory.createSOAPHeaderBlock(elementName, null, parent, this);
            processNamespaceData(element, false);

        } else {
            // this is neither of above. Just create an element
            element = ombuilderFactory.createOMElement(elementName, null, parent, this);
            processNamespaceData(element, false);
        }

        return element;
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
                    elementLevel++;
                    lastNode = createOMElement();
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
                    elementLevel--;
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
            e.printStackTrace();
            throw new OMException(e);
        }
    }

    public OMElement getDocumentElement() {
        return getOMEnvelope();
    }

    protected void processNamespaceData(OMElement node, boolean isSOAPElement) {
        int namespaceCount = parser.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            node.createNamespace(parser.getNamespaceURI(i), parser.getNamespacePrefix(i));
        }

        //set the own namespace
        OMNamespace namespace = node.resolveNamespace(parser.getNamespaceURI(), parser.getPrefix());

        if (namespace == null) {
            throw new OMException("All elements must be namespace qualified!");
        }

        if (isSOAPElement) {
            if (!namespace.getValue().equals(OMConstants.SOAP_ENVELOPE_NAMESPACE_URI))
                throw new OMBuilderException("invalid SOAP namespace URI");
        }

        node.setNamespace(namespace);
    }

}
