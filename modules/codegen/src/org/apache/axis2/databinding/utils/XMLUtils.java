/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis2.databinding.utils;

//import org.apache.axis.AxisEngine;
//import org.apache.axis.Constants;
//import org.apache.axis.InternalException;
//import org.apache.axis.Message;
//import org.apache.axis.MessageContext;
//import org.apache.axis.AxisProperties;
//import org.apache.axis.components.encoding.XMLEncoder;
//import org.apache.axis.components.encoding.XMLEncoderFactory;
//import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.databinding.Constants;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
//import javax.xml.soap.SOAPException;
//import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;


public class XMLUtils {
    protected static Log log =
        LogFactory.getLog(XMLUtils.class.getName());

    public static final String httpAuthCharEncoding = "ISO-8859-1";
    private static final String saxParserFactoryProperty =
        "javax.xml.parsers.SAXParserFactory";

//    private static DocumentBuilderFactory dbf = getDOMFactory();
    private static SAXParserFactory       saxFactory;
    private static Stack                  saxParsers = new Stack();
    private static DefaultHandler doNothingContentHandler = new DefaultHandler();

    private static String EMPTY = "";
    private static ByteArrayInputStream bais = new ByteArrayInputStream(EMPTY.getBytes());

    private static boolean tryReset= true;

    protected static boolean enableParserReuse = false;

//    private static class ThreadLocalDocumentBuilder extends ThreadLocal {
//        protected Object initialValue() {
//            try {
//                return getDOMFactory().newDocumentBuilder();
//            } catch (ParserConfigurationException e) {
////                log.error(Messages.getMessage("parserConfigurationException00"),
////                        e);
//            }
//            return null;
//        }
//    }
//    private static ThreadLocalDocumentBuilder documentBuilder = new ThreadLocalDocumentBuilder();
//
//    static {
//        // Initialize SAX Parser factory defaults
//        initSAXFactory(null, true, false);
//
//        String value = AxisProperties.getProperty(AxisEngine.PROP_XML_REUSE_SAX_PARSERS,
//                "" + false);
//        if (value.equalsIgnoreCase("true") ||
//                value.equals("1") ||
//                value.equalsIgnoreCase("yes")) {
//            enableParserReuse = true;
//        } else {
//            enableParserReuse = false;
//        }
//    }

//    /**
//     * Encode a string appropriately for XML.
//     * @param orig the String to encode
//     * @return a String in which XML special chars are repalced by entities
//     */
//    public static String xmlEncodeString(String orig)
//    {
//        XMLEncoder encoder = getXMLEncoder(MessageContext.getCurrentContext());
//        return encoder.encode(orig);
//    }
//
//    /**
//     * Get the current XMLEncoder
//     * @return XMLEncoder
//     */
//    public static XMLEncoder getXMLEncoder(MessageContext msgContext) {
//        return getXMLEncoder(getEncoding(null, msgContext));
//    }
//
//    /**
//     * Get the XMLEncoder for specific encoding
//     * @return XMLEncoder
//     */
//    public static XMLEncoder getXMLEncoder(String encoding) {
//        XMLEncoder encoder = null;
//        try {
//            encoder = XMLEncoderFactory.getEncoder(encoding);
//        } catch (Exception e) {
//            log.error(Messages.getMessage("exception00"), e);
//            encoder = XMLEncoderFactory.getDefaultEncoder();
//        }
//        return encoder;
//    }
//
//    /**
//     * Get the current encoding in effect
//     * @return string
//     */
//    public static String getEncoding(MessageContext msgContext) {
//        XMLEncoder encoder = getXMLEncoder(msgContext);
//        return encoder.getEncoding();
//    }
//
//    /**
//     * Get the current encoding in effect
//     * @return string
//     */
//    public static String getEncoding() {
//        XMLEncoder encoder = getXMLEncoder(MessageContext.getCurrentContext());
//        return encoder.getEncoding();
//    }
//
//    /** Initialize the SAX parser factory.
//     *
//     * @param factoryClassName The (optional) class name of the desired
//     *                         SAXParserFactory implementation. Will be
//     *                         assigned to the system property
//     *                         <b>javax.xml.parsers.SAXParserFactory</b>
//     *                         unless this property is already set.
//     *                         If <code>null</code>, leaves current setting
//     *                         alone.
//     * @param namespaceAware true if we want a namespace-aware parser
//     * @param validating true if we want a validating parser
//     *
//     */
//    public static void initSAXFactory(String factoryClassName,
//                                      boolean namespaceAware,
//                                      boolean validating)
//    {
//        if (factoryClassName != null) {
//            try {
//                saxFactory = (SAXParserFactory)Class.forName(factoryClassName).
//                    newInstance();
//                /*
//                 * Set the system property only if it is not already set to
//                 * avoid corrupting environments in which Axis is embedded.
//                 */
//                if (System.getProperty(saxParserFactoryProperty) == null) {
//                    System.setProperty(saxParserFactoryProperty,
//                                       factoryClassName);
//                }
//            } catch (Exception e) {
//                log.error(Messages.getMessage("exception00"), e);
//                saxFactory = null;
//            }
//       } else {
//            saxFactory = SAXParserFactory.newInstance();
//        }
//        saxFactory.setNamespaceAware(namespaceAware);
//        saxFactory.setValidating(validating);
//
//        // Discard existing parsers
//        saxParsers.clear();
//    }

//    private static DocumentBuilderFactory getDOMFactory() {
//        DocumentBuilderFactory dbf;
//        try {
//            dbf = DocumentBuilderFactory.newInstance();
//            dbf.setNamespaceAware(true);
//        }
//        catch( Exception e ) {
//            log.error(Messages.getMessage("exception00"), e );
//            dbf = null;
//        }
//        return( dbf );
//    }

//    /**
//     * Gets a DocumentBuilder
//     * @return DocumentBuilder
//     * @throws ParserConfigurationException
//     */
//    public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
//        return (DocumentBuilder) documentBuilder.get();
//    }

