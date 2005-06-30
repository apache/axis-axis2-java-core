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
package org.apache.axis.om;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.xml.stream.XMLStreamException;

/**
 * @author <a href="mailto:thilina@opensource.lk">Thilina Gunarathne </a>
 */
public class MIMEOutputUtils {
	
	private static byte[] CRLF = { 13, 10 };
	
	static String SOAP_PART_CONTENT_ID = "<SOAPPart>";
	
	public static void complete(OutputStream outStream,
			OutputStream bufferedSoapOutStream, LinkedList binaryNodeList,
			String boundary) throws XMLStreamException {
		try {
			startWritingMime(outStream, boundary);
			
			DataHandler dh = new DataHandler(bufferedSoapOutStream.toString(),
			"text/xml");
			MimeBodyPart rootMimeBodyPart = new MimeBodyPart();
			rootMimeBodyPart.setDataHandler(dh);
			rootMimeBodyPart.addHeader("Content-Type", "application/xop+xml");
			rootMimeBodyPart.addHeader("Content-Transfer-Encoding", "8bit");
			rootMimeBodyPart.addHeader("Content-ID", SOAP_PART_CONTENT_ID);
			
			writeBodyPart(outStream, rootMimeBodyPart, boundary);
			
			Iterator binaryNodeIterator = binaryNodeList.iterator();
			while (binaryNodeIterator.hasNext()) {
				OMText binaryNode = (OMText) binaryNodeIterator.next();
				writeBodyPart(outStream, createMimeBodyPart(binaryNode),
						boundary);
			}
			finishWritingMime(outStream);
		} catch (IOException e) {
			throw new OMException("Problem with the OutputStream."
					+ e.toString());
		} catch (MessagingException e) {
			throw new OMException("Problem writing Mime Parts." + e.toString());
		}
	}
	
	private static MimeBodyPart createMimeBodyPart(OMText node)
	throws MessagingException {
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setDataHandler(node.getDataHandler());
		mimeBodyPart.addHeader("Content-Transfer-Encoding", "binary");
		mimeBodyPart.addHeader("Content-ID", "<" + node.getContentID() + ">");
		return mimeBodyPart;
		
	}
	
	/**
	 * @throws IOException
	 *             This will write the boundary to output Stream
	 */
	private static void writeMimeBoundary(OutputStream outStream,
			String boundary) throws IOException {
		outStream.write(new byte[] { 45, 45 });
		outStream.write(boundary.getBytes());
	}
	
	/**
	 * @throws IOException
	 *             This will write the boundary with CRLF
	 */
	private static void startWritingMime(OutputStream outStream, String boundary)
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
	private static void writeBodyPart(OutputStream outStream,
			MimeBodyPart part, String boundary) throws IOException,
			MessagingException {
		outStream.write(CRLF);
		part.writeTo(outStream);
		outStream.write(CRLF);
		writeMimeBoundary(outStream, boundary);
	}
	
	/**
	 * @throws IOException
	 *             This will write "--" to the end of last boundary
	 */
	private static void finishWritingMime(OutputStream outStream)
	throws IOException {
		outStream.write(new byte[] { 45, 45 });
	}
	
	public static String getContentTypeForMime(String boundary) {
		ContentType contentType = new ContentType();
		contentType.setPrimaryType("multipart");
		contentType.setSubType("related");
		contentType.setParameter("boundary", boundary);
		contentType.setParameter("start", MIMEOutputUtils.SOAP_PART_CONTENT_ID);
		contentType.setParameter("type", "application/xop+xml");
		//TODO theres something called action that can be set with
		// following. May be SOAPAction. Better check.
		contentType.setParameter("startinfo", "application/xop+xml");
		return contentType.toString();
	}
	
}