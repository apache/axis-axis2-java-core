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

package org.apache.axis.transport.http;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;

import org.apache.axis.core.AxisEngine;
import org.apache.axis.core.context.MessageContext;
import org.apache.axis.core.registry.EngineRegistry;
import org.apache.axis.encoding.Base64;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.impl.OMXMLPullParserWrapper;
import org.apache.axis.om.impl.SOAPMessageImpl;
import org.apache.axis.transport.TransportSender;
import org.apache.axis.utils.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;



public class SimpleAxisWorker implements Runnable {
    protected static Log log =
            LogFactory.getLog(SimpleAxisWorker.class.getName());

    private AxisEngine engine;
    private SimpleAxisServer server;
    private Socket socket;

    // Axis specific constants
    private static String transportName = "SimpleHTTP";

    // HTTP status codes
    private static byte OK[] = ("200 " + Messages.getMessage("ok00")).getBytes();
    private static byte NOCONTENT[] = ("202 " + Messages.getMessage("ok00") + "\n\n").getBytes();
    private static byte UNAUTH[] = ("401 " + Messages.getMessage("unauth00")).getBytes();
    private static byte SENDER[] = "400".getBytes();
    private static byte ISE[] = ("500 " + Messages.getMessage("internalError01")).getBytes();

    // HTTP prefix
    private static byte HTTP[] = "HTTP/1.0 ".getBytes();

    // Standard MIME headers for XML payload
    private static byte XML_MIME_STUFF[] =
            ("\r\nContent-Type: text/xml; charset=utf-8\r\n" +
            "Content-Length: ").getBytes();

    // Standard MIME headers for HTML payload
    private static byte HTML_MIME_STUFF[] =
            ("\r\nContent-Type: text/html; charset=utf-8\r\n" +
            "Content-Length: ").getBytes();

    // Mime/Content separator
    private static byte SEPARATOR[] = "\r\n\r\n".getBytes();

    // Tiddly little response
//    private static final String responseStr =
//            "<html><head><title>SimpleAxisServer</title></head>" +
//            "<body><h1>SimpleAxisServer</h1>" +
//            Messages.getMessage("reachedServer00") +
//            "</html>";
//    private static byte cannedHTMLResponse[] = responseStr.getBytes();

    // ASCII character mapping to lower case
    private static final byte[] toLower = new byte[256];

    static {
        for (int i = 0; i < 256; i++) {
            toLower[i] = (byte) i;
        }

        for (int lc = 'a'; lc <= 'z'; lc++) {
            toLower[lc + 'A' - 'a'] = (byte) lc;
        }
    }

    // buffer for IO
    private static final int BUFSIZ = 4096;

    // mime header for content length
    private static final byte lenHeader[] = "content-length: ".getBytes();
    private static final int lenLen = lenHeader.length;

    // mime header for content type
    private static final byte typeHeader[] = (HTTPConstants.HEADER_CONTENT_TYPE.toLowerCase() + ": ").getBytes();
    private static final int typeLen = typeHeader.length;

    // mime header for content location
    private static final byte locationHeader[] = (HTTPConstants.HEADER_CONTENT_LOCATION.toLowerCase() + ": ").getBytes();
    private static final int locationLen = locationHeader.length;

    // mime header for soap action
    private static final byte actionHeader[] = "soapaction: ".getBytes();
    private static final int actionLen = actionHeader.length;

    // mime header for cookie
    private static final byte cookieHeader[] = "cookie: ".getBytes();
    private static final int cookieLen = cookieHeader.length;

    // mime header for cookie2
    private static final byte cookie2Header[] = "cookie2: ".getBytes();
    private static final int cookie2Len = cookie2Header.length;

    // HTTP header for authentication
    private static final byte authHeader[] = "authorization: ".getBytes();
    private static final int authLen = authHeader.length;

    // mime header for GET
    private static final byte getHeader[] = "GET".getBytes();

    // mime header for POST
    private static final byte postHeader[] = "POST".getBytes();

    // header ender
    private static final byte headerEnder[] = ": ".getBytes();

    // "Basic" auth string
    private static final byte basicAuth[] = "basic ".getBytes();

    public SimpleAxisWorker(SimpleAxisServer server, Socket socket,AxisEngine engine) {
        this.server = server;
        this.socket = socket;
        this.engine = engine;
    }

