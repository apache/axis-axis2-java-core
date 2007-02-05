/*
 * Copyright 2006 The Apache Software Foundation.
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

import javax.activation.DataHandler;
import javax.xml.soap.MimeHeaders;

/**
 * Attachment
 * 
 * This class is very similar to the SAAJ concept of Attachment.
 * An Attachment has a content and mimeheaders.
 * 
 * The most important mimeheaders are the ContentType and ContentId.
 * The content is stored with a DataHandler
 * 
 * @see javax.xml.soap.AttachmentPart
 */
public interface Attachment {
    
    /**
     * Gets the MIME content type of the attachment.
     * @return contentType
     */
    public String getContentType();
    
    /**
     * Set the MIME content type of the attachment
     * @param contentType
     */
    public void setContentType(String contentType);
    
    /**
     * Gets the MIME content id that identifies this attachment.
     * @return contentID
     */
    public String getContentID();
    
    /**
     * Set the MIME content id that identifies this attachment
     * @param contentID
     */
    public void setContentID(String contentID);
    
    /**
     * Gets the DataHandler of the attachment in a DataHandler form.
     * @return DataHandler
     */
    public DataHandler getDataHandler();
    
    /**
     * Sets the DataHandler of the attachment
     * @param DataHandler
     */
    public void setDataHandler(DataHandler dh);
    
    /**
     * @return get the MimeHeaders
     */
    public MimeHeaders getMimeHeaders();
    
    /**
     * Set the MimeHeaders
     * @param mh MimeHeaders
     */
    public void setMimeHeaders(MimeHeaders mhs);
    
    // Common Header keys
    public final static String CONTENT_ID         = "Content-Id";
    public final static String CONTENT_TYPE       = "Content-Type";
    public final static String CONTENT_LOCATION   = "Content-Location";
    public final static String CONTENT_LENGTH     = "Content-Length";
}
