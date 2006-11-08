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

import javax.xml.ws.soap.SOAPBinding;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;

/**
 * Protocol
 * Each message has a protocol (soap11, soap12, rest)
 * This enum represents the protocol within the Message sub-component
 */
public enum Protocol {
	soap11, soap12, rest, unknown;
    
    /**
     * Return the right value for the Protocol based on the binding
     * URL that was passed in.
     * 
     * @param url
     * @return
     */
    public static Protocol getProtocolForBinding(String url) throws MessageException {
        //TODO: Add support for more URLs as needed.
        if (url.equals(SOAPBinding.SOAP11HTTP_BINDING) || 
        	url.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING)) {
            return Protocol.soap11;
        }
        else if (url.equals(SOAPBinding.SOAP12HTTP_BINDING) ||
        		 url.equals(SOAPBinding.SOAP12HTTP_BINDING)) {
            return Protocol.soap12;
        }
        else {
            throw ExceptionFactory.makeMessageException(Messages.getMessage("protoNotFound00", url));
        }
    }
}