    /**
     * Releases a DocumentBuilder
     * @param db
     */
    public static void releaseDocumentBuilder(DocumentBuilder db) {
        try {
            db.setErrorHandler(null); // setting implementation default
        } catch (Throwable t) {
            log.debug("Failed to set ErrorHandler to null on DocumentBuilder",
                    t);
        }
        try {
            db.setEntityResolver(null); // setting implementation default
        } catch (Throwable t) {
            log.debug("Failed to set EntityResolver to null on DocumentBuilder",
                    t);
        }
    }

//    /** Get a SAX parser instance from the JAXP factory.
//     *
//     * @return a SAXParser instance.
//     */
//    public static synchronized SAXParser getSAXParser() {
//        if(enableParserReuse && !saxParsers.empty()) {
//            return (SAXParser )saxParsers.pop();
//        }
//
//        try {
//            SAXParser parser = saxFactory.newSAXParser();
//            XMLReader reader = parser.getXMLReader();
//            // parser.getParser().setEntityResolver(new DefaultEntityResolver());
//            // The above commented line and the following line are added
//            // for preventing XXE (bug #14105).
//            // We may need to uncomment the deprecated setting
//            // in case that it is considered necessary.
//            try {
//                reader.setEntityResolver(new DefaultEntityResolver());
//            } catch (Throwable t) {
//                log.debug("Failed to set EntityResolver on DocumentBuilder", t);
//            }
//            reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
//            return parser;
//        } catch (ParserConfigurationException e) {
//            log.error(Messages.getMessage("parserConfigurationException00"), e);
//            return null;
//        } catch (SAXException se) {
//            log.error(Messages.getMessage("SAXException00"), se);
//            return null;
//        }
//    }


