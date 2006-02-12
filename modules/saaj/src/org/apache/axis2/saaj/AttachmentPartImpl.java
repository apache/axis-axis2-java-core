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

import org.apache.axis2.om.DOOMAbstractFactory;
import org.apache.axis2.om.impl.dom.DocumentImpl;
import org.apache.axis2.om.impl.dom.TextImpl;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.ws.commons.om.OMText;

import javax.activation.DataHandler;
import javax.activation.UnsupportedDataTypeException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * 
 */
public class AttachmentPartImpl extends AttachmentPart {

    private DataHandler dataHandler;

    /**
     * Field mimeHeaders.
     */
    private MimeHeaders mimeHeaders = new MimeHeaders();

    private OMText omText;

    /**
     * Check whether at least one of the headers of this object matches a provided header
     *
     * @param headers
     * @return <b>true</b> if at least one header of this AttachmentPart matches
     *         a header in the provided <code>headers</code> parameter,
     *         <b>false</b> if none of the headers of this AttachmentPart matches
     *         at least one of the header in the provided <code>headers</code> parameter
     */
    public boolean matches(MimeHeaders headers) {
        for (Iterator i = headers.getAllHeaders(); i.hasNext();) {
            MimeHeader hdr = (javax.xml.soap.MimeHeader) i.next();
            String values[] = mimeHeaders.getHeader(hdr.getName());
            boolean found = false;
            if (values != null) {
                for (int j = 0; j < values.length; j++) {
                    if (!hdr.getValue().equalsIgnoreCase(values[j])) {
                        continue;
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the number of bytes in this <CODE>
     * AttachmentPart</CODE> object.
     *
     * @return the size of this <CODE>AttachmentPart</CODE> object
     *         in bytes or -1 if the size cannot be determined
     * @throws javax.xml.soap.SOAPException if the content of this
     *                                      attachment is corrupted of if there was an exception
     *                                      while trying to determine the size.
     */
    public int getSize() throws SOAPException {
        if (dataHandler == null) {
            return -1;
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            dataHandler.writeTo(bout);
        } catch (java.io.IOException ex) {
            throw new SOAPException(ex);
        }
        return bout.size();
    }

    /**
     * Clears out the content of this <CODE>
     * AttachmentPart</CODE> object. The MIME header portion is left
     * untouched.
     */
    public void clearContent() {
        dataHandler = null;
        omText = null;
    }

    /**
     * Gets the content of this <code>AttachmentPart</code> object as a Java
     * object. The type of the returned Java object depends on
     * <ol>
     * <li> the
     * <code>DataContentHandler</code> object that is used to interpret the bytes
     * </li>
     * <li> the <code>Content-Type</code> given in the header</li>
     * </ol>
     * <p/>
     * For the MIME content types "text/plain", "text/html" and "text/xml", the
     * <code>DataContentHandler</code> object does the conversions to and
     * from the Java types corresponding to the MIME types.
     * For other MIME types,the <code>DataContentHandler</code> object
     * can return an <code>InputStream</code> object that contains the content data
     * as raw bytes.
     * <p/>
     * A JAXM-compliant implementation must, as a minimum, return a
     * <code>java.lang.String</code> object corresponding to any content
     * stream with a <code>Content-Type</code> value of
     * <code>text/plain</code>, a
     * <code>javax.xml.transform.StreamSource</code> object corresponding to a
     * content stream with a <code>Content-Type</code> value of
     * <code>text/xml</code>, a <code>java.awt.Image</code> object
     * corresponding to a content stream with a
     * <code>Content-Type</code> value of <code>image/gif</code> or
     * <code>image/jpeg</code>.  For those content types that an
     * installed <code>DataContentHandler</code> object does not understand, the
     * <code>DataContentHandler</code> object is required to return a
     * <code>java.io.InputStream</code> object with the raw bytes.
     *
     * @return a Java object with the content of this <CODE>
     *         AttachmentPart</CODE> object
     * @throws javax.xml.soap.SOAPException if there is no content set
     *                                      into this <CODE>AttachmentPart</CODE> object or if there
     *                                      was a data transformation error
     */
    public Object getContent() throws SOAPException {
        if (dataHandler == null) {
            throw new SOAPException("No content is present in this AttachmentPart");
        }
        try {
            String ContentType = dataHandler.getContentType();
            if (ContentType.equals("text/plain") ||
                ContentType.equals("text/xml") ||
                ContentType.equals("text/html")) {

                //For these content types underlying DataContentHandler surely does
                //the conversion to appropriate java object and we will return that java object
                return dataHandler.getContent();
            } else {
                try {
                    return dataHandler.getContent();
                } catch (UnsupportedDataTypeException e) {

                    //If the underlying DataContentHandler can't handle the object contents,
                    //we will return an inputstream of raw bytes represneting the content data
                    return dataHandler.getDataSource().getInputStream();
                }
            }
        } catch (IOException e) {
            throw new SOAPException(e);
        }
    }

    /**
     * Sets the content of this attachment part to that of the
     * given <CODE>Object</CODE> and sets the value of the <CODE>
     * Content-Type</CODE> header to the given type. The type of the
     * <CODE>Object</CODE> should correspond to the value given for
     * the <CODE>Content-Type</CODE>. This depends on the particular
     * set of <CODE>DataContentHandler</CODE> objects in use.
     *
     * @param object      the Java object that makes up
     *                    the content for this attachment part
     * @param contentType the MIME string that
     *                    specifies the type of the content
     * @throws IllegalArgumentException if
     *                                  the contentType does not match the type of the content
     *                                  object, or if there was no <CODE>
     *                                  DataContentHandler</CODE> object for this content
     *                                  object
     * @see #getContent()
     */
    public void setContent(Object object, String contentType) {

        //TODO: need to check whether the type of the content object matches contentType
        //TODO: need to check whether there is a DataContentHandler for this object
        setDataHandler(new DataHandler(object, contentType));
    }

    /**
     * Gets the <CODE>DataHandler</CODE> object for this <CODE>
     * AttachmentPart</CODE> object.
     *
     * @return the <CODE>DataHandler</CODE> object associated with
     *         this <CODE>AttachmentPart</CODE> object
     * @throws javax.xml.soap.SOAPException if there is
     *                                      no data in this <CODE>AttachmentPart</CODE> object
     */
    public DataHandler getDataHandler() throws SOAPException {
        if (getContent() == null) {
            throw new SOAPException("No Content present in the Attachment part");
        }
        return dataHandler;
    }

    /**
     * Sets the given <CODE>DataHandler</CODE> object as the
     * data handler for this <CODE>AttachmentPart</CODE> object.
     * Typically, on an incoming message, the data handler is
     * automatically set. When a message is being created and
     * populated with content, the <CODE>setDataHandler</CODE>
     * method can be used to get data from various data sources into
     * the message.
     *
     * @param datahandler <CODE>DataHandler</CODE> object to
     *                    be set
     * @throws IllegalArgumentException if
     *                                  there was a problem with the specified <CODE>
     *                                  DataHandler</CODE> object
     */
    public void setDataHandler(DataHandler datahandler) {
        if (datahandler != null) {
            this.dataHandler = datahandler;
            setMimeHeader(HTTPConstants.HEADER_CONTENT_TYPE, datahandler.getContentType());
            omText = DOOMAbstractFactory.getOMFactory().createText(dataHandler, true);
        }
    }

    /**
     * Removes all MIME headers that match the given name.
     *
     * @param header - the string name of the MIME
     *               header/s to be removed
     */
    public void removeMimeHeader(String header) {
        mimeHeaders.removeHeader(header);
    }

    /**
     * Removes all the MIME header entries.
     */
    public void removeAllMimeHeaders() {
        mimeHeaders.removeAllHeaders();
    }

    /**
     * Gets all the values of the header identified by the given
     * <CODE>String</CODE>.
     *
     * @param name the name of the header; example:
     *             "Content-Type"
     * @return a <CODE>String</CODE> array giving the value for the
     *         specified header
     * @see #setMimeHeader(String, String) setMimeHeader(java.lang.String, java.lang.String)
     */
    public String[] getMimeHeader(String name) {
        return mimeHeaders.getHeader(name);
    }

    /**
     * Changes the first header entry that matches the given name
     * to the given value, adding a new header if no existing
     * header matches. This method also removes all matching
     * headers but the first.
     * <p/>
     * <P>Note that RFC822 headers can only contain US-ASCII
     * characters.</P>
     *
     * @param name  a <CODE>String</CODE> giving the
     *              name of the header for which to search
     * @param value a <CODE>String</CODE> giving the
     *              value to be set for the header whose name matches the
     *              given name
     * @throws IllegalArgumentException if
     *                                  there was a problem with the specified mime header name
     *                                  or value
     */
    public void setMimeHeader(String name, String value) {
        mimeHeaders.setHeader(name, value);
    }

    /**
     * Adds a MIME header with the specified name and value to
     * this <CODE>AttachmentPart</CODE> object.
     * <p/>
     * <P>Note that RFC822 headers can contain only US-ASCII
     * characters.</P>
     *
     * @param name  a <CODE>String</CODE> giving the
     *              name of the header to be added
     * @param value a <CODE>String</CODE> giving the
     *              value of the header to be added
     * @throws IllegalArgumentException if
     *                                  there was a problem with the specified mime header name
     *                                  or value
     */
    public void addMimeHeader(String name, String value) {
        mimeHeaders.addHeader(name, value);
    }

    /**
     * Retrieves all the headers for this <CODE>
     * AttachmentPart</CODE> object as an iterator over the <CODE>
     * MimeHeader</CODE> objects.
     *
     * @return an <CODE>Iterator</CODE> object with all of the Mime
     *         headers for this <CODE>AttachmentPart</CODE> object
     */
    public Iterator getAllMimeHeaders() {
        return mimeHeaders.getAllHeaders();
    }

    /**
     * Retrieves all <CODE>MimeHeader</CODE> objects that match
     * a name in the given array.
     *
     * @param names a <CODE>String</CODE> array with
     *              the name(s) of the MIME headers to be returned
     * @return all of the MIME headers that match one of the names
     *         in the given array as an <CODE>Iterator</CODE>
     *         object
     */
    public Iterator getMatchingMimeHeaders(String names[]) {
        return mimeHeaders.getMatchingHeaders(names);
    }

    /**
     * Retrieves all <CODE>MimeHeader</CODE> objects whose name
     * does not match a name in the given array.
     *
     * @param names a <CODE>String</CODE> array with
     *              the name(s) of the MIME headers not to be returned
     * @return all of the MIME headers in this <CODE>
     *         AttachmentPart</CODE> object except those that match one
     *         of the names in the given array. The nonmatching MIME
     *         headers are returned as an <CODE>Iterator</CODE>
     *         object.
     */
    public Iterator getNonMatchingMimeHeaders(String names[]) {
        return mimeHeaders.getNonMatchingHeaders(names);
    }

    /**
     * Retrieve the OMText
     *
     * @return the OMText
     * @throws SOAPException If omText is not available
     */
    public OMText getOMText() throws SOAPException {
        if (omText == null) {
            throw new SOAPException("OMText set to null");
        }
        return omText;
    }

    public TextImpl getText(DocumentImpl doc) {
        return new TextImpl(doc, omText.getText());
    }
}
