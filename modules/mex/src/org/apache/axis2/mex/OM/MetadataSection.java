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


import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.mex.MexConstants;


/**
 * Class implemented for MetadataSection element defined in 
 * the WS-MEX spec.
 *
 */
public  class MetadataSection extends MexOM implements IMexOM {
	private String namespaceValue = null;
	private OMFactory factory;
	// Choices of content: inline metadata, MetadataReference, Location
	private String anyAttribute = null;
	private OMNode inlineData = null;
	//private String inlineData = null;
	private Location location = null;
	private MetadataReference ref = null;
	
    // Attributes
    private String dialet;
    private String identifier;
    
    /**
     * Constructor
     * @param defaultFactory
     * @param namespaceValue
     * @throws MexOMException
     */
	public MetadataSection(OMFactory defaultFactory, String namespaceValue) throws MexOMException  {
		this.factory = defaultFactory;
		this.namespaceValue = namespaceValue;
	}
	
		
	/**
	 * Convert MetadatSection content to the OMElement representation.
	 * @return OMElement representation of MetadataSection.
	 * @throws MexOMException
	 */
	public OMElement toOM() throws MexOMException {
		OMNamespace mexNamespace = factory.createOMNamespace(namespaceValue,
				MexConstants.SPEC.NS_PREFIX);
		OMElement metadataSection = factory.createOMElement(
				MexConstants.SPEC.METADATA_SECTION, mexNamespace);

		// dialet is required
		if (dialet == null) {
			throw new MexOMException("Dialet was not set. Dialet must be set.");
		}
		OMAttribute dialetAttrib = factory.createOMAttribute(
				MexConstants.SPEC.DIALECT, null, dialet);

		metadataSection.addAttribute(dialetAttrib);

		if (identifier != null && identifier.trim().length() > 0) {
			OMAttribute identifierAttrib = factory.createOMAttribute(
					MexConstants.SPEC.IDENTIFIER, null, identifier);

			metadataSection.addAttribute(identifierAttrib);
		}
		if (anyAttribute != null) {
			OMAttribute anyAttrib = factory.createOMAttribute("AnyAttribute",
					null, anyAttribute);

			metadataSection.addAttribute(anyAttrib);
		}

		if (inlineData != null) {
			metadataSection.addChild(inlineData);
			
		}

		if (location != null) {
			metadataSection.addChild(location.toOM());
		}

		if (ref != null) {
			metadataSection.addChild(ref.toOM());
		}
		return metadataSection;

	}
	
	public String getDialet() {
		return dialet;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getanyAttribute() {
		return anyAttribute;
	}
	public Location getLocation() {
		return location;
	}
	
	public OMNode getInlineData() {
		return inlineData;
	}
	
	public MetadataReference getMetadataReference() {
		return ref;
	}
	
	public void setIdentifier(String in_identifier) {
		identifier =in_identifier;
	}
	
	public void setDialet(String in_dialet) {
		dialet = in_dialet;
	}
	
	
	public void setLocation(Location in_location) {
		location = in_location;
	}
	
	public void setinlineData(Object in_inlineData) {
		inlineData = (OMNode)in_inlineData;
	}
	
	public void setMetadataReference(MetadataReference in_ref) {
		ref = in_ref;
	}
}