    /** Return a SAX parser for reuse.
     * @param parser A SAX parser that is available for reuse
     */
    public static void releaseSAXParser(SAXParser parser) {
        if(!tryReset || !enableParserReuse) return;

        //Free up possible ref. held by past contenthandler.
        try{
            XMLReader xmlReader= parser.getXMLReader();
            if(null != xmlReader){
                xmlReader.setContentHandler(doNothingContentHandler);
                xmlReader.setDTDHandler(doNothingContentHandler);
                try {
                    xmlReader.setEntityResolver(doNothingContentHandler);
                } catch (Throwable t) {
                    log.debug("Failed to set EntityResolver on DocumentBuilder", t);
                }
                try {
                    xmlReader.setErrorHandler(doNothingContentHandler);
                } catch (Throwable t) {
                    log.debug("Failed to set ErrorHandler on DocumentBuilder", t);
                }

                synchronized (XMLUtils.class ) {
                    saxParsers.push(parser);
                }
            }
            else {
                tryReset= false;
            }
        } catch (org.xml.sax.SAXException e) {
            tryReset= false;
        }
    }
//    /**
//     * Get an empty new Document
//     *
//     * @return Document
//     * @throws ParserConfigurationException if construction problems occur
//     */
//    public static Document newDocument()
//            throws ParserConfigurationException {
//        DocumentBuilder db = null;
//        try {
//            db = getDocumentBuilder();
//            Document doc = db.newDocument();
//            return doc;
//        } finally {
//            if (db != null) {
//                releaseDocumentBuilder(db);
//            }
//        }
//    }

//    /**
//     * Get a new Document read from the input source
//     * @return Document
//     * @throws ParserConfigurationException if construction problems occur
//     * @throws SAXException if the document has xml sax problems
//     * @throws IOException if i/o exceptions occur
//     */
//    public static Document newDocument(InputSource inp)
//            throws ParserConfigurationException, SAXException, IOException {
//        DocumentBuilder db = null;
//        try {
//            db = getDocumentBuilder();
//            try {
//                db.setEntityResolver(new DefaultEntityResolver());
//            } catch (Throwable t) {
//                log.debug("Failed to set EntityResolver on DocumentBuilder", t);
//            }
//            try {
//                db.setErrorHandler(new XMLUtils.ParserErrorHandler());
//            } catch (Throwable t) {
//                log.debug("Failed to set ErrorHandler on DocumentBuilder", t);
//            }
//            Document doc = db.parse(inp);
//            return doc;
//        } finally {
//            if (db != null) {
//                releaseDocumentBuilder(db);
//            }
//        }
//    }

    /**
     * Get a new Document read from the input stream
     * @return Document
     * @throws ParserConfigurationException if construction problems occur
     * @throws SAXException if the document has xml sax problems
     * @throws IOException if i/o exceptions occur
     */
//    public static Document newDocument(InputStream inp)
//        throws ParserConfigurationException, SAXException, IOException
//    {
//        return XMLUtils.newDocument(new InputSource(inp));
//    }

