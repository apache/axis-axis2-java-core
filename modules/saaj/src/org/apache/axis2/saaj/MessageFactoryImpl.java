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

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ashutosh Shahi ashutosh.shahi@gmail.com
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class MessageFactoryImpl extends MessageFactory {

    /* (non-Javadoc)
     * @see javax.xml.soap.MessageFactory#createMessage()
     */
    public SOAPMessage createMessage() throws SOAPException {
        SOAPEnvelopeImpl env = new SOAPEnvelopeImpl();
        SOAPMessageImpl message = new SOAPMessageImpl(env);
        return message;
    }

    /* (non-Javadoc)
     * @see javax.xml.soap.MessageFactory#createMessage(javax.xml.soap.MimeHeaders, java.io.InputStream)
     */
    public SOAPMessage createMessage(MimeHeaders mimeheaders,
                                     InputStream inputstream) throws IOException, SOAPException {
        // TODO Auto-generated method stub
        SOAPMessageImpl message = new SOAPMessageImpl(inputstream, false, mimeheaders);
        return message;
    }

}
