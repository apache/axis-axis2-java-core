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

package javax.xml.ws;

/**
 * Interface LogicalMessage
 * The LogicalMessage interface represents a protocol agnostic XML message 
 * and contains methods that provide access to the payload of the message.
 * 
 * @since JAX-WS 2.0
 * @author sunja07
 */
public interface LogicalMessage {
	
	/**
	 * Method getPayload
	 * Gets the message payload as an XML source, may be called multiple times
	 * on the same LogicalMessage instance, always returns a new Source that 
	 * may be used to retrieve the entire message payload.
	 * If the returned Source is an instance of DOMSource, then modifications 
	 * to the encapsulated DOM tree change the message payload in-place, there
	 * is no need to susequently call setPayload. Other types of Source 
	 * provide only read access to the message payload.
	 * @return The contained message payload; returns null if no payload is 
	 * present in this message.
	 */
	javax.xml.transform.Source getPayload();

	/**
	 * Method getPayload
	 * Gets the message payload as a JAXB object. Note that there is no 
	 * connection between the returned object and the message payload, changes
	 * to the payload require calling setPayload.
	 * @param context The JAXBContext that should be used to unmarshall the 
	 * message payload
	 * @return The contained message payload; returns null if no payload is 
	 * present in this message
	 * @throws WebServiceException If an error occurs when using a supplied 
	 * JAXBContext to unmarshall the payload. The cause of the JAXRPCException
	 * is the original JAXBException.
	 */
	java.lang.Object getPayload(javax.xml.bind.JAXBContext context) throws 
	WebServiceException;
	
	/**
	 * Method setPayload
	 * Sets the message payload
	 * @param payload message payload 
	 * @throws WebServiceException If any error during the setting of the payload
	 *  in this message java.lang.UnsupportedOperationException - If this 
	 *  operation is not supported.
	 */
	void setPayload(javax.xml.transform.Source payload) throws WebServiceException;	
	
	/**
	 * Method setPayload
	 * Sets the message payload 
	 * @param payload message payload
	 * @param context The JAXBContext that should be used to marshall the 
	 * payload 
	 * @throws java.lang.UnsupportedOperationException If this operation is 
	 * not supported
	 * @throws WebServiceException If an error occurs when using the supplied 
	 * JAXBContext to marshall the payload. The cause of the JAXRPCException 
	 * is the original JAXBException.
	 */
	void setPayload(java.lang.Object payload,
            javax.xml.bind.JAXBContext context) throws WebServiceException;
}
