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

import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.savan.SavanException;

/**
 * Defines a filter used by Savan. 
 *
 */
public abstract class Filter {
	
	/**
	 * To check weather the passed envelope is compliant with the current filter.
	 * @param envelope
	 * @return
	 * @throws SavanException
	 */
	public abstract boolean checkEnvelopeCompliance (SOAPEnvelope envelope) throws SavanException;
	
	/**
	 * To initialize the filter. The filter value should be sent to the argument
	 * (for e.g. As a OMText for a String)
	 * 
	 * @param element
	 */
	public abstract void setUp (OMNode element);
	
	/**
	 * Returns a previously set filter value.
	 * 
	 * @return
	 */
	public abstract Object getFilterValue ();
}
