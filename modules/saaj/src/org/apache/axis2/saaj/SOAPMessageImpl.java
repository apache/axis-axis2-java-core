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
package org.apache.axis2.saaj;

import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class SOAPMessageImpl extends SOAPMessage {

    private SOAPPartImpl mSOAPPart;
    private ArrayList attachments = new ArrayList();
    private java.util.Hashtable mProps = new java.util.Hashtable();
    private MimeHeaders headers;
    private Log log = LogFactory.getLog(getClass());


    public SOAPMessageImpl(Object initialContents) {
        try {
            setup(initialContents, false, null, null, null);
        } catch (SOAPException e) {
           log.error("Error in creating SOAPMessage", e);
        }
    }

    public SOAPMessageImpl(Object initialContents,
                           boolean bodyInStream,
                           javax.xml.soap.MimeHeaders headers) {
        try {
            setup(initialContents,
                    bodyInStream,
                    null,
                    null,
                    headers);
        } catch (SOAPException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void setup(Object initialContents, boolean bodyInStream,
                       String contentType, String contentLocation,
                       javax.xml.soap.MimeHeaders mimeHeaders) throws SOAPException {
        if(contentType == null && mimeHeaders != null) {
            String contentTypes[] = mimeHeaders.getHeader("Content-Type");
            contentType = (contentTypes != null)? contentTypes[0] : null;
        }
        
        if(contentLocation == null && mimeHeaders != null) {
            String contentLocations[] = mimeHeaders.getHeader("Content-Location");
            contentLocation = (contentLocations != null)? contentLocations[0] : null;
        }
        
        if (contentType != null) {
            int delimiterIndex = contentType.lastIndexOf("charset");
            if (delimiterIndex > 0) {
                String charsetPart = contentType.substring(delimiterIndex);
                int charsetIndex = charsetPart.indexOf('=');
                String charset = charsetPart.substring(charsetIndex + 1).trim();
                if ((charset.startsWith("\"") || charset.startsWith("\'"))) {
                    charset = charset.substring(1, charset.length());
                }
                if ((charset.endsWith("\"") || charset.endsWith("\'"))) {
                    charset = charset.substring(0, charset.length()-1);
                }
                try {
                    setProperty(SOAPMessage.CHARACTER_SET_ENCODING, charset);
                } catch (SOAPException e) {
                }
            }
        }
    	
    	if (null == mSOAPPart)
            mSOAPPart = new SOAPPartImpl(this, initialContents, bodyInStream);
        else
            mSOAPPart.setMessage(this);

        headers = (mimeHeaders == null) ?
                new MimeHeaders() : new MimeHeaders(mimeHeaders);
    }

    /**
     * Retrieves a description of this <CODE>SOAPMessage</CODE>
     * object's content.
     *
     * @return a <CODE>String</CODE> describing the content of this
     *         message or <CODE>null</CODE> if no description has been
     *         set
     * @see #setContentDescription(java.lang.String) setContentDescription(java.lang.String)
     */
    public String getContentDescription() {
        String values[] = headers.getHeader(
                HTTPConstants.HEADER_CONTENT_DESCRIPTION);
        if (values != null && values.length > 0)
            return values[0];
        return null;
    }

    /**
     * Sets the description of this <CODE>SOAPMessage</CODE>
     * object's content with the given description.
     *
     * @param description a <CODE>String</CODE>
     *                    describing the content of this message
     * @see #getContentDescription() getContentDescription()
     */
    public void setContentDescription(String description) {
        headers.setHeader(HTTPConstants.HEADER_CONTENT_DESCRIPTION,
                description);
    }

    /**
     * @see javax.xml.soap.SOAPMessage#getSOAPPart()
     */
    public SOAPPart getSOAPPart() {
        return mSOAPPart;
    }

    public SOAPBody getSOAPBody() throws SOAPException {
        return mSOAPPart.getEnvelope().getBody();
    }

    public SOAPHeader getSOAPHeader() throws SOAPException {
        return mSOAPPart.getEnvelope().getHeader();
    }

    public void setProperty(String property, Object value) throws SOAPException {
        mProps.put(property, value);
    }

    public Object getProperty(String property) throws SOAPException {
        return mProps.get(property);
    }

    /**
     * @see javax.xml.soap.SOAPMessage#removeAllAttachments()
     */
    public void removeAllAttachments() {
        
    	attachments.clear();
    }

    /**
     * @see javax.xml.soap.SOAPMessage#countAttachments()
     */
    public int countAttachments() {
        
        return attachments.size();
    }

    /**
     * @see javax.xml.soap.SOAPMessage#getAttachments()
     */
    public Iterator getAttachments() {
        
        return attachments.iterator();
    }

    /**
     * @see javax.xml.soap.SOAPMessage#getAttachments(javax.xml.soap.MimeHeaders)
     */
    public Iterator getAttachments(javax.xml.soap.MimeHeaders headers) {
        
    	ArrayList temp = new ArrayList();
    	Iterator iterator = getAttachments();
    	while(iterator.hasNext()){
    		AttachmentPartImpl part = (AttachmentPartImpl)iterator.next();
    		if(part.matches(headers)){
    			temp.add(part);
    		}
    	}
        return temp.iterator();
    }

    /**
     * @see javax.xml.soap.SOAPMessage#addAttachmentPart(javax.xml.soap.AttachmentPart)
     */
    public void addAttachmentPart(AttachmentPart attachmentpart) {
        
    	if(attachmentpart != null){
    		attachments.add(attachmentpart);
    		headers.setHeader("Content-Type","multipart/related");
    	}

    }

    /**
     * @see javax.xml.soap.SOAPMessage#createAttachmentPart()
     */
    public AttachmentPart createAttachmentPart() {
        
        return new AttachmentPartImpl();
    }

    /** 
     * @see javax.xml.soap.SOAPMessage#getMimeHeaders()
     */
    public javax.xml.soap.MimeHeaders getMimeHeaders() {

        return headers;
    }

    /**
     * @see javax.xml.soap.SOAPMessage#saveChanges()
     */
    public void saveChanges() throws SOAPException {
        // TODO Not sure what we should do here
    }

    /**
     * @see javax.xml.soap.SOAPMessage#saveRequired()
     */
    public boolean saveRequired() {
        return false;
    }

    /**
     * @see javax.xml.soap.SOAPMessage#writeTo(java.io.OutputStream)
     */
    public void writeTo(OutputStream out) throws SOAPException, IOException {
        try {
            OMOutputImpl output = new OMOutputImpl();
            output.setCharSetEncoding((String)getProperty(CHARACTER_SET_ENCODING));
            String writeXmlDecl = (String)getProperty(WRITE_XML_DECLARATION);
            if(writeXmlDecl==null || writeXmlDecl.equals("false")) { //SAAJ default case doesn't send XML decl
            	output.ignoreXMLDeclaration(true);
            }
            output.setOutputStream(out, false);
            //the writeTo method forces the elements to be built!!!
            ((SOAPEnvelopeImpl) mSOAPPart.getEnvelope()).getOMEnvelope()
                    .serializeWithCache(output);
            output.flush();
        } catch (Exception e) {
            throw new SOAPException(e);
        }
    }

}
