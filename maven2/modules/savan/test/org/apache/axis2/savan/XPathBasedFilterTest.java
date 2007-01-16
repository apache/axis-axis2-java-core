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

package org.apache.axis2.savan;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.savan.filters.Filter;
import org.apache.savan.filters.XPathBasedFilter;

public class XPathBasedFilterTest extends TestCase {

	String filterString = "//elem1";
	
	public void testMessageFiltering () throws AxisFault {
		SOAPEnvelope envelope = createTestEnvelope ();
		
		OMNode filterNode = getFilterElement ();
		Filter filter = new XPathBasedFilter ();
		filter.setUp(filterNode);
		
		assertTrue (filter.checkEnvelopeCompliance(envelope));
	}
	
	private SOAPEnvelope createTestEnvelope () {
		SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		
		OMElement elem1 = factory.createOMElement("elem1",null);
		OMElement elem2 = factory.createOMElement("elem2",null);
		OMElement elem3 = factory.createOMElement("elem3",null);

		elem2.addChild(elem3);
		elem1.addChild(elem2);
		
		envelope.getBody().addChild(elem1);
		factory.createOMDocument().addChild(envelope);
		
		return envelope;
	}
	
	private OMNode getFilterElement () {
		SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
		OMText text = factory.createOMText(filterString);
		return text;
	}
	
}