    /**
     * Run method
     */ 
    public void run() {
        try {
            execute();
        } finally {
        }
    }
    
    /**
     * The main workhorse method.
     */
    public void execute () {
        byte buf[] = new byte[BUFSIZ];
        // create an Axis server

        MessageContext msgContext = new MessageContext(engine.getRegistry());


        // Reusuable, buffered, content length controlled, InputStream
        NonBlockingBufferedInputStream is =
                new NonBlockingBufferedInputStream();

        // buffers for the headers we care about
        StringBuffer soapAction = new StringBuffer();
        StringBuffer httpRequest = new StringBuffer();
        StringBuffer fileName = new StringBuffer();
        StringBuffer cookie = new StringBuffer();
        StringBuffer cookie2 = new StringBuffer();
        StringBuffer authInfo = new StringBuffer();
        StringBuffer contentType = new StringBuffer();
        StringBuffer contentLocation = new StringBuffer();


        try {
            // assume the best
            byte[] status = OK;

            // assume we're not getting WSDL
            boolean doWsdl = false;

            // cookie for this session, if any
            String cooky = null;

            String methodName = null;

            try {
                authInfo.delete(0, authInfo.length());

                // read headers
                is.setInputStream(socket.getInputStream());
                // parse all headers into hashtable
                MimeHeaders requestHeaders = new MimeHeaders();
                int contentLength = parseHeaders(is, buf, contentType,
                        contentLocation, soapAction,
                        httpRequest, fileName,
                        cookie, cookie2, authInfo, requestHeaders);
                is.setContentLength(contentLength);

                int paramIdx = fileName.toString().indexOf('?');
                if (paramIdx != -1) {
                    // Got params
                    String params = fileName.substring(paramIdx + 1);
                    fileName.setLength(paramIdx);

                    log.debug(Messages.getMessage("filename00",
                            fileName.toString()));
                    log.debug(Messages.getMessage("params00",
                            params));

                    if ("wsdl".equalsIgnoreCase(params))
                        doWsdl = true;

                    if (params.startsWith("method=")) {
                        methodName = params.substring(7);
                    }
                }



                String filePart = fileName.toString();
                if (filePart.startsWith("axis/services/")) {
                    String servicePart = filePart.substring(14);
                    int separator = servicePart.indexOf('/');
                    if (separator > -1) {
                        msgContext.setProperty("objectID",
                                       servicePart.substring(separator + 1));
                        servicePart = servicePart.substring(0, separator);
                    }
                    msgContext.setCurrentService(new QName(servicePart));
                }

                if (authInfo.length() > 0) {
                    // Process authentication info
                    //authInfo = new StringBuffer("dXNlcjE6cGFzczE=");
                    byte[] decoded = Base64.decode(authInfo.toString());
                    StringBuffer userBuf = new StringBuffer();
                    StringBuffer pwBuf = new StringBuffer();
                    StringBuffer authBuf = userBuf;
                    for (int i = 0; i < decoded.length; i++) {
                        if ((char) (decoded[i] & 0x7f) == ':') {
                            authBuf = pwBuf;
                            continue;
                        }
                        authBuf.append((char) (decoded[i] & 0x7f));
                    }

                    if (log.isDebugEnabled()) {
                        log.debug(Messages.getMessage("user00",
                                userBuf.toString()));
                    }

                    msgContext.setProperty(MessageContext.USER_NAME,userBuf.toString());
                    msgContext.setProperty(MessageContext.PASSWARD,pwBuf.toString());
                }
///////////////////////
                // if get, then return simpleton document as response
                if (httpRequest.toString().equals("GET")) {
                		throw new UnsupportedOperationException("GET not supported"); 
                } else {

                    // this may be "" if either SOAPAction: "" or if no SOAPAction at all.
                    // for now, do not complain if no SOAPAction at all
                    String soapActionString = soapAction.toString();
                    if (soapActionString != null) {
                        msgContext.setUseSOAPAction(true);
                        msgContext.setSoapAction(soapActionString);
                    }

                    // Send it on its way...
                    OutputStream out = socket.getOutputStream();
                    out.write(HTTP);
                    out.write(status);
                    log.info("status written");
                    msgContext.setTransportSender(new TransportSender(out));

                    XmlPullParserFactory pf = XmlPullParserFactory.newInstance();
                    pf.setNamespaceAware(true);
                    XmlPullParser  parser = pf.newPullParser();
                    parser.setInput(new InputStreamReader(is));
                    
                    OMXMLParserWrapper parserWrapper = new OMXMLPullParserWrapper(parser); 
                    msgContext.setInMessage(parserWrapper.getSOAPMessage());
                    EngineRegistry reg = engine.getRegistry();
                    // invoke the Axis engine
                    engine.recive(msgContext);
                    log.info("revice done");
                    out.flush();
                }

            } catch (Exception e) {
            	e.printStackTrace();
            }


//            if (resposeMessage != null) {
//
//                //out.write(XML_MIME_STUFF);
//                out.write(("\r\n" + HTTPConstants.HEADER_CONTENT_TYPE + ": " +resposeMessage.getContentType()).getBytes());
//
//            }

            // out.write(response);
            
        } catch (Exception e) {
            log.info(Messages.getMessage("exception00"), e);
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (Exception e) {
            }
        }
    }

    protected void invokeMethodFromGet(String methodName, String args) throws Exception {

    }

    /**
     * Read all mime headers, returning the value of Content-Length and
     * SOAPAction.
     * @param is         InputStream to read from
     * @param contentType The content type.
     * @param contentLocation The content location
     * @param soapAction StringBuffer to return the soapAction into
     * @param httpRequest StringBuffer for GET / POST
     * @param cookie first cookie header (if doSessions)
     * @param cookie2 second cookie header (if doSessions)
     * @param headers HTTP headers to transfer to MIME headers
     * @return Content-Length
     */
    private int parseHeaders(NonBlockingBufferedInputStream is,
                             byte buf[],
                             StringBuffer contentType,
                             StringBuffer contentLocation,
                             StringBuffer soapAction,
                             StringBuffer httpRequest,
                             StringBuffer fileName,
                             StringBuffer cookie,
                             StringBuffer cookie2,
                             StringBuffer authInfo,
                             MimeHeaders headers)
            throws java.io.IOException {
        int n;
        int len = 0;

        // parse first line as GET or POST
        n = this.readLine(is, buf, 0, buf.length);
        if (n < 0) {
            // nothing!
            throw new java.io.IOException(Messages.getMessage("unexpectedEOS00"));
        }

        // which does it begin with?
        httpRequest.delete(0, httpRequest.length());
        fileName.delete(0, fileName.length());
        contentType.delete(0, contentType.length());
        contentLocation.delete(0, contentLocation.length());

        if (buf[0] == getHeader[0]) {
            httpRequest.append("GET");
            for (int i = 0; i < n - 5; i++) {
                char c = (char) (buf[i + 5] & 0x7f);
                if (c == ' ')
                    break;
                fileName.append(c);
            }
            log.debug(Messages.getMessage("filename01", "SimpleAxisServer", fileName.toString()));
            return 0;
        } else if (buf[0] == postHeader[0]) {
            httpRequest.append("POST");
            for (int i = 0; i < n - 6; i++) {
                char c = (char) (buf[i + 6] & 0x7f);
                if (c == ' ')
                    break;
                fileName.append(c);
            }
            log.debug(Messages.getMessage("filename01", "SimpleAxisServer", fileName.toString()));
        } else {
            throw new java.io.IOException(Messages.getMessage("badRequest00"));
        }

        while ((n = readLine(is, buf, 0, buf.length)) > 0) {

            if ((n <= 2) && (buf[0] == '\n' || buf[0] == '\r') && (len > 0)) break;

            // RobJ gutted the previous logic; it was too hard to extend for more headers.
            // Now, all it does is search forwards for ": " in the buf,
            // then do a length / byte compare.
            // Hopefully this is still somewhat efficient (Sam is watching!).

            // First, search forwards for ": "
            int endHeaderIndex = 0;
            while (endHeaderIndex < n && toLower[buf[endHeaderIndex]] != headerEnder[0]) {
                endHeaderIndex++;
            }
            endHeaderIndex += 2;
            // endHeaderIndex now points _just past_ the ": ", and is
            // comparable to the various lenLen, actionLen, etc. values

            // convenience; i gets pre-incremented, so initialize it to one less
            int i = endHeaderIndex - 1;

            // which header did we find?
            if (endHeaderIndex == lenLen && matches(buf, lenHeader)) {
                // parse content length

                while ((++i < n) && (buf[i] >= '0') && (buf[i] <= '9')) {
                    len = (len * 10) + (buf[i] - '0');
                }
                headers.addHeader(HTTPConstants.HEADER_CONTENT_LENGTH, String.valueOf(len));

            } else if (endHeaderIndex == actionLen
                    && matches(buf, actionHeader)) {

                soapAction.delete(0, soapAction.length());
                // skip initial '"'
                i++;
                while ((++i < n) && (buf[i] != '"')) {
                    soapAction.append((char) (buf[i] & 0x7f));
                }
                headers.addHeader(HTTPConstants.HEADER_SOAP_ACTION, "\"" + soapAction.toString() + "\"");

            } else if (endHeaderIndex == authLen && matches(buf, authHeader)) {
                if (matches(buf, endHeaderIndex, basicAuth)) {
                    i += basicAuth.length;
                    while (++i < n && (buf[i] != '\r') && (buf[i] != '\n')) {
                        if (buf[i] == ' ') continue;
                        authInfo.append((char) (buf[i] & 0x7f));
                    }
                    headers.addHeader(HTTPConstants.HEADER_AUTHORIZATION, new String(basicAuth) + authInfo.toString());
                } else {
                    throw new java.io.IOException(
                            Messages.getMessage("badAuth00"));
                }
            } else if (endHeaderIndex == locationLen && matches(buf, locationHeader)) {
                while (++i < n && (buf[i] != '\r') && (buf[i] != '\n')) {
                    if (buf[i] == ' ') continue;
                    contentLocation.append((char) (buf[i] & 0x7f));
                }
                headers.addHeader(HTTPConstants.HEADER_CONTENT_LOCATION, contentLocation.toString());
            } else if (endHeaderIndex == typeLen && matches(buf, typeHeader)) {
                while (++i < n && (buf[i] != '\r') && (buf[i] != '\n')) {
                    if (buf[i] == ' ') continue;
                    contentType.append((char) (buf[i] & 0x7f));
                }
                headers.addHeader(HTTPConstants.HEADER_CONTENT_TYPE, contentLocation.toString());
            } else {
                String customHeaderName = new String(buf, 0, endHeaderIndex - 2);
                StringBuffer customHeaderValue = new StringBuffer();
                while (++i < n && (buf[i] != '\r') && (buf[i] != '\n')) {
                    if (buf[i] == ' ') continue;
                    customHeaderValue.append((char) (buf[i] & 0x7f));
                }
                headers.addHeader(customHeaderName, customHeaderValue.toString());
            }

        }
        return len;
    }

    /**
     * does tolower[buf] match the target byte array, up to the target's length?
     */
    public boolean matches(byte[] buf, byte[] target) {
        for (int i = 0; i < target.length; i++) {
            if (toLower[buf[i]] != target[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Case-insensitive match of a target byte [] to a source byte [],
     * starting from a particular offset into the source.
     */
    public boolean matches(byte[] buf, int bufIdx, byte[] target) {
        for (int i = 0; i < target.length; i++) {
            if (toLower[buf[bufIdx + i]] != target[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * output an integer into the output stream
     * @param out       OutputStream to be written to
     * @param value     Integer value to be written.
     */
    private void putInt(byte buf[], OutputStream out, int value)
            throws java.io.IOException {
        int len = 0;
        int offset = buf.length;

        // negative numbers
        if (value < 0) {
            buf[--offset] = (byte) '-';
            value = -value;
            len++;
        }

        // zero
        if (value == 0) {
            buf[--offset] = (byte) '0';
            len++;
        }

        // positive numbers
        while (value > 0) {
            buf[--offset] = (byte) (value % 10 + '0');
            value = value / 10;
            len++;
        }

        // write the result
        out.write(buf, offset, len);
    }

    /**
     * Read a single line from the input stream
     * @param is        inputstream to read from
     * @param b         byte array to read into
     * @param off       starting offset into the byte array
     * @param len       maximum number of bytes to read
     */
    private int readLine(NonBlockingBufferedInputStream is, byte[] b, int off, int len)
            throws java.io.IOException {
        int count = 0, c;

        while ((c = is.read()) != -1) {
            if (c != '\n' && c != '\r') {
                b[off++] = (byte) c;
                count++;
            }
            if (count == len) break;
            if ('\n' == c) {
                int peek = is.peek(); //If the next line begins with tab or space then this is a continuation.
                if (peek != ' ' && peek != '\t') break;
            }
        }
        return count > 0 ? count : -1;
    }
}
