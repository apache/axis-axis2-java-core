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

import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.rampart.handler.config.InflowConfiguration;
import org.apache.rampart.handler.config.OutflowConfiguration;

import java.util.Hashtable;
import java.util.Properties;

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

    protected OutflowConfiguration getOutflowConfigurationWithRefs() {
        OutflowConfiguration ofc = new OutflowConfiguration(2);
        
        ofc.setActionItems("Signature NoSerialization");
        ofc.setUser("alice");
        ofc.setSignaturePropRefId("key1");
        ofc.setPasswordCallbackClass("org.apache.axis2.security.PWCallback");
        ofc.setSignatureKeyIdentifier(WSSHandlerConstants.BST_DIRECT_REFERENCE);
        ofc.setSignatureParts("{}{http://xmlsoap.org/Ping}ticket");
        
        ofc.nextAction();
        
        ofc.setActionItems("Signature Timestamp");
        ofc.setUser("alice");
        ofc.setSignaturePropRefId("key2");
        ofc.setPasswordCallbackClass("org.apache.axis2.security.PWCallback");
        
        return ofc;
    }

    protected InflowConfiguration getInflowConfigurationWithRefs() {
        return null;
    }

    protected Hashtable getPropertyRefs() {
        Properties prop1 =  new Properties();
        prop1.setProperty("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
        prop1.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", "jks");
        prop1.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", "password");
        prop1.setProperty("org.apache.ws.security.crypto.merlin.file", "interop2.jks");

        Properties prop2 =  new Properties();
        prop2.setProperty("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
        prop2.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", "jks");
        prop2.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", "password");
        prop2.setProperty("org.apache.ws.security.crypto.merlin.file", "interop2.jks");
        
        Hashtable table = new Hashtable();
        table.put("key1", prop1);
        
        //IMPORTANT: Note that the key of the first repetition has "1" appended to it
        table.put("key21", prop2);
        
        return table;
    }
}
