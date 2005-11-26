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
package org.apache.axis2.saaj2;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

public class SOAPMessageImpl extends SOAPMessage {

	
    private SOAPPartImpl mSOAPPart;
    private ArrayList attachments = new ArrayList();
    private java.util.Hashtable mProps = new java.util.Hashtable();
    private MimeHeadersEx headers;
	
    
    
    
	private void setup(Object initialContents, String contentType, String contentLocation,
            MimeHeaders mimeHeaders) throws SOAPException {
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
        
	}
	
	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPMessage#getContentDescription()
	 */
	public String getContentDescription() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPMessage#setContentDescription(java.lang.String)
	 */
	public void setContentDescription(String description) {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPMessage#getSOAPPart()
	 */
	public SOAPPart getSOAPPart() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPMessage#removeAllAttachments()
	 */
	public void removeAllAttachments() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPMessage#countAttachments()
	 */
	public int countAttachments() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPMessage#getAttachments()
	 */
	public Iterator getAttachments() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPMessage#getAttachments(javax.xml.soap.MimeHeaders)
	 */
	public Iterator getAttachments(javax.xml.soap.MimeHeaders headers) {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPMessage#addAttachmentPart(javax.xml.soap.AttachmentPart)
	 */
	public void addAttachmentPart(AttachmentPart attachmentpart) {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPMessage#createAttachmentPart()
	 */
	public AttachmentPart createAttachmentPart() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPMessage#getMimeHeaders()
	 */
	public javax.xml.soap.MimeHeaders getMimeHeaders() {
		return this.headers;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPMessage#saveChanges()
	 */
	public void saveChanges() throws SOAPException {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPMessage#saveRequired()
	 */
	public boolean saveRequired() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPMessage#writeTo(java.io.OutputStream)
	 */
	public void writeTo(OutputStream out) throws SOAPException, IOException {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}
	
}