    /**
     * Get a new Document read from the indicated uri
     * @return Document
     * @throws ParserConfigurationException if construction problems occur
     * @throws SAXException if the document has xml sax problems
     * @throws IOException if i/o exceptions occur
     */
//    public static Document newDocument(String uri)
//        throws ParserConfigurationException, SAXException, IOException
//    {
//        // call the authenticated version as there might be
//        // username/password info embeded in the uri.
//        return XMLUtils.newDocument(uri, null, null);
//    }

//    /**
//     * Create a new document from the given URI, use the username and password
//     * if the URI requires authentication.
//     * @param uri the resource to get
//     * @param username basic auth username
//     * @param password basic auth password
//     * @throws ParserConfigurationException if construction problems occur
//     * @throws SAXException if the document has xml sax problems
//     * @throws IOException if i/o exceptions occur
//     */
//    public static Document newDocument(String uri, String username, String password)
//        throws ParserConfigurationException, SAXException, IOException
//     {
//         InputSource ins = XMLUtils.getInputSourceFromURI(uri, username, password);
//         Document doc = XMLUtils.newDocument(ins);
//         // Close the Stream
//         if (ins.getByteStream() != null) {
//             ins.getByteStream().close();
//         } else if (ins.getCharacterStream() != null) {
//             ins.getCharacterStream().close();
//         }
//         return doc;
//     }

//    private static String privateElementToString(Element element,
//                                                 boolean omitXMLDecl)
//    {
//        return DOM2Writer.nodeToString(element, omitXMLDecl);
//    }

//    /**
//     * turn an element into an XML fragment
//     * @param element
//     * @return stringified element
//     */
//    public static String ElementToString(Element element) {
//        return privateElementToString(element, true);
//    }

//    /**
//     * turn a whole DOM document into XML
//     * @param doc DOM document
//     * @return string representation of the document, including XML declaration
//     */
//    public static String DocumentToString(Document doc) {
//        return privateElementToString(doc.getDocumentElement(), false);
//    }

//    public static String PrettyDocumentToString(Document doc) {
//        StringWriter sw = new StringWriter();
//        PrettyElementToWriter(doc.getDocumentElement(), sw);
//        return sw.toString();
//    }

//    public static void privateElementToWriter(Element element, Writer writer,
//                                              boolean omitXMLDecl,
//                                              boolean pretty) {
//        DOM2Writer.serializeAsXML(element, writer, omitXMLDecl, pretty);
//    }

//    public static void ElementToStream(Element element, OutputStream out) {
//        Writer writer = getWriter(out);
//        privateElementToWriter(element, writer, true, false);
//    }
//
//    public static void PrettyElementToStream(Element element, OutputStream out) {
//        Writer writer = getWriter(out);
//        privateElementToWriter(element, writer, true, true);
//    }
//
//    public static void ElementToWriter(Element element, Writer writer) {
//        privateElementToWriter(element, writer, true, false);
//    }
//
//    public static void PrettyElementToWriter(Element element, Writer writer) {
//        privateElementToWriter(element, writer, true, true);
//    }
//
//    public static void DocumentToStream(Document doc, OutputStream out) {
//        Writer writer = getWriter(out);
//        privateElementToWriter(doc.getDocumentElement(), writer, false, false);
//    }
//
//    public static void PrettyDocumentToStream(Document doc, OutputStream out) {
//        Writer writer = getWriter(out);
//        privateElementToWriter(doc.getDocumentElement(), writer, false, true);
//    }

    private static Writer getWriter(OutputStream os) {
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(os, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            //log.error(Messages.getMessage("exception00"), uee);
            writer = new OutputStreamWriter(os);
        }
        return writer;
    }

//    public static void DocumentToWriter(Document doc, Writer writer) {
//        privateElementToWriter(doc.getDocumentElement(), writer, false, false);
//    }
//
//    public static void PrettyDocumentToWriter(Document doc, Writer writer) {
//        privateElementToWriter(doc.getDocumentElement(), writer, false, true);
//    }
//    /**
//     * Convert a simple string to an element with a text node
//     *
//     * @param namespace - element namespace
//     * @param name - element name
//     * @param string - value of the text node
//     * @return element - an XML Element, null if no element was created
//     */
//    public static Element StringToElement(String namespace, String name, String string) {
//        try {
//            Document doc = XMLUtils.newDocument();
//            Element element = doc.createElementNS(namespace, name);
//            Text text = doc.createTextNode(string);
//            element.appendChild(text);
//            return element;
//        }
//        catch (ParserConfigurationException e) {
//            // This should not occur
//            throw new UnsupportedOperationException(e.getMessage());
//        }
//    }

//    /**
//     * get the inner XML inside an element as a string. This is done by
//     * converting the XML to its string representation, then extracting the
//     * subset between beginning and end tags.
//     * @param element
//     * @return textual body of the element, or null for no inner body
//     */
//    public static String getInnerXMLString(Element element) {
//        String elementString = ElementToString(element);
//        int start, end;
//        start = elementString.indexOf(">") + 1;
//        end = elementString.lastIndexOf("</");
//        if (end > 0)
//            return elementString.substring(start,end);
//        else
//            return null;
//    }

