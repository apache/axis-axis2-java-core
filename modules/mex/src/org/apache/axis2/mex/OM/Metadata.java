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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.mex.MexConstants;

/**
 * 
 * Class implementing  mex:Metadata element 
 *
 */

public class Metadata extends MexOM implements IMexOM {
	private String namespaceValue = null;
	private OMFactory factory;
	private List  metadataSections = new ArrayList(); 
	private OMAttribute attribute = null;
	
	/**
	 * 
	 * @param defaultFactory
	 * @param namespaceValue
	 * @throws MexOMException
	 */

	public Metadata(OMFactory defaultFactory, String namespaceValue) throws MexOMException  {
		this.factory = defaultFactory;
		this.namespaceValue = namespaceValue;
	}

	
	public OMElement toOM() throws MexOMException
	{
		OMNamespace mexNamespace = factory.createOMNamespace(namespaceValue,MexConstants.SPEC.NS_PREFIX);
		OMElement metadata = factory.createOMElement(MexConstants.SPEC.METADATA, mexNamespace);

		Iterator sections = metadataSections.iterator();
		while (sections.hasNext()) {
			MetadataSection aSection = (MetadataSection) sections.next();
			metadata.addChild(aSection.toOM());
		}
		if (attribute != null){
			metadata.addAttribute(attribute); //???
		}
		return metadata;
	}
	
	public void setMetadatSections(List in_metadataSections) {
		metadataSections = in_metadataSections;
	}
	
	public void addMetadatSections(List in_metadataSections) {
		Iterator sections = in_metadataSections.iterator();
		while (sections.hasNext()) {
			addMetadatSection((MetadataSection) sections.next());
		}
	}

	public void addMetadatSection(MetadataSection section) {
		metadataSections.add(section);
	}
	
	public MetadataSection[] getMetadatSections() {
		return (MetadataSection[])metadataSections.toArray(new MetadataSection[0]);
	}
	
	public void setAttribute(OMAttribute in_attribute) {
		attribute = in_attribute;
	}

}
