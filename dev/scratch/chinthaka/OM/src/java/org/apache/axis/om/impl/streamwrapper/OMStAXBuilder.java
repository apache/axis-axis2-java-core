package org.apache.axis.om.impl.streamwrapper;

import org.apache.axis.om.*;
import org.apache.axis.om.impl.*;
import org.apache.axis.om.impl.factory.OMLinkedListImplFactory;
import org.apache.axis.om.soap.SOAPMessage;
import org.apache.axis.om.soap.SOAPEnvelope;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamConstants;

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
 * Date: Nov 18, 2004
 * Time: 2:30:17 PM
 *
 * Note - OM navigator has been removed to simplify the build process
 */
public class OMStAXBuilder implements OMXMLParserWrapper{



    private OMFactory ombuilderFactory;
    private XMLStreamReader parser;
    private SOAPMessage document;
    private OMNode lastNode;

    //keeps the state of the cache
    private boolean cache = true;
    //keeps the state of the parser access. if the parser is
    //accessed atleast once,this flag will be set
    private boolean parserAccessed = false;

    //returns the state of completion
    private boolean done = false;

    /**
     * element level 1 = envelope level
     * element level 2 = Header or Body level
     * element level 3 = HeaderElement or BodyElement level
     */
    private int elementLevel = 0;

    public OMStAXBuilder(OMFactory ombuilderFactory, XMLStreamReader parser) {
        this.ombuilderFactory = ombuilderFactory;
        this.parser = parser;
    }

    public OMStAXBuilder(XMLStreamReader parser) {
        this.parser = parser;
    }

    public void setOmbuilderFactory(OMFactory ombuilderFactory) {
        this.ombuilderFactory = ombuilderFactory;
    }

    public SOAPMessage getSOAPMessage() throws OMException {
        document = ombuilderFactory.createSOAPMessage(this);
        return document;
    }

    private OMNode createOMElement() throws OMException {
        OMElement node;
        String elementName = parser.getLocalName();

        if (document==null){
            getSOAPMessage();
        }

        if (lastNode == null) {
            node = ombuilderFactory.createSOAPEnvelope(elementName, null, null, this);
            document.setEnvelope((SOAPEnvelope) node);
        } else if (lastNode.isComplete()) {
            node = constructNode(lastNode.getParent(), elementName);
            lastNode.setNextSibling(node);
            node.setPreviousSibling(lastNode);
        } else {
//            System.out.println("lastNode = " + lastNode.getClass());

            OMElement e = (OMElement) lastNode;
            node = constructNode((OMElement) lastNode, elementName);
            e.setFirstChild(node);
        }

        //create the namespaces
        int namespaceCount = parser.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            node.createNamespace(parser.getNamespaceURI(i), parser.getNamespacePrefix(i));
        }

        //set the own namespace
        node.setNamespace(node.resolveNamespace(parser.getNamespaceURI(), parser.getPrefix()));

        //fill in the attributes
        int attribCount = parser.getAttributeCount();
        for (int i = 0; i < attribCount; i++) {
            OMNamespace ns = null;
            String uri = parser.getAttributeNamespace(i);
            if (uri.hashCode() != 0)
                ns = node.resolveNamespace(uri, parser.getAttributePrefix(i));

            node.insertAttribute(ombuilderFactory.createOMAttribute(parser.getAttributeLocalName(i),
                    ns,
                    parser.getAttributeValue(i)));
        }