    public static String getPrefix(String uri, Node e) {
        while (e != null && (e.getNodeType() == Element.ELEMENT_NODE)) {
            NamedNodeMap attrs = e.getAttributes();
            for (int n = 0; n < attrs.getLength(); n++) {
                Attr a = (Attr)attrs.item(n);
                String name;
                if ((name = a.getName()).startsWith("xmlns:") &&
                    a.getNodeValue().equals(uri)) {
                    return name.substring(6);
                }
            }
            e = e.getParentNode();
        }
        return null;
    }

    /**
     * Searches for the namespace URI of the given prefix in the given DOM range.
     *
     * The namespace is not searched in parent of the "stopNode". This is
     * usefull to get all the needed namespaces when you need to ouput only a
     * subtree of a DOM document.
     *
     * @param prefix the prefix to find
     * @param e the starting node
     * @param stopNode null to search in all the document or a parent node where the search must stop.
     * @return null if no namespace is found, or the namespace URI.
     */
    public static String getNamespace(String prefix, Node e, Node stopNode) {
        while (e != null && (e.getNodeType() == Node.ELEMENT_NODE)) {
            Attr attr = null;
            if (prefix == null) {
                attr = ((Element) e).getAttributeNode("xmlns");
            } else {
                attr = ((Element) e).getAttributeNodeNS(Constants.NS_URI_XMLNS,
                        prefix);
            }
            if (attr != null) return attr.getValue();
            if (e == stopNode)
                return null;
            e = e.getParentNode();
        }
        return null;
    }

    public static String getNamespace(String prefix, Node e) {
        return getNamespace(prefix, e, null);
    }

    /**
     * Return a QName when passed a string like "foo:bar" by mapping
     * the "foo" prefix to a namespace in the context of the given Node.
     *
     * @return a QName generated from the given string representation
     */
    public static QName getQNameFromString(String str, Node e) {
        return getQNameFromString(str, e, false);
    }
    /**
     * Return a QName when passed a string like "foo:bar" by mapping
     * the "foo" prefix to a namespace in the context of the given Node.
     * If default namespace is found it is returned as part of the QName.
     *
     * @return a QName generated from the given string representation
     */
    public static QName getFullQNameFromString(String str, Node e) {
        return getQNameFromString(str, e, true);
    }
    private static QName getQNameFromString(String str, Node e, boolean defaultNS) {
        if (str == null || e == null)
            return null;

        int idx = str.indexOf(':');
        if (idx > -1) {
            String prefix = str.substring(0, idx);
            String ns = getNamespace(prefix, e);
            if (ns == null)
                return null;
            return new QName(ns, str.substring(idx + 1));
        } else {
            if (defaultNS) {
                String ns = getNamespace(null, e);
                if (ns != null)
                    return new QName(ns, str);
            }
            return new QName("", str);
        }
    }

    /**
     * Return a string for a particular QName, mapping a new prefix
     * if necessary.
     */
    public static String getStringForQName(QName qname, Element e)
    {
        String uri = qname.getNamespaceURI();
        String prefix = getPrefix(uri, e);
        if (prefix == null) {
            int i = 1;
            prefix = "ns" + i;
            while (getNamespace(prefix, e) != null) {
                i++;
                prefix = "ns" + i;
            }
            e.setAttributeNS(Constants.NS_URI_XMLNS,
                        "xmlns:" + prefix, uri);
        }
        return prefix + ":" + qname.getLocalPart();
    }

  /**
   * Concat all the text and cdata node children of this elem and return
   * the resulting text.
   * (by Matt Duftler)
   *
   * @param parentEl the element whose cdata/text node values are to
   *                 be combined.
   * @return the concatanated string.
   */
  public static String getChildCharacterData (Element parentEl) {
    if (parentEl == null) {
      return null;
    }
    Node          tempNode = parentEl.getFirstChild();
    StringBuffer  strBuf   = new StringBuffer();
    CharacterData charData;

    while (tempNode != null) {
      switch (tempNode.getNodeType()) {
        case Node.TEXT_NODE :
        case Node.CDATA_SECTION_NODE : charData = (CharacterData)tempNode;
                                       strBuf.append(charData.getData());
                                       break;
      }
      tempNode = tempNode.getNextSibling();
    }
    return strBuf.toString();
  }

