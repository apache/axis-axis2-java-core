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

package org.apache.axis2.wsdl.util;

import com.ibm.wsdl.Constants;
import org.apache.axis2.attachments.Base64;
import org.w3c.dom.*;
import org.w3c.dom.CharacterData;
import org.xml.sax.*;

import javax.xml.namespace.QName;
import javax.xml.parsers.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Stack;


public class XMLUtils {
//    protected Log log =
//        LogFactory.getLog(getClass().getName());
        
    public static final String charEncoding = "ISO-8859-1";
    private static final String saxParserFactoryProperty =
        "javax.xml.parsers.SAXParserFactory";

    private static DocumentBuilderFactory dbf = getDOMFactory();
    private static SAXParserFactory       saxFactory;
    private static Stack                  saxParsers = new Stack();

    private static String empty = new String("");
    private static ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes());

    static {
        // Initialize SAX Parser factory defaults
        initSAXFactory(null, true, false);
    }

    /** Encode a string appropriately for XML.
     *
     * Lifted from ApacheSOAP 2.2 (org.apache.soap.Utils)
     *
     * @param orig the String to encode
     * @return a String in which XML special chars are repalced by entities
     */
    public static String xmlEncodeString(String orig)
    {
        if (orig == null)
        {
            return "";
        }

        char[] chars = orig.toCharArray();

        // if the string doesn't have any of the magic characters, leave
        // it alone.
        boolean needsEncoding = false;

        search:
        for(int i = 0; i < chars.length; i++) {
            switch(chars[i]) {
            case '&': case '"': case '\'': case '<': case '>':
                needsEncoding = true;
                break search;
            }
        }

        if (!needsEncoding) return orig;

        StringBuffer strBuf = new StringBuffer();
        for (int i = 0; i < chars.length; i++)
        {
            switch (chars[i])
            {
            case '&'  : strBuf.append("&amp;");
                        break;
            case '\"' : strBuf.append("&quot;");
                        break;
            case '\'' : strBuf.append("&apos;");
                        break;
            case '<'  : strBuf.append("&lt;");
                        break;
            case '\r' : strBuf.append("&#xd;");
                        break;
            case '>'  : strBuf.append("&gt;");
                        break;
            default   : 
                if (((int)chars[i]) > 127) {
                        strBuf.append("&#");
                        strBuf.append((int)chars[i]);
                        strBuf.append(";");
                } else {
                        strBuf.append(chars[i]);
                }
            }
        }

        return strBuf.toString();
    }

    /** Initialize the SAX parser factory.
     *
     * @param factoryClassName The (optional) class name of the desired
     *                         SAXParserFactory implementation. Will be
     *                         assigned to the system property
     *                         <b>javax.xml.parsers.SAXParserFactory</b>
     *                         unless this property is already set.
     *                         If <code>null</code>, leaves current setting
     *                         alone.
     * @param namespaceAware true if we want a namespace-aware parser
     * @param validating true if we want a validating parser
     *
     */
    public static void initSAXFactory(String factoryClassName,
                                      boolean namespaceAware,
                                      boolean validating)
    {
        if (factoryClassName != null) {
            try {
                saxFactory = (SAXParserFactory)Class.forName(factoryClassName).
                    newInstance();
                /*
                 * Set the system property only if it is not already set to
                 * avoid corrupting environments in which Axis is embedded.
                 */
                if (System.getProperty(saxParserFactoryProperty) == null) {
                    System.setProperty(saxParserFactoryProperty,
                                       factoryClassName);
                }
            } catch (Exception e) {
                //log.error(Messages.getMessage("exception00"), e);
                saxFactory = null;
            }
       } else {
            saxFactory = SAXParserFactory.newInstance();
        }
        saxFactory.setNamespaceAware(namespaceAware);
        saxFactory.setValidating(validating);

        // Discard existing parsers
        saxParsers.clear();
    }

    private static DocumentBuilderFactory getDOMFactory() {
        DocumentBuilderFactory dbf;
        try {
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
        }
        catch( Exception e ) {
            //log.error(Messages.getMessage("exception00"), e );
            dbf = null;
        }
        return( dbf );
    }
    
    private static boolean tryReset= true;

