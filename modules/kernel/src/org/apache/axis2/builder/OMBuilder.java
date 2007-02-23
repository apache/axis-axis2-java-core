package org.apache.axis2.builder;

import java.io.InputStream;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

public interface OMBuilder {
	
    /**
     * @return Returns the document element.
     */
	public OMElement processDocument(InputStream inputStream, MessageContext messageContext) throws AxisFault;
}
