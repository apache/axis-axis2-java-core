/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.savan.filters;

import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.savan.SavanException;
/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import org.jaxen.JaxenException;

/**
 * A filter that does filtering of messages based on a XPath string.
 * 
 */
public class XPathBasedFilter extends Filter {

	private String XPathString = null;
	
	public String getXPathString() {
		return XPathString;
	}

	public void setXPathString(String XPathString) {
		this.XPathString = XPathString;
	}

	/**
	 * This method may fail due to the JIRA issues WS-Commons(40) amd WS-Commons (41)
	 */
	public boolean checkEnvelopeCompliance(SOAPEnvelope envelope) throws SavanException {
		OMElement firstChild = envelope.getBody().getFirstElement();
		if (firstChild==null)
			return false;
		
		if (XPathString==null)
			return true;
		
		try {
			AXIOMXPath xpath = new AXIOMXPath (XPathString);
			List resultList = xpath.selectNodes(firstChild);

            return resultList.size() > 0;
		} catch (JaxenException e) {
			throw new SavanException (e);
		}
	}

	public void setUp(OMNode element) {
		if (!(element instanceof OMText))
			throw new IllegalArgumentException ("Cannot determine a valid XPath string");
		
		OMText text = (OMText) element;
		XPathString = text.getText();
	}

	public Object getFilterValue() {
		return XPathString;
	}

	
}
