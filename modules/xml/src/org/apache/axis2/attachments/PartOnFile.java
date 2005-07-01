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
 */
package org.apache.axis2.attachments;

import org.apache.axis2.om.OMException;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

/**
 * @author <a href="mailto:thilina@opensource.lk"> Thilina Gunarathne </a>
 */
public class PartOnFile implements Part {
	
	String fileName;
	
	Part bodyPart;
	
	int size;
	
	String contentType;
	
	Enumeration headers;
	
	String contentID;
	
	public PartOnFile(Part bodyPart,String contentID, String repoDir) throws Exception {
		super();
		size = bodyPart.getSize();
		contentType = bodyPart.getContentType();
		headers = bodyPart.getAllHeaders();
		// TODO Find a better naming algorithm
		fileName = repoDir+"/"+(new Date()).getTime() + ".tmp";
		FileOutputStream outFileStream;
		outFileStream = new FileOutputStream(fileName);
		bodyPart.writeTo(outFileStream);
		outFileStream.close();
	}
	
	private Part getPartOnFile() {
		FileInputStream inFileStream;
		Part part=null;
		try {
			inFileStream = new FileInputStream(fileName);
			
			part = new MimeBodyPart(inFileStream);
		} catch (FileNotFoundException e) {
			throw new OMException("File Not Found"+e.toString());
		} catch (MessagingException e1) {
			throw new OMException("Cannot create MimePart from the Part read from file"+e1.toString());
		}
		return part;
	}
	
	public String getContentID()
	{
		return contentID;
	}
	public int getSize() throws MessagingException {
		return size;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#getLineCount()
	 */
	public int getLineCount() throws MessagingException {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#getContentType()
	 */
	public String getContentType() throws MessagingException {
		// TODO Auto-generated method stub
		return contentType;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#isMimeType(java.lang.String)
	 */
	public boolean isMimeType(String arg0) throws MessagingException {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#getDisposition()
	 */
	public String getDisposition() throws MessagingException {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#setDisposition(java.lang.String)
	 */
	public void setDisposition(String arg0) throws MessagingException {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#getDescription()
	 */
	public String getDescription() throws MessagingException {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#setDescription(java.lang.String)
	 */
	public void setDescription(String arg0) throws MessagingException {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#getFileName()
	 */
	public String getFileName() throws MessagingException {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#setFileName(java.lang.String)
	 */
	public void setFileName(String arg0) throws MessagingException {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#getInputStream()
	 */
	public InputStream getInputStream() throws IOException, MessagingException {
		Part part = getPartOnFile();
		return part.getInputStream();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#getDataHandler()
	 */
	public DataHandler getDataHandler() throws MessagingException {
		Part part = getPartOnFile();
		return part.getDataHandler();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#getContent()
	 */
	public Object getContent() throws IOException, MessagingException {
		Part part = getPartOnFile();
		return part.getContent();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#setDataHandler(javax.activation.DataHandler)
	 */
	public void setDataHandler(DataHandler arg0) throws MessagingException {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#setContent(java.lang.Object, java.lang.String)
	 */
	public void setContent(Object arg0, String arg1) throws MessagingException {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#setText(java.lang.String)
	 */
	public void setText(String arg0) throws MessagingException {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#setContent(javax.mail.Multipart)
	 */
	public void setContent(Multipart arg0) throws MessagingException {
		throw new UnsupportedOperationException();
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#writeTo(java.io.OutputStream)
	 */
	public void writeTo(OutputStream outStream) throws IOException,
	MessagingException {
		Part part = getPartOnFile();
		part.writeTo(outStream);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#getHeader(java.lang.String)
	 */
	public String[] getHeader(String arg0) throws MessagingException {
		ArrayList selectedHeader = null;
		while (headers.hasMoreElements()) {
			String header = (String) headers.nextElement();
			if (arg0.equals(header)) {
				selectedHeader.add(header);
			}
		}
		String[] headerStrings = (String[]) selectedHeader.toArray();
		return headerStrings;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String arg0, String arg1) throws MessagingException {
		throw new UnsupportedOperationException();
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String arg0, String arg1) throws MessagingException {
		throw new UnsupportedOperationException();
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#removeHeader(java.lang.String)
	 */
	public void removeHeader(String arg0) throws MessagingException {
		throw new UnsupportedOperationException();
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#getAllHeaders()
	 */
	public Enumeration getAllHeaders() throws MessagingException {
		return headers;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#getMatchingHeaders(java.lang.String[])
	 */
	public Enumeration getMatchingHeaders(String[] arg0)
	throws MessagingException {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Part#getNonMatchingHeaders(java.lang.String[])
	 */
	public Enumeration getNonMatchingHeaders(String[] arg0)
	throws MessagingException {
		throw new UnsupportedOperationException();
	}
	
}