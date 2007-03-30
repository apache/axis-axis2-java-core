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
package org.apache.axis2.jaxws.message.impl;

import org.apache.axis2.jaxws.message.Attachment;

import javax.activation.DataHandler;
import javax.xml.soap.MimeHeaders;

/**
 * Implementation of the Attachment interface
 *
 * @see org.apache.axis2.jaxws.message.Attachment
 */
public class AttachmentImpl implements Attachment {
    private DataHandler dh;
    private MimeHeaders mimeHeaders = new MimeHeaders();

    /**
     * Constructor
     *
     * @param dh DataHandler
     * @param id Content ID
     */
    AttachmentImpl(DataHandler dh, String id) {
        setDataHandler(dh);
        setContentID(id);
    }

    /*
    * (non-Javadoc)
    * @see org.apache.axis2.jaxws.message.Attachment#getContentType()
    */
    public String getContentType() {
        return getHeaderValue(CONTENT_TYPE);
    }


    /* (non-Javadoc)
    * @see org.apache.axis2.jaxws.message.Attachment#setContentType(java.lang.String)
    */
    public void setContentType(String contentType) {
        mimeHeaders.setHeader(CONTENT_TYPE, contentType);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.Attachment#getContentID()
     */
    public String getContentID() {
        return getHeaderValue(CONTENT_ID);
    }

    /* (non-Javadoc)
    * @see org.apache.axis2.jaxws.message.Attachment#setContentID(java.lang.String)
    */
    public void setContentID(String contentID) {
        mimeHeaders.setHeader(CONTENT_ID, contentID);
    }

    /*
    * (non-Javadoc)
    * @see org.apache.axis2.jaxws.message.Attachment#getDataHandler()
    */
    public DataHandler getDataHandler() {
        return dh;
    }

    /* (non-Javadoc)
    * @see org.apache.axis2.jaxws.message.Attachment#setDataHandler(javax.activation.DataHandler)
    */
    public void setDataHandler(DataHandler dh) {
        this.dh = dh;
        if (dh.getContentType() != null) {
            setContentType(dh.getContentType());
        }
    }

    /* (non-Javadoc)
    * @see org.apache.axis2.jaxws.message.Attachment#getMimeHeaders()
    */
    public MimeHeaders getMimeHeaders() {
        return mimeHeaders;
    }


    /* (non-Javadoc)
    * @see org.apache.axis2.jaxws.message.Attachment#setMimeHeaders(javax.xml.soap.MimeHeaders)
    */
    public void setMimeHeaders(MimeHeaders mhs) {
        mimeHeaders = mhs;
        if (mimeHeaders == null) {
            mimeHeaders = new MimeHeaders();
        }
    }

    private String getHeaderValue(String header) {
        String[] values = mimeHeaders.getHeader(header);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return null;
    }
}
