/*
* Copyright 2005 The Apache Software Foundation.
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


package org.apache.axis2.fault;

import org.apache.ws.commons.soap.SOAPFault;

import java.util.Iterator;

/**
 * This is an interface to be implemented by exceptions that can generate
 * their own SOAPFault directly.
 * Axis2 can extract the body and any headers and use them in the response.
 */
public interface SoapFaultSource {

    /**
     * Get any Headers to include in the message.
     * Most
     *
     * @return an iterator over headers, or null for no headers of interest.
     */
    Iterator getHeaders();

    /**
     * The full SOAPFault to send back. This will become the body of a message.
     *
     * @return the SOAPFault to return as the body of a message.
     */
    SOAPFault getSOAPFault();
}
