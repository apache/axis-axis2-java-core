package org.apache.axis2.security;

import org.apache.axis2.security.handler.WSSHandlerConstants;
import org.apache.axis2.security.handler.config.InflowConfiguration;
import org.apache.axis2.security.handler.config.OutflowConfiguration;

public class AddressingMTOMSecurityTest extends InteropTestBase {

	protected OutflowConfiguration getOutflowConfiguration() {
		OutflowConfiguration ofc = new OutflowConfiguration();
		
		ofc.setActionItems("Timestamp Signature Encrypt");
		ofc.setUser("alice");
		ofc.setEncryptionUser("bob");
		ofc.setSignaturePropFile("interop.properties");
		ofc.setPasswordCallbackClass("org.apache.axis2.security.PWCallback");
		ofc.setSignatureKeyIdentifier(WSSHandlerConstants.SKI_KEY_IDENTIFIER);
		ofc.setEncryptionKeyIdentifier(WSSHandlerConstants.SKI_KEY_IDENTIFIER);
		ofc.setSignatureParts("{Element}{http://schemas.xmlsoap.org/ws/2004/08/addressing}To;{Element}{http://schemas.xmlsoap.org/ws/2004/08/addressing}ReplyTo;{Element}{http://schemas.xmlsoap.org/ws/2004/08/addressing}MessageID;{Element}{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Timestamp");
		ofc.setOptimizeParts("//xenc:EncryptedData/xenc:CipherData/xenc:CipherValue");
		
		return ofc;
	}

	protected InflowConfiguration getInflowConfiguration() {
		InflowConfiguration ifc = new InflowConfiguration();
		
		ifc.setActionItems("Timestamp Signature Encrypt");
		ifc.setPasswordCallbackClass("org.apache.axis2.security.PWCallback");
		ifc.setSignaturePropFile("interop.properties");
		
		return ifc;
	}

	protected String getClientRepo() {
		return COMPLETE_CLIENT_REPOSITORY;
	}

	protected String getServiceRepo() {
		return COMPLETE_SERVICE_REPOSITORY;
	}
	
	
}
