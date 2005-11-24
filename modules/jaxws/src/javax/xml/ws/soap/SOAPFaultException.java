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

package javax.xml.ws.soap;

import javax.xml.ws.ProtocolException;

/**
 * The SOAPFaultException exception represents a SOAP 1.1 or 1.2 fault.
 * <p>
 * A SOAPFaultException wraps a SAAJ SOAPFault  that manages the SOAP-specific representation of faults. The
 * createFault method of javax.xml.soap.SOAPFactory may be used to create an instance of
 * javax.xml.soap.SOAPFault for use with the constructor. SOAPBinding contains an accessor for the SOAPFactory used by
 *  the binding instance.
 *  <p>
 *  Note that the value of getFault is the only part of the exception used when searializing a SOAP fault.
 *  <p>
 *  Refer to the SOAP specification for a complete description of SOAP faults.
 * 
 * @since JAX-WS 2.0
 * @author sunja07
 */
public class SOAPFaultException extends ProtocolException {
	
	/**
	 * Constructor for SOAPFaultException
	 * @param fault - SOAPFault representing the fault
	 * @see javax.xml.soap.SOAPFactory#createFault
	 */
	public SOAPFaultException(javax.xml.soap.SOAPFault fault){
		soapFault = fault;
	}
	
	/**
	 * Gets the embedded SOAPFault instance.
	 * @return javax.xml.soap.SOAPFault SOAP fault element
	 */
	public javax.xml.soap.SOAPFault getFault(){
		return soapFault;
	}

	/**
	 * embedded soap fault instance
	 */
	private javax.xml.soap.SOAPFault soapFault;
}