    public static class ParserErrorHandler implements ErrorHandler {
        protected static Log log =
            LogFactory.getLog(ParserErrorHandler.class.getName());
        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId +
                " Line=" + spe.getLineNumber() +
                ": " + spe.getMessage();
            return info;
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
            if (log.isDebugEnabled()) {}
                //log.debug( Messages.getMessage("warning00", getParseExceptionInfo(spe)));
        }

        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }


    /**
     * Utility to get the bytes uri.
     * Does NOT handle authenticated URLs,
     * use getInputSourceFromURI(uri, username, password)
     *
     * @param uri the resource to get
     * @see #getInputSourceFromURI(String uri, String username, String password)
     */
    public static InputSource getInputSourceFromURI(String uri) {
        return new InputSource(uri);
    }

//    /**
//     * Utility to get the bytes uri
//     *
//     * @param source the resource to get
//     */
//    public static InputSource sourceToInputSource(Source source) {
//        if (source instanceof SAXSource) {
//            return ((SAXSource) source).getInputSource();
//        } else if (source instanceof DOMSource) {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            Node node = ((DOMSource)source).getNode();
//            if (node instanceof Document) {
//                node = ((Document)node).getDocumentElement();
//            }
//            Element domElement = (Element)node;
//            ElementToStream(domElement, baos);
//            InputSource  isource = new InputSource(source.getSystemId());
//            isource.setByteStream(new ByteArrayInputStream(baos.toByteArray()));
//            return isource;
//        } else if (source instanceof StreamSource) {
//            StreamSource ss      = (StreamSource) source;
//            InputSource  isource = new InputSource(ss.getSystemId());
//            isource.setByteStream(ss.getInputStream());
//            isource.setCharacterStream(ss.getReader());
//            isource.setPublicId(ss.getPublicId());
//            return isource;
//        } else {
//            return getInputSourceFromURI(source.getSystemId());
//        }
//    }

    /**
     * Utility to get the bytes at a protected uri
     *
     * This will retrieve the URL if a username and password are provided.
     * The java.net.URL class does not do Basic Authentication, so we have to
     * do it manually in this routine.
     *
     * If no username is provided, we create an InputSource from the uri
     * and let the InputSource go fetch the contents.
     *
     * @param uri the resource to get
     * @param username basic auth username
     * @param password basic auth password
     */
    private static InputSource getInputSourceFromURI(String uri,
                                                     String username,
                                                     String password)
        throws IOException, ProtocolException, UnsupportedEncodingException
    {
        URL wsdlurl = null;
        try {
            wsdlurl = new URL(uri);
        } catch (MalformedURLException e) {
            // we can't process it, it might be a 'simple' foo.wsdl
            // let InputSource deal with it
            return new InputSource(uri);
        }

        // if no authentication, just let InputSource deal with it
        if (username == null && wsdlurl.getUserInfo() == null) {
            return new InputSource(uri);
        }

        // if this is not an HTTP{S} url, let InputSource deal with it
        if (!wsdlurl.getProtocol().startsWith("http")) {
            return new InputSource(uri);
        }

        URLConnection connection = wsdlurl.openConnection();
        // Does this work for https???
        if (!(connection instanceof HttpURLConnection)) {
            // can't do http with this URL, let InputSource deal with it
            return new InputSource(uri);
        }
        HttpURLConnection uconn = (HttpURLConnection) connection;
        String userinfo = wsdlurl.getUserInfo();
        uconn.setRequestMethod("GET");
        uconn.setAllowUserInteraction(false);
        uconn.setDefaultUseCaches(false);
        uconn.setDoInput(true);
        uconn.setDoOutput(false);
        uconn.setInstanceFollowRedirects(true);
        uconn.setUseCaches(false);

        // username/password info in the URL overrides passed in values
        String auth = null;
        if (userinfo != null) {
            auth = userinfo;
        } else if (username != null) {
            auth = (password == null) ? username : username + ":" + password;
        }

        if (auth != null) {
            uconn.setRequestProperty("Authorization",
                                     "Basic " +
                                     base64encode(auth.getBytes(httpAuthCharEncoding)));
        }

        uconn.connect();

        return new InputSource(uconn.getInputStream());
    }

    public static final String base64encode(byte[] bytes) {
       // return new String(Base64.encode(bytes));
        return null; //todo
    }

    public static InputSource getEmptyInputSource() {
        return new InputSource(bais);
    }

    /**
     * Find a Node with a given QName
     *
     * @param node parent node
     * @param name QName of the child we need to find
     * @return child node
     */
    public static Node findNode(Node node, QName name){
        if(name.getNamespaceURI().equals(node.getNamespaceURI()) &&
           name.getLocalPart().equals(node.getLocalName()))
            return node;
        NodeList children = node.getChildNodes();
        for(int i=0;i<children.getLength();i++){
            Node ret = findNode(children.item(i), name);
            if(ret != null)
                return ret;
        }
        return null;
    }

    /**
     * Trim all new lines from text nodes.
     *
     * @param node
     */
    public static void normalize(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            String data = ((Text) node).getData();
            if (data.length() > 0) {
                char ch = data.charAt(data.length()-1);
                 if(ch == '\n' || ch == '\r' || ch == ' ') {
                    String data2 = trim(data);
                    ((Text) node).setData(data2);
                 }
            }
        }
        for (Node currentChild = node.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
            normalize(currentChild);
        }
    }

    public static String trim(String str) {
        if (str.length() == 0) {
            return str;
        }

        if (str.length() == 1) {
            if ("\r".equals(str) || "\n".equals(str)) {
                return "";
            } else {
                return str;
            }
        }

        int lastIdx = str.length() - 1;
        char last = str.charAt(lastIdx);
        while(lastIdx > 0) {
            if(last != '\n' && last != '\r' && last != ' ')
                break;
            lastIdx--;
            last = str.charAt(lastIdx);
        }
        if(lastIdx == 0)
            return "";
        return str.substring(0, lastIdx);
    }

    /**
     * Converts a List with org.w3c.dom.Element objects to an Array
     * with org.w3c.dom.Element objects.
     * @param list List containing org.w3c.dom.Element objects
     * @return Element[] Array with org.w3c.dom.Element objects
     */
    public static Element[] asElementArray(List list) {

        Element[] elements = new Element[list.size()];

        int i = 0;
        Iterator detailIter = list.iterator();
        while (detailIter.hasNext()) {
            elements[i++] = (Element) detailIter.next();
        }

        return elements;
    }

//    public static String getEncoding(Message message,
//                                     MessageContext msgContext) {
//        return getEncoding(message, msgContext,
//                XMLEncoderFactory.getDefaultEncoder());
//    }
//
//    public static String getEncoding(Message message,
//                                     MessageContext msgContext,
//                                     XMLEncoder defaultEncoder) {
//        String encoding = null;
//        try {
//            if(message != null) {
//                encoding = (String) message.getProperty(SOAPMessage.CHARACTER_SET_ENCODING);
//            }
//        } catch (SOAPException e) {
//        }
//        if(msgContext == null) {
//            msgContext = MessageContext.getCurrentContext();
//        }
//        if(msgContext != null && encoding == null){
//            encoding = (String) msgContext.getProperty(SOAPMessage.CHARACTER_SET_ENCODING);
//        }
//        if (msgContext != null && encoding == null && msgContext.getAxisEngine() != null) {
//            encoding = (String) msgContext.getAxisEngine().getOption(AxisEngine.PROP_XML_ENCODING);
//        }
//        if (encoding == null && defaultEncoder != null) {
//            encoding = defaultEncoder.getEncoding();
//        }
//        return encoding;
//    }
}
