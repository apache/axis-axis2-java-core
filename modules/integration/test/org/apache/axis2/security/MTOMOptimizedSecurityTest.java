/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.axis2.security;

import org.apache.axis2.security.handler.WSSHandlerConstants;
import org.apache.axis2.security.handler.config.InflowConfiguration;
import org.apache.axis2.security.handler.config.OutflowConfiguration;

/**
 * Testing optimizing the base 64 elements with 
 * <code><parameter name="optimizeParts" locked="false">//xenc:EncryptedData/xenc:CipherData/xenc:CipherValue</parameter></code>
 */
public class MTOMOptimizedSecurityTest extends InteropTestBase {

	protected OutflowConfiguration getOutflowConfiguration() {
		OutflowConfiguration ofc = new OutflowConfiguration();
		
		ofc.setActionItems("Signature Encrypt Timestamp");
		ofc.setUser("alice");
		ofc.setEncryptionUser("bob");
		ofc.setSignaturePropFile("interop.properties");
		ofc.setPasswordCallbackClass("org.apache.axis2.security.PWCallback");
		ofc.setSignatureKeyIdentifier(WSSHandlerConstants.BST_DIRECT_REFERENCE);
		ofc.setEncryptionKeyIdentifier(WSSHandlerConstants.SKI_KEY_IDENTIFIER);
		ofc.setOptimizeParts("//xenc:EncryptedData/xenc:CipherData/xenc:CipherValue");
		
		return ofc;
	}

	protected InflowConfiguration getInflowConfiguration() {
		InflowConfiguration ifc = new InflowConfiguration();
		
		ifc.setActionItems("Signature Encrypt Timestamp");
		ifc.setPasswordCallbackClass("org.apache.axis2.security.PWCallback");
		ifc.setSignaturePropFile("interop.properties");
		
		return ifc;
	}

	protected String getClientRepo() {
		return MTOM_SEC_CLIENT_REPOSITORY;
	}

	protected String getServiceRepo() {
		return MTOM_SEC_SERVICE_REPOSITORY;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.security.InteropTestBase#useSOAP12()
	 */
	protected boolean isUseSOAP12InStaticConfigTest() {
		return true;
	}
	
}
