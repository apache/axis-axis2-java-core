package org.apache.axis.om.soap;

import org.apache.axis.om.OMNode;
import org.apache.axis.om.mime.MimeHeaders;


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
 *
 * @author Axis team
 *         Date: Oct 4, 2004
 *         Time: 4:48:10 PM
 */
public interface SOAPMessage extends OMNode {

    /**
     * Get the root element of this document as a SOAPEnvelope
     *
     * @return the root element
     */
    public SOAPEnvelope getEnvelope();
    public void setEnvelope(SOAPEnvelope soapEnvelope);

    /**
     * Returns all the transport-specific MIME headers for this
     * <CODE>SOAPMessage</CODE> object in a transport-independent
     * fashion.
     * @return a <CODE>MimeHeaders</CODE> object containing the
     *     <CODE>MimeHeader</CODE> objects
     */
    public abstract MimeHeaders getMimeHeaders();

}