        return node;
    }

    private OMElement constructNode(OMElement parent, String elementName) {
        OMElement element = null;
        if (elementLevel == 2) {
            // this is either a header or a body
            if (elementName.equalsIgnoreCase("Header")) {
                //since its level 2 parent MUST be the envelope
                element = ombuilderFactory.createHeader(elementName,null,parent,this);
            } else if (elementName.equalsIgnoreCase("Body")) {
                //since its level 2 parent MUST be the envelope
                element = ombuilderFactory.createSOAPBody(elementName,null,parent,this);
            } else {
                // can there be Elements other than Header and Body in Envelope. If yes, what are they and is it YAGNI ??
                throw new OMException(elementName + " is not supported here. Envelope can not have elements other than Header and Body.");
            }

        } else if (elementLevel == 3) {
            // this is either a headerelement or a bodyelement
            if (parent.getLocalName().equalsIgnoreCase("Header")) {
                element = ombuilderFactory.createSOAPHeaderElement(elementName,null,parent,this);//todo NS is required here
            } else if (parent.getLocalName().equalsIgnoreCase("Body")) {
                element = ombuilderFactory.createSOAPBodyElement(elementName,null,parent,this);//todo put the NS here
            } else {
                // can there be Elements other than Header and Body in Envelope. If yes, what are they and is it YAGNI ??
                throw new OMException(elementName + " is not supported here. Envelope can not have elements other than Header and Body.");
            }

        } else {
            // this is neither of above. Just create an element
            element = ombuilderFactory.createOMElement(elementName,null,parent,this);//todo put the name
        }

        return element;
    }

    private OMNode createOMText() throws OMException {
        if (lastNode == null)
            throw new OMException();
        OMNodeImpl node;
        if (lastNode.isComplete()) {
            node = new OMTextImpl(lastNode.getParent(), parser.getText());
            lastNode.setNextSibling(node);
            node.setPreviousSibling(lastNode);
        } else {
            OMElementImpl e = (OMElementImpl) lastNode;
            node = new OMTextImpl(e, parser.getText());
            e.setFirstChild(node);
        }
        return node;
    }

    public void reset(OMNode node) throws OMException {
        lastNode = null;
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

    public void discard(OMElement el) throws OMException {

        OMElementImpl elementImpl = null;
        if (el instanceof OMElementImpl) {
            elementImpl = (OMElementImpl) el;
        } else {
            throw new OMException();
        }

        if (elementImpl.isComplete() || !cache)
            throw new OMException();
        try {
            cache = false;
            do {
                while (parser.next() != XMLStreamConstants.END_ELEMENT) ;
                //	TODO:
            } while (!parser.getName().equals(elementImpl.getLocalName()));
            lastNode = (OMNodeImpl) elementImpl.getPreviousSibling();
            if (lastNode != null)
                lastNode.setNextSibling(null);
            else {
                OMElement parent = elementImpl.getParent();
                if (parent == null)
                    throw new OMException();
                parent.setFirstChild(null);
                lastNode = parent;
            }
           cache = true;
        } catch (OMException e) {
            throw e;
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    public void setCache(boolean b) {
        if (parserAccessed && b)
            throw new UnsupportedOperationException("parser accessed. cannot set cache");
        cache = b;
    }

    public String getName() throws OMException {
        return parser.getLocalName();
    }

    public String getText() throws OMException {
        return parser.getText();
    }

    public String getNamespace() throws OMException {
        return parser.getNamespaceURI();
    }

    public int getNamespaceCount() throws OMException {
        try {
            return parser.getNamespaceCount();
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    public String getNamespacePrefix(int index) throws OMException {
        try {
            return parser.getNamespacePrefix(index);
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    public String getNamespaceUri(int index) throws OMException {
        try {
            return parser.getNamespaceURI(index);
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

//    public String getNamespace(String arg) throws OMException {
//        try {
//            return parser.getNamespaceU(arg);
//        } catch (Exception e) {
//            throw new OMException(e);
//        }
//    }

    public String getPrefix() throws OMException {
        return parser.getPrefix();
    }

    public int getAttributeCount() throws OMException {
        return parser.getAttributeCount();
    }

    public String getAttributeNamespace(int arg) throws OMException {
        return parser.getAttributeNamespace(arg);
    }

    public String getAttributeName(int arg) throws OMException {
        return parser.getAttributeNamespace(arg);
    }

    public String getAttributePrefix(int arg) throws OMException {
        return parser.getAttributeNamespace(arg);
    }

    public Object getParser() {
        if (!cache){
            parserAccessed=true;
            return parser;
        }else{
            throw new UnsupportedOperationException("cache must be switched off to access the parser");
        }
    }

    public boolean isCompleted() {
        return done;
    }
}
