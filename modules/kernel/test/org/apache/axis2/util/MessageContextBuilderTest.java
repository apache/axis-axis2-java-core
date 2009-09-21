package org.apache.axis2.util;

import junit.framework.TestCase;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axis2.namespace.Constants;

public class MessageContextBuilderTest extends TestCase {
    private OMNamespaceImpl nsp = new OMNamespaceImpl(Constants.URI_SOAP11_ENV, "soapenv");

    public void testSwitchNamespacePrefixes()
            throws Exception {

        // Incoming envelope has a "soapenv" prefix
        assertEquals("soapenv:" + SOAPConstants.FAULT_CODE_VERSION_MISMATCH,
                MessageContextBuilder.switchNamespacePrefix("soapenv:" + SOAPConstants.FAULT_CODE_VERSION_MISMATCH, nsp));

        // Incoming envelope has a "s" prefix 
        assertEquals("soapenv:" + SOAPConstants.FAULT_CODE_VERSION_MISMATCH,
                MessageContextBuilder.switchNamespacePrefix("s:" + SOAPConstants.FAULT_CODE_VERSION_MISMATCH, nsp));

        // Incoming envelope uses default namespace and no prefixes 
        assertEquals("soapenv:" + SOAPConstants.FAULT_CODE_VERSION_MISMATCH,
                MessageContextBuilder.switchNamespacePrefix(":" + SOAPConstants.FAULT_CODE_VERSION_MISMATCH, nsp));
    }
}