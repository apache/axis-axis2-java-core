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

package org.apache.axis.impl.transport.http;

import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.OutputStream;



public abstract class SimpleHTTPHandler implements Runnable {
    protected static Log log =
            LogFactory.getLog(SimpleHTTPHandler.class.getName());

    // Axis specific constants
    protected static String transportName = "SimpleHTTP";
    protected AxisEngine engine;

    // HTTP status codes
    protected static byte OK[] = ("200 OK").getBytes();
    protected static byte NOCONTENT[] = ("202 OK\n\n").getBytes();
    protected static byte UNAUTH[] = ("401 Unauthorized").getBytes();
    protected static byte SENDER[] = "400".getBytes();
    protected static byte ISE[] = ("500 Internal server error").getBytes();

    // HTTP prefix
    protected static byte HTTP[] = "HTTP/1.0 ".getBytes();

    // Standard MIME headers for XML payload
    protected static byte XML_MIME_STUFF[] =
            ("\r\nContent-Type: text/xml; charset=utf-8\r\n" +
            "Content-Length: ").getBytes();

    // Standard MIME headers for HTML payload
    protected static byte HTML_MIME_STUFF[] =
            ("\r\nContent-Type: text/html; charset=utf-8\r\n" +
            "Content-Length: ").getBytes();

    // Mime/Content separator
    protected static byte SEPARATOR[] = "\r\n\r\n".getBytes();

    // Tiddly little response
//    protected static final String responseStr =
//            "<html><head><title>SimpleAxisServer</title></head>" +
//            "<body><h1>SimpleAxisServer</h1>" +
//            Messages.getEnvelope("reachedServer00") +
//            "</html>";
//    protected static byte cannedHTMLResponse[] = responseStr.getBytes();

    // ASCII character mapping to lower case
    protected static final byte[] toLower = new byte[256];

    static {
        for (int i = 0; i < 256; i++) {
            toLower[i] = (byte) i;
        }

        for (int lc = 'a'; lc <= 'z'; lc++) {
            toLower[lc + 'A' - 'a'] = (byte) lc;
        }
    }

    // buffer for IO
    protected static final int BUFSIZ = 4096;

    // mime header for content length
    protected static final byte lenHeader[] = "content-length: ".getBytes();
    protected static final int lenLen = lenHeader.length;

    // mime header for content type
    protected static final byte typeHeader[] = (HTTPConstants.HEADER_CONTENT_TYPE.toLowerCase() + ": ").getBytes();
    protected static final int typeLen = typeHeader.length;

    // mime header for content location
    protected static final byte locationHeader[] = (HTTPConstants.HEADER_CONTENT_LOCATION.toLowerCase() + ": ").getBytes();
    protected static final int locationLen = locationHeader.length;

    // mime header for soap action
    protected static final byte actionHeader[] = "soapaction: ".getBytes();
    protected static final int actionLen = actionHeader.length;

    // mime header for cookie
    protected static final byte cookieHeader[] = "cookie: ".getBytes();
    protected static final int cookieLen = cookieHeader.length;

    // mime header for cookie2
    protected static final byte cookie2Header[] = "cookie2: ".getBytes();
    protected static final int cookie2Len = cookie2Header.length;

    // HTTP header for authentication
    protected static final byte authHeader[] = "authorization: ".getBytes();
    protected static final int authLen = authHeader.length;

    // mime header for GET
    protected static final byte getHeader[] = "GET".getBytes();

    // mime header for POST
    protected static final byte postHeader[] = "POST".getBytes();

    // header ender
    protected static final byte headerEnder[] = ": ".getBytes();

    // "Basic" auth string
    protected static final byte basicAuth[] = "basic ".getBytes();


    /**
     * Run method
     */
    
    public abstract MessageContext parseHTTPHeaders()throws AxisFault;
    
    public void run() {
        try {
            parseHTTPHeaders();
        }catch(AxisFault e){
            log.error(e);
        }finally {
            
        }
    }
    
    /**
     * The main workhorse method.
     */

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
    protected int parseHeaders(NonBlockingBufferedInputStream is,
                             byte buf[],
                             StringBuffer contentType,
                             StringBuffer contentLocation,
                             StringBuffer soapAction,
                             StringBuffer httpRequest,
                             StringBuffer fileName,
                             StringBuffer cookie,
                             StringBuffer cookie2,
                             StringBuffer authInfo)
//                             MimeHeaders headers)
            throws java.io.IOException {
        int n;
        int len = 0;

        // parse first line as GET or POST
        n = this.readLine(is, buf, 0, buf.length);
        if (n < 0) {
            // nothing!
            throw new java.io.IOException("Unexpected end of stream");
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
            return 0;
        } else if (buf[0] == postHeader[0]) {
            httpRequest.append("POST");
            for (int i = 0; i < n - 6; i++) {
                char c = (char) (buf[i + 6] & 0x7f);
                if (c == ' ')
                    break;
                fileName.append(c);
            }
        } else {
            throw new java.io.IOException("Cannot handle non-GET, non-POST request");
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
//                headers.addHeader(HTTPConstants.HEADER_CONTENT_LENGTH, String.valueOf(len));

            } else if (endHeaderIndex == actionLen
                    && matches(buf, actionHeader)) {

                soapAction.delete(0, soapAction.length());
                // skip initial '"'
                i++;
                while ((++i < n) && (buf[i] != '"')) {
                    soapAction.append((char) (buf[i] & 0x7f));
                }
//                headers.addHeader(HTTPConstants.HEADER_SOAP_ACTION, "\"" + soapAction.toString() + "\"");

            } else if (endHeaderIndex == authLen && matches(buf, authHeader)) {
                if (matches(buf, endHeaderIndex, basicAuth)) {
                    i += basicAuth.length;
                    while (++i < n && (buf[i] != '\r') && (buf[i] != '\n')) {
                        if (buf[i] == ' ') continue;
                        authInfo.append((char) (buf[i] & 0x7f));
                    }
//                    headers.addHeader(HTTPConstants.HEADER_AUTHORIZATION, new String(basicAuth) + authInfo.toString());
                } else {
                    throw new java.io.IOException("Bad authentication type (I can only handle \"Basic\")");
                }
            } else if (endHeaderIndex == locationLen && matches(buf, locationHeader)) {
                while (++i < n && (buf[i] != '\r') && (buf[i] != '\n')) {
                    if (buf[i] == ' ') continue;
                    contentLocation.append((char) (buf[i] & 0x7f));
                }
//                headers.addHeader(HTTPConstants.HEADER_CONTENT_LOCATION, contentLocation.toString());
            } else if (endHeaderIndex == typeLen && matches(buf, typeHeader)) {
                while (++i < n && (buf[i] != '\r') && (buf[i] != '\n')) {
                    if (buf[i] == ' ') continue;
                    contentType.append((char) (buf[i] & 0x7f));
                }
//                headers.addHeader(HTTPConstants.HEADER_CONTENT_TYPE, contentLocation.toString());
            } else {
                String customHeaderName = new String(buf, 0, endHeaderIndex - 2);
                StringBuffer customHeaderValue = new StringBuffer();
                while (++i < n && (buf[i] != '\r') && (buf[i] != '\n')) {
                    if (buf[i] == ' ') continue;
                    customHeaderValue.append((char) (buf[i] & 0x7f));
                }
//                headers.addHeader(customHeaderName, customHeaderValue.toString());
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
    protected void putInt(byte buf[], OutputStream out, int value)
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
    protected int readLine(NonBlockingBufferedInputStream is, byte[] b, int off, int len)
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
