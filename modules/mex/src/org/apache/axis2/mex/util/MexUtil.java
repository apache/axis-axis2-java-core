/*
* Copyright 2007 The Apache Software Foundation.
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

package org.apache.axis2.mex.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.mex.MexException;
import org.apache.axis2.mex.MexConstants;
import org.apache.axis2.dataRetrieval.OutputForm;

public class MexUtil {
	
	/**
	 * Answer SOAPVersion for specified envelope
	 * @param envelope SOAP Envelope
	 * @return version of SOAP
	 * @throws MexException
	 */
	public static int getSOAPVersion(SOAPEnvelope envelope) throws MexException {
		String namespaceName = envelope.getNamespace().getNamespaceURI();
		if (namespaceName.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI))
			return MexConstants.SOAPVersion.v1_1;
		else if (namespaceName.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI))
			return MexConstants.SOAPVersion.v1_2;
		else
			throw new MexException("Unknown SOAP version");
	}

	/**
	 * Answer SOAPFactory corresponding to specified SOAP namespace URI
	 * @param soapNameSpaceURI soap namespace uri
	 * @return
	 * @throws MexException
	 */
	public static SOAPFactory getSOAPFactory(String soapNameSpaceURI) throws MexException {
			if (soapNameSpaceURI.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI))
			return  OMAbstractFactory.getSOAP11Factory();
		else if (soapNameSpaceURI.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI))
			return OMAbstractFactory.getSOAP12Factory();
		else
			throw new MexException("Unknown SOAP soapNameSpaceURI");
	}

	/**
	 * Answers SOAPFactory corresponding to specified SOAP Version
	 * @param SOAPVersion SOAP version
	 * @return SOAPFactory
	 */
	public static SOAPFactory getSOAPFactory(int SOAPVersion) {

		if (SOAPVersion == MexConstants.SOAPVersion.v1_1)
			return OMAbstractFactory.getSOAP11Factory();
		else
			return OMAbstractFactory.getSOAP12Factory();

	}
	
	
	// Return all supported output forms
	public static OutputForm[] allSupportedOutputForms(){
		OutputForm[]outputforms = new OutputForm[] {
		OutputForm.INLINE_FORM,
		OutputForm.LOCATION_FORM, 
		OutputForm.REFERENCE_FORM};
		return outputforms;
	}
	
	/**
	 * Answers WS-Addressing namespace
	 * @param toAddress To Address element
	 * @return OMNamespaceImpl WS-Addressing namespace
	 * @throws AxisFault
	 */
	
	 public static OMNamespaceImpl getAddressingNameSpace(OMElement toAddress)
			throws MexException {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespaceImpl wsa = null;
		try {
			String prefix = toAddress.getNamespace().getPrefix();
			String nsURI = toAddress.getNamespace().getNamespaceURI();
			wsa = (OMNamespaceImpl) factory.createOMNamespace(nsURI, prefix);
		} catch (Exception e) {
		    throw new MexException(e);
		}
		return wsa;

	}

}
