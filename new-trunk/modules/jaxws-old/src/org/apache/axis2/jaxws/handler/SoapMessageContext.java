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
package org.apache.axis2.jaxws.handler;

import java.net.URI;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;

import org.apache.axis2.jaxws.core.MessageContext;

/**
 * The SOAPMessageContext is the context handed to SOAP-based application
 * handlers.  It provides access to the SOAP message that represents the
 * request or response via SAAJ.  It also allows access to any properties
 * that have been registered and set on the MessageContext.
 */
public class SoapMessageContext extends ProtectedMessageContext 
    implements javax.xml.ws.handler.soap.SOAPMessageContext {

	
    public SoapMessageContext() {
		super();
	}

	public SoapMessageContext(MessageContext mc) {
		super(mc);
	}

	public Object[] getHeaders(QName qname, JAXBContext jaxbcontext, boolean flag) {
        return null;
    }

    public SOAPMessage getMessage() {
        return null;
    }

    public Set<URI> getRoles() {
        return null;
    }

    public void setMessage(SOAPMessage soapmessage) {
    }
}
