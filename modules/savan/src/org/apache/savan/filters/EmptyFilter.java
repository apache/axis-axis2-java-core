package org.apache.savan.filters;

import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.savan.SavanException;

/**
 * This filter does not do any affective filtering.
 * May be the default for some protocols.
 */
public class EmptyFilter extends Filter {

	public boolean checkEnvelopeCompliance(SOAPEnvelope envelope) throws SavanException {
		return true;
	}

	public Object getFilterValue() {
		return null;
	}

	public void setUp(OMNode element) {
	}
	
}
