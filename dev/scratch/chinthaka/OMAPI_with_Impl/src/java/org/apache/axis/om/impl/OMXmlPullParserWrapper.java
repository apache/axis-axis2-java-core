package org.apache.axis.om.impl;

import org.apache.axis.om.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
 * Date: Oct 6, 2004
 * Time: 11:42:44 AM
 */
public class OMXmlPullParserWrapper {
    private XmlPullParser parser;
    // private OMElementImpl root;
    private OMDocumentImpl document;
    private OMNodeImpl lastNode;
    private boolean cache = true;
    private boolean slip = false;
    private boolean navigate = false;
    private boolean done = false;
    private OMNavigator navigator = new OMNavigator();

    public OMXmlPullParserWrapper(XmlPullParser parser) {
        this.parser = parser;
    }

//    public OMElementImpl getDocument() throws OMException {
//        if (root == null)
//            next();
//        return root;
//    }
    public OMDocumentImpl getDocument() throws OMException {
        document = new OMDocumentImpl(this);
        return document;
    }

    private OMNode createOMElement() throws OMException {
        OMElementImpl node;
        if (lastNode == null) {
            node = new OMElementImpl(parser.getName(), null, null, this);
            document.setRootElement(node);
//            root = new OMElementImpl(parser.getName(), null, null, this);
//            node = root;
        } else if (lastNode.isComplete()) {
            node = new OMElementImpl(parser.getName(), null, lastNode.getParent(), this);
            lastNode.setNextSibling(node);
            node.setPreviousSibling(lastNode);
        } else {
            System.out.println("lastNode = " + lastNode.getClass());
            OMElementImpl e = (OMElementImpl) lastNode;
            node = new OMElementImpl(parser.getName(), null, (OMElement) lastNode, this);
            e.setFirstChild(node);
        }

        int i, j;
        try {
            j = parser.getNamespaceCount(parser.getDepth());
            i = 0;
            if (j > 1)
                i = parser.getNamespaceCount(parser.getDepth() - 1);
            while (i < j) {
                node.createNamespace(parser.getNamespaceUri(i), parser.getNamespacePrefix(i));
                i++;
            }
        } catch (XmlPullParserException e) {
            throw new OMException(e);
        }

        node.setNamespace(node.resolveNamespace(parser.getNamespace(), parser.getPrefix()));

        j = parser.getAttributeCount();
        for (i = 0; i < j; i++) {
            OMNamespace ns = null;
            String uri = parser.getAttributeNamespace(i);
            if (uri.hashCode() != 0)
                ns = node.resolveNamespace(uri, parser.getAttributePrefix(i));
            node.insertAttribute(new OMAttributeImpl(parser.getAttributeName(i), ns, parser.getAttributeValue(i), node));
        }

        return node;
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
        navigate = true;
        lastNode = null;
        navigator.init(node);
    }

    
    public int next() throws OMException {
        try {
            if (navigate) {
                OMNodeImpl next = (OMNodeImpl) navigator.next();
                if (next != null) {
                    lastNode = next;
                    if (lastNode instanceof OMText)
                        return XmlPullParser.TEXT;
                    else if (navigator.visited())
                        return XmlPullParser.END_TAG;
                    else
                        return XmlPullParser.START_TAG;
                }
                navigate = false;
                if (done)
                    return XmlPullParser.END_DOCUMENT;
                if (slip)
                    throw new OMException();
            }

            if (done)
                throw new OMException();

            int token = parser.nextToken();

            if (!cache) {
                slip = true;
                return token;
            }

            switch (token) {
                case XmlPullParser.START_TAG:
                    lastNode = (OMNodeImpl) createOMElement();
                    break;

                case XmlPullParser.TEXT:
                    lastNode = (OMNodeImpl) createOMText();
                    break;

                case XmlPullParser.END_TAG:
                    if (lastNode.isComplete()) {
                        OMElement parent = lastNode.getParent();
                        parent.setComplete(true);
                        lastNode = (OMNodeImpl) parent;
                    } else {
                        OMElement e = (OMElement) lastNode;
                        e.setComplete(true);
                    }
                    break;

                case XmlPullParser.END_DOCUMENT:
                    done = true;
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

    public void discard(OMElementImpl el) throws OMException {
        if (el.isComplete() || !cache)
            throw new OMException();
        try {
            cache = false;
            do {
                while (parser.next() != XmlPullParser.END_TAG) ;
                //	TODO:
            } while (!parser.getName().equals(el.getLocalName()));
            lastNode = (OMNodeImpl) el.getPreviousSibling();
            if (lastNode != null)
                lastNode.setNextSibling(null);
            else {
                OMElementImpl parent = (OMElementImpl) el.getParent();
                if (parent == null)
                    throw new OMException();
                parent.setFirstChild(null);
                lastNode = parent;
            }
            slip = false;
            cache = true;
        } catch (OMException e) {
            throw e;
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    public void setCache(boolean b) {
        cache = b;
    }

    public String getName() throws OMException {
        if (navigate) {
            try {
                OMElement e = (OMElement) lastNode;
                return e.getLocalName();
            } catch (Exception e) {
                throw new OMException(e);
            }
        }
        return parser.getName();
    }

    public String getText() throws OMException {
        if (navigate) {
            try {
                return (String) lastNode.getValue();
            } catch (Exception e) {
                throw new OMException(e);
            }
        }
        return parser.getText();
    }

    public String getNamespace() throws OMException {
        if (navigate) {
            if (lastNode instanceof OMElement) {
                OMElement node = (OMElement) lastNode;
                OMNamespace ns = node.getNamespace();
                if (ns != null)
                    return ns.getValue();
                //	TODO: else
            }
            throw new OMException();
        }
        return parser.getNamespace();
    }

    public int getNamespaceCount(int arg) throws OMException {
        if (navigate)
        //	TODO:
            throw new OMException();
        try {
            return parser.getNamespaceCount(arg);
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    public String getNamespacePrefix(int arg) throws OMException {
        if (navigate)
        //	TODO:
            throw new OMException();
        try {
            return parser.getNamespacePrefix(arg);
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    public String getNamespaceUri(int arg) throws OMException {
        if (navigate)
        //	TODO:
            throw new OMException();
        try {
            return parser.getNamespaceUri(arg);
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    public String getNamespace(String arg) throws OMException {
        if (navigate)
        //	TODO:
            throw new OMException();
        try {
            return parser.getNamespace(arg);
        } catch (Exception e) {
            throw new OMException(e);
        }
    }

    public String getPrefix() throws OMException {
        if (navigate)
        //	TODO:
            throw new OMException();
        return parser.getPrefix();
    }

    public int getAttributeCount() throws OMException {
        if (navigate)
        //	TODO:
            throw new OMException();
        return parser.getAttributeCount();
    }

    public String getAttributeNamespace(int arg) throws OMException {
        if (navigate)
        //	TODO:
            throw new OMException();
        return parser.getAttributeNamespace(arg);
    }

    public String getAttributeName(int arg) throws OMException {
        if (navigate)
        //	TODO:
            throw new OMException();
        return parser.getAttributeNamespace(arg);
    }

    public String getAttributePrefix(int arg) throws OMException {
        if (navigate)
        //	TODO:
            throw new OMException();
        return parser.getAttributeNamespace(arg);
    }
}
