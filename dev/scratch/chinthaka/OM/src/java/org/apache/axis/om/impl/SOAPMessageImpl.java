package org.apache.axis.om.impl;

import org.apache.axis.om.*;
import org.apache.axis.om.soap.SOAPEnvelope;
import org.apache.axis.om.soap.SOAPMessage;
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
 * <p/>
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Oct 7, 2004
 * Time: 10:52:17 AM
 */
public class SOAPMessageImpl extends OMNodeImpl implements SOAPMessage {

    private SOAPEnvelope envelope;
    private OMXMLParserWrapper parserWrapper;

    public SOAPMessageImpl(OMXMLParserWrapper parserWrapper) {
        this.parserWrapper = parserWrapper;
    }

    public SOAPMessageImpl(Object[] obj) {
        //TODO create the OM from Obj
    }


    public void setEnvelope(SOAPEnvelopeImpl root) {
        this.envelope = root;
    }

    /**
     * Get the envelope element of this document as a SOAPEnvelope
     *
     * @return the envelope element
     */
    public SOAPEnvelope getEnvelope() {
        if (envelope == null) {
            parserWrapper.next();
        }
        return envelope;
    }

    /**
     * Returns all the transport-specific MIME headers for this
     * <CODE>SOAPMessage</CODE> object in a transport-independent
     * fashion.
     *
     * @return a <CODE>MimeHeaders</CODE> object containing the
     *         <CODE>MimeHeader</CODE> objects
     */
    public MimeHeaders getMimeHeaders() {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    public void setEnvelope(SOAPEnvelope envelope) {
        this.setEnvelope((SOAPEnvelopeImpl)envelope);
    }
}