//    /** Get a SAX parser instance from the JAXP factory.
//     *
//     * @return a SAXParser instance.
//     */
//    public static synchronized SAXParser getSAXParser() {
//        if(!saxParsers.empty()) {
//            return (SAXParser )saxParsers.pop();
//        }
//
//        try {
//            SAXParser parser = saxFactory.newSAXParser();
//            parser.getParser().setEntityResolver(new DefaultEntityResolver());
//            XMLReader reader = parser.getXMLReader(); 
//            reader.setEntityResolver(new DefaultEntityResolver());
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
        if(!tryReset) return;

        //Free up possible ref. held by past contenthandler.
        try{
            XMLReader xmlReader= parser.getXMLReader();
            if(null != xmlReader){
//                xmlReader.setContentHandler(doNothingContentHandler);
//                xmlReader.setDTDHandler(doNothingContentHandler);
//                xmlReader.setEntityResolver(doNothingContentHandler);
//                xmlReader.setErrorHandler(doNothingContentHandler);
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
    /**
     * Get an empty new Document
     * @return Document
     * @throws ParserConfigurationException if construction problems occur
     */
    public static Document newDocument() 
         throws ParserConfigurationException
    {
        synchronized (dbf) {
            return dbf.newDocumentBuilder().newDocument();
        }
    }

    /**
     * Get a new Document read from the input source
     * @return Document
     * @throws ParserConfigurationException if construction problems occur
     * @throws SAXException if the document has xml sax problems
     * @throws IOException if i/o exceptions occur
     */
    public static Document newDocument(InputSource inp)
        throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilder db;
        synchronized (dbf) {
            db = dbf.newDocumentBuilder();
        }
        db.setEntityResolver(new DefaultEntityResolver());
        db.setErrorHandler( new ParserErrorHandler() );
        return( db.parse( inp ) );
    }

    /**
     * Get a new Document read from the input stream
     * @return Document
     * @throws ParserConfigurationException if construction problems occur
     * @throws SAXException if the document has xml sax problems
     * @throws IOException if i/o exceptions occur
     */
    public static Document newDocument(InputStream inp) 
        throws ParserConfigurationException, SAXException, IOException 
    {
        return XMLUtils.newDocument(new InputSource(inp));
    } 

    /**
     * Get a new Document read from the indicated uri
     * @return Document
     * @throws ParserConfigurationException if construction problems occur
     * @throws SAXException if the document has xml sax problems
     * @throws IOException if i/o exceptions occur
     */
    public static Document newDocument(String uri) 
        throws ParserConfigurationException, SAXException, IOException 
    {
        // call the authenticated version as there might be 
        // username/password info embeded in the uri.
        return XMLUtils.newDocument(uri, null, null);
    }
    
    /**
     * Create a new document from the given URI, use the username and password
     * if the URI requires authentication.
     * @param uri the resource to get
     * @param username basic auth username
     * @param password basic auth password
     * @throws ParserConfigurationException if construction problems occur
     * @throws SAXException if the document has xml sax problems
     * @throws IOException if i/o exceptions occur
     */ 
    public static Document newDocument(String uri, String username, String password)
        throws ParserConfigurationException, SAXException, IOException
     {
         InputSource ins = XMLUtils.getInputSourceFromURI(uri, username, password);
         Document doc = XMLUtils.newDocument(ins);
         // Close the Stream
         if (ins.getByteStream() != null) {
             ins.getByteStream().close();
         } else if (ins.getCharacterStream() != null) {
             ins.getCharacterStream().close();
         }
         return doc;
     }



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

    public static String getNamespace(String prefix, Node e) {
        while (e != null && (e.getNodeType() == Node.ELEMENT_NODE)) {
            Attr attr =
                ((Element)e).getAttributeNodeNS(Constants.NS_URI_XMLNS, prefix);
            if (attr != null) return attr.getValue();
            e = e.getParentNode();
        }
        return null;
    }

    /**
     * Return a QName when passed a string like "foo:bar" by mapping
     * the "foo" prefix to a namespace in the context of the given Node.
     *
     * @return a QName generated from the given string representation
     */
    public static QName getQNameFromString(String str, Node e) {
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
    
    public static class ParserErrorHandler implements ErrorHandler
    {
//        protected static Log log =
//            LogFactory.getLog(ParserErrorHandler.class.getName());
        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            return "URI=" + systemId +
                " Line=" + spe.getLineNumber() +
                ": " + spe.getMessage();
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
//            if (log.isDebugEnabled())
//                log.debug( Messages.getMessage("warning00", getParseExceptionInfo(spe)));
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
                                     base64encode(auth.getBytes(charEncoding)));
        }
        
        uconn.connect();

        return new InputSource(uconn.getInputStream());
    }

    public static final String base64encode(byte[] bytes) {
        return new String(Base64.encode(bytes));
    }

    public static InputSource getEmptyInputSource() {
        return new InputSource(bais);
    }
    
    /**
     * Find a Node with a given QNameb
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
}
