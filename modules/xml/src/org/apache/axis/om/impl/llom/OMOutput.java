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
package org.apache.axis.om.impl.llom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis.om.OMText;

/**
 * @author <a href="mailto:thilina@opensource.lk">Thilina Gunarathne </a>
 * For the moment this assumes that transport takes the decision of whether to optimise or not
 * by looking at whether the MTOM optimise is enabled & also looking at the OM tree whether it has any 
 * optimisable content
 */

public class OMOutput {
	private XMLStreamWriter xmlWriter;
	
	private boolean doOptimise;
	
	private OutputStream outStream;
	
	private byte[] CRLF = { 13, 10 };
	
	private XMLStreamWriter writer;
	
	private LinkedList binaryNodeList;
	
	private ByteArrayOutputStream bufferedSoapOutStream;
	
	private String mimeBoundary = null;
	
	private String SOAP_PART_CONTENT_ID = "<SOAPPart>";
	
	private ContentType contentType = null;
	
	/**
	 * @param xmlWriter
	 *            if it is guaranteed for not using attachments one can use this
	 */
	public OMOutput(XMLStreamWriter xmlWriter) {
		this.xmlWriter = xmlWriter;
	}
	
	/**
	 * @throws FactoryConfigurationError
	 * @throws XMLStreamException
	 *  
	 */
	public OMOutput(OutputStream outStream, boolean doOptimise)
	throws XMLStreamException, FactoryConfigurationError {
		this.doOptimise = doOptimise;
		this.outStream = outStream;
		if (doOptimise) {
			bufferedSoapOutStream = new ByteArrayOutputStream();
			xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(
					bufferedSoapOutStream);
			binaryNodeList = new LinkedList();
		} else {
			xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(
					outStream);
			
		}
		
	}
	
	public XMLStreamWriter getXmlStreamWriter() {
		return xmlWriter;
	}
	
	public void flush() throws XMLStreamException {
		if (doOptimise) {
			try {
				this.complete();
			} catch (IOException e) {
				//TODO this is just a hack to avoid passing IOException. Must find a better way to handle this
				throw new XMLStreamException("Error creating mime parts. Problem with Streams");
			} catch (MessagingException e) {
				throw new XMLStreamException("Error creating mime Body parts");
			} 
		} else {
			xmlWriter.flush();
		}
		
	}
	
	public boolean doOptimise() {
		return doOptimise;
	}
	
	public String getContentType() {
		if (contentType == null && doOptimise) {
			contentType = new ContentType();
			contentType.setPrimaryType("multipart");
			contentType.setSubType("related");
			contentType.setParameter("boundary", getMimeBoundary());
			contentType.setParameter("start", SOAP_PART_CONTENT_ID);
			contentType.setParameter("type", "application/xop+xml");
			//TODO theres something called action that can be set with
			// following. May be SOAPAction. Better check.
			contentType.setParameter("startinfo", "application/xop+xml");
		}
		return contentType.toString();
	}
	
	public void writeOptimised(OMText node) {
		binaryNodeList.add(node);
	}
	
	public void complete() throws IOException, MessagingException, XMLStreamException {
		startWritingMime();
		xmlWriter.flush();
		DataHandler dh = new DataHandler(bufferedSoapOutStream.toString(),
		"text/xml");
		MimeBodyPart rootMimeBodyPart = new MimeBodyPart();
		rootMimeBodyPart.setDataHandler(dh);
		rootMimeBodyPart.addHeader("Content-Type", "application/xop+xml");
		rootMimeBodyPart.addHeader("Content-Transfer-Encoding", "8bit");
		rootMimeBodyPart.addHeader("Content-ID", SOAP_PART_CONTENT_ID);
		
		writeBodyPart(rootMimeBodyPart);
		
		Iterator binaryNodeIterator = binaryNodeList.iterator();
		while (binaryNodeIterator.hasNext()) {
			OMText binaryNode = (OMText) binaryNodeIterator.next();
			writeBodyPart(createMimeBodyPart(binaryNode));
		}
		finishWritingMime();
		
	}
	
	private MimeBodyPart createMimeBodyPart(OMText node)
	throws MessagingException {
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setDataHandler(node.getDataHandler());
		mimeBodyPart.addHeader("Content-Transfer-Encoding", "binary");
		mimeBodyPart.addHeader("Content-ID", "<" + node.getContentID() + ">");
		return mimeBodyPart;
		
	}
	
	private String getMimeBoundary() {
		//TODO have to dynamically generate.
		if (mimeBoundary == null) {
			mimeBoundary = "----=_AxIs2_Def_boundary_=42214532";
		}
		return mimeBoundary;
	}
	
	/**
	 * @throws IOException
	 *             This will write the boundary to output Stream
	 */
	private void writeMimeBoundary() throws IOException {
		outStream.write(new byte[] { 45, 45 });
		outStream.write(getMimeBoundary().getBytes());
	}
	
	/**
	 * @throws IOException
	 *             This will write the boundary with CRLF
	 */
	private void startWritingMime() throws IOException {
		writeMimeBoundary();
		outStream.write(CRLF);
	}
	
	/**
	 * this will write a CRLF for the earlier boudary then the BodyPart data
	 * with headers followed by boundary. Writes only the boundary. No more
	 * CRLF's are wriiting after that.
	 * 
	 * @throws IOException
	 * @throws MessagingException
	 */
	private void writeBodyPart(MimeBodyPart part) throws IOException,
	MessagingException {
		outStream.write(CRLF);
		part.writeTo(outStream);
		outStream.write(CRLF);
		writeMimeBoundary();
	}
	
	/**
	 * @throws IOException
	 *             This will write "--" to the end of last boundary
	 */
	private void finishWritingMime() throws IOException {
		outStream.write(new byte[] { 45, 45 });
	}
	
}