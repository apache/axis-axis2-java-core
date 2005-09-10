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
package org.apache.axis2.om.impl;

import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;

public class MIMEOutputUtils {

    private static byte[] CRLF = {13, 10};
    private Log log = LogFactory.getLog(getClass());

    public static void complete(OutputStream outStream,
                                OutputStream bufferedSoapOutStream, LinkedList binaryNodeList,
                                String boundary, String contentId, String charSetEncoding,String SOAPContentType) {
        try {
            startWritingMime(outStream, boundary);

            DataHandler dh = new DataHandler(bufferedSoapOutStream.toString(),
                    "text/xml");
            MimeBodyPart rootMimeBodyPart = new MimeBodyPart();
            rootMimeBodyPart.setDataHandler(dh);
            
            rootMimeBodyPart.addHeader("content-type",
                    "application/xop+xml; charset=" + charSetEncoding + 
					"; type=\""+SOAPContentType+"\";");
            rootMimeBodyPart.addHeader("content-transfer-encoding", "binary");
            rootMimeBodyPart.addHeader("content-id","<"+contentId+">");

            writeBodyPart(outStream, rootMimeBodyPart, boundary);

            Iterator binaryNodeIterator = binaryNodeList.iterator();
            while (binaryNodeIterator.hasNext()) {
                OMText binaryNode = (OMText) binaryNodeIterator.next();
                writeBodyPart(outStream, createMimeBodyPart(binaryNode),
                        boundary);
            }
            finishWritingMime(outStream);
        } catch (IOException e) {
            throw new OMException("Problem with the OutputStream.", e);
        } catch (MessagingException e) {
            throw new OMException("Problem writing Mime Parts.", e);
        }
    }

    public static MimeBodyPart createMimeBodyPart(OMText node)
            throws MessagingException {
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setDataHandler(node.getDataHandler());
        mimeBodyPart.addHeader("content-id", "<"+node.getContentID()+">");
        mimeBodyPart.addHeader("content-type", "application/octet-stream");
        mimeBodyPart.addHeader("content-transfer-encoding", "binary");
        return mimeBodyPart;

    }

    /**
     * @throws IOException This will write the boundary to output Stream
     */
    public static void writeMimeBoundary(OutputStream outStream,
                                         String boundary) throws IOException {
        outStream.write(new byte[]{45, 45});
        outStream.write(boundary.getBytes());
    }

    /**
     * @throws IOException This will write the boundary with CRLF
     */
    public static void startWritingMime(OutputStream outStream,
                                        String boundary)
            throws IOException {
        writeMimeBoundary(outStream, boundary);
        //outStream.write(CRLF);
    }

    /**
     * this will write a CRLF for the earlier boudary then the BodyPart data
     * with headers followed by boundary. Writes only the boundary. No more
     * CRLF's are wriiting after that.
     *
     * @throws IOException
     * @throws MessagingException
     */
    public static void writeBodyPart(OutputStream outStream,
                                     MimeBodyPart part, String boundary) throws IOException,
            MessagingException {
        outStream.write(CRLF);
        part.writeTo(outStream);
        outStream.write(CRLF);
        writeMimeBoundary(outStream, boundary);
    }

    /**
     * @throws IOException This will write "--" to the end of last boundary
     */
    public static void finishWritingMime(OutputStream outStream)
            throws IOException {
        outStream.write(new byte[]{45, 45});
    }

    public static String getContentTypeForMime(String boundary, String contentId, String charSetEncoding, String SOAPContentType) {
        StringBuffer sb = new StringBuffer();
        sb.append("multipart/related");
        sb.append("; ");
        sb.append("boundary=");
        sb.append(boundary);
        sb.append("; ");
        sb.append("type=\"application/xop+xml\"");
        sb.append("; ");
        sb.append("start=\"<" + contentId + ">\"");
        sb.append("; ");
        sb.append("start-info=\""+SOAPContentType+"\"");
        return sb.toString();
    }

}