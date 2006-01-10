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
 * WS-Security interop scenario 5
 */
public class Scenario5Test extends InteropTestBase {

	protected OutflowConfiguration getOutflowConfiguration() {
		OutflowConfiguration ofc = new OutflowConfiguration(2);
		
		ofc.setActionItems("Signature NoSerialization");
		ofc.setUser("alice");
		ofc.setSignaturePropFile("interop.properties");
		ofc.setPasswordCallbackClass("org.apache.axis2.security.PWCallback");
		ofc.setSignatureKeyIdentifier(WSSHandlerConstants.BST_DIRECT_REFERENCE);
		ofc.setSignatureParts("{}{http://xmlsoap.org/Ping}ticket");
		
		ofc.nextAction();
		
		ofc.setActionItems("Signature Timestamp");
		ofc.setUser("alice");
		ofc.setSignaturePropFile("interop.properties");
		ofc.setPasswordCallbackClass("org.apache.axis2.security.PWCallback");
		
		return ofc;
	}

	protected InflowConfiguration getInflowConfiguration() {
		return null;
	}

	protected String getClientRepo() {
		return SCENARIO5_CLIENT_REPOSITORY;
	}

	protected String getServiceRepo() {
		return SCENARIO5_SERVICE_REPOSITORY;
	}

	protected boolean isUseSOAP12InStaticConfigTest() {
		return true;
	}
}
