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

package javax.xml.ws.handler.soap;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

/**
 * public interface SOAPMessageContext
 * extends <code>MessageContext</code>
 * <p>
 * The interface javax.xml.rpc.handler.soap.SOAPMessageContext provides access to the SOAP message for either RPC
 *  request or response. The javax.xml.soap.SOAPMessage specifies the standard Java API for the representation of a SOAP
 * 1.1 message with attachments.
 * @version 1.0
 * @author shaas02
 * @see SOAPMessage
 */
public interface SOAPMessageContext extends MessageContext {

	/**
	 * Gets the SOAPMessage from this message context. Modifications to the returned SOAPMessage change the message
	 * in-place, there is no need to susequently call setMessage.
	 * @return Returns the SOAPMessage; returns null if no SOAPMessage is present in this message context
	 */
	javax.xml.soap.SOAPMessage getMessage();
	
	/**
	 * Sets the SOAPMessage in this message context
	 * @param message - SOAP message
	 * @throws WebServiceException - If any error during the setting of the SOAPMessage in this message context
	 * @throws java.lang.UnsupportedOperationException - If this operation is not supported
	 * 
	 */
	void setMessage(javax.xml.soap.SOAPMessage message) throws WebServiceException, java.lang.UnsupportedOperationException;
	
	/**
	 * Gets headers that have a particular qualified name from the message in the message context. Note that a SOAP message
	 * can contain multiple headers with the same qualified name.
	 * @param header - The XML qualified name of the SOAP header(s).
	 * @param context - The JAXBContext that should be used to unmarshall the header
	 * @param allRoles - If true then returns headers for all SOAP roles, if false then only returns headers targetted at the
	 * roles currently being played by this SOAP node, see getRoles.
	 * @return An array of unmarshalled headers; returns an empty array if no message is present in this message context or no
	 * headers match the supplied qualified name.
	 * @throws WebServiceException  - If an error occurs when using the supplied JAXBContext to unmarshall. The cause of the
	 * JAXRPCException is the original JAXBException.
	 */
	java.lang.Object[] getHeaders(javax.xml.namespace.QName header,
			javax.xml.bind.JAXBContext context,
			boolean allRoles) throws WebServiceException;
	
	/**
	 * Gets the SOAP actor roles associated with an execution of the handler chain. Note that SOAP actor roles apply to the
	 * SOAP node and are managed using SOAPBinding.setRoles and SOAPBinding.getRoles. Handler instances in the
	 * handler chain use this information about the SOAP actor roles to process the SOAP header blocks. Note that the SOAP
	 * actor roles are invariant during the processing of SOAP message through the handler chain.
	 * @return Array of URIs for SOAP actor roles	 
	 */
	java.util.Set<java.net.URI> getRoles();
}
