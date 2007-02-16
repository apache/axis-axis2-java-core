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

package org.apache.axis2.mex.OM;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
//import org.apache.axis2.addressing.EndpointReference;
//import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.mex.MexConstants;

/**
 * Class implemented for MetadataReference element defined in 
 * the WS-MetadataExchange spec.
 *
 */

public class MetadataReference extends MexOM implements IMexOM {

	private OMFactory factory;
	private OMElement eprElement = null; 

	private String namespaceValue = null;

	/**
	 * Constructor
	 * @param defaultFactory
	 * @param namespaceValue
	 * @throws MexOMException
	 */

	public MetadataReference(OMFactory defaultFactory, String namespaceValue)
	 throws MexOMException {
		if (!isNamespaceSupported(namespaceValue))
			throw new MexOMException("Unsupported namespace");

		this.factory = defaultFactory;
		this.namespaceValue = namespaceValue;
		}

	/**
	 * Convert MetadatReference object content to the OMElement representation.
	 * @return OMElement representation of MetadatReference.
	 * @throws MexOMException
	 */
	public OMElement toOM() throws MexOMException {
		if (eprElement == null) {
			throw new MexOMException(
					"Must have EndpointReference element in MetadataReference");
		}

		OMElement metadataRef = null;
		/*if (eprElement.getLocalName() == "EndpointReference") {
			EndpointReference epr;
			try {
				epr = EndpointReferenceHelper.fromOM(eprElement);
				metadataRef = EndpointReferenceHelper.toOM(factory, epr,
						new QName(namespaceValue,
								MexConstants.SPEC.METADATA_REFERENCE,
								MexConstants.SPEC.NS_PREFIX), eprElement
								.getNamespace().getNamespaceURI());

			} catch (AxisFault e) {
				throw new MexOMException(e);
			}
		} */

		return metadataRef;
	}
	
	/**
	 * Set EPR element
	 * 
	 * @param element
	 */
	public void setEPRElement(OMElement element) {
		eprElement = element;
	}
	
}
