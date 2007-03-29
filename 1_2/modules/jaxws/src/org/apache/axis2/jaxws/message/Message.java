/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.message;

import java.util.List;

import javax.activation.DataHandler;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.jaxws.message.factory.BlockFactory;

/**
 * Message
 * 
 * A Message represents the XML + Attachments
 * 
 * Most of the methods available on a message are only applicable to 
 * the XML part of the Message.  See the XMLPart interface for an explantation of these methods.
 * 
 * @see org.apache.axis2.jaxws.message.XMLPart
 * @see org.apache.axis2.jaxws.message.Attachment
 * 
 */
public interface Message extends XMLPart {
	
	/**
	 * Get the protocol for this Message (soap11, soap12, etc.)
	 * @return Protocl
	 */
	public Protocol getProtocol();
	
	/**
	 * getAsSOAPMessage
	 * Get the xml part as a read/write SOAPEnvelope
	 * @return SOAPEnvelope
	 */
	public SOAPMessage getAsSOAPMessage() throws WebServiceException;
	
    /**
     * Adds an attachment part to the message
     * @param Attachment - the content to add
     */
	public void addAttachment(Attachment a);
    
    /**
     * Create an Attachment.
     * After the Attachment is created, it must be added with addAttachment()
     * @param dh DataHandler (type of Attachment is inferred from dh.getContentType)
     * @param id String which is the Attachment id
     * @return Attachment
     * @see addAttachment(Attachment)
     */
    public Attachment createAttachment(DataHandler dh, String id);
    
    /**
	 * Get the list of attachments for the message
	 * @return List<Attachments>
	 */
	public List<Attachment> getAttachments();
    
    /**
     * Get the attachment identified by the contentID 
     * @param cid
     * @return
     */
    public Attachment getAttachment(String cid);
    
    /** 
     * Get the attachment and remove it from the Message
     * @param cid
     * @return
     */
    public Attachment removeAttachment(String cid);
    
    /**
     * @return if this is an MTOM message
     */
    public boolean isMTOMEnabled();
    
    /** Indicate whether this is an MTOM message
     * @param b
     */
    public void setMTOMEnabled(boolean b);
    
    /**
     * @return get the MimeHeaders
     */
    public MimeHeaders getMimeHeaders();
    
    /**
     * Set the MimeHeaders
     * @param mhs MimeHeaders
     */
    public void setMimeHeaders(MimeHeaders mhs);
    
    /**
     * Indicate that this message is passed the pivot point.
     * For example, this is set in the JAX-WS Dispatcher
     * to indicate 
     */
    public void setPostPivot();
    
    /**
     * @return true if post pivot
     */
    public boolean isPostPivot();
    
    /* 
     * Get the entire message rendered in a certain type of value (i.e. String, Source, SOAPMessage, etc.)
     * @param context
     * @param blockFactory blockfactory associated with the kind of rendering
     */
    public Object getValue(Object context, BlockFactory blockFactory)  
        throws WebServiceException;
}
