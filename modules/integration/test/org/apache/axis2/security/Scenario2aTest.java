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

import org.apache.axis2.Constants;
import org.apache.axis2.rpc.MultirefTest;
import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.rampart.handler.config.InflowConfiguration;
import org.apache.rampart.handler.config.OutflowConfiguration;
import org.apache.ws.security.WSConstants;

import java.util.Hashtable;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * WS-Security interop scenario 2a
 */
public class Scenario2aTest extends InteropTestBase {

    public static Test suite() {
        return getTestSetup2(new TestSuite(Scenario2aTest.class),Constants.TESTING_PATH + SCENARIO2a_SERVICE_REPOSITORY);
    }
    
	protected OutflowConfiguration getOutflowConfiguration() {
		OutflowConfiguration ofc = new OutflowConfiguration();
		
		ofc.setActionItems("UsernameTokenSignature Encrypt Timestamp");
		ofc.setUser("Chris");
		ofc.setEncryptionParts("{Element}{" + WSSE_NS + "}UsernameToken");
		ofc.setEncryptionUser("bob");
		ofc.setEncryptionPropFile("interop.properties");
		ofc.setPasswordCallbackClass("org.apache.axis2.security.PWCallback");
		ofc.setEncryptionSymAlgorithm(WSConstants.TRIPLE_DES);
		ofc.setEncryptionKeyIdentifier(WSSHandlerConstants.SKI_KEY_IDENTIFIER);
		
		return ofc;
	}

	protected InflowConfiguration getInflowConfiguration() {
		return null;
	}

	protected String getClientRepo() {
		return SCENARIO2a_CLIENT_REPOSITORY;
	}

	protected String getServiceRepo() {
		return SCENARIO2a_SERVICE_REPOSITORY;
	}

	protected boolean isUseSOAP12InStaticConfigTest() {
		return true;
	}

    protected OutflowConfiguration getOutflowConfigurationWithRefs() {
        OutflowConfiguration ofc = new OutflowConfiguration();
        
        ofc.setActionItems("UsernameTokenSignature Encrypt Timestamp");
        ofc.setUser("Chris");
        ofc.setEncryptionParts("{Element}{" + WSSE_NS + "}UsernameToken");
        ofc.setEncryptionUser("bob");
        ofc.setPasswordCallbackClass("org.apache.axis2.security.PWCallback");
        ofc.setEncryptionSymAlgorithm(WSConstants.TRIPLE_DES);
        ofc.setEncryptionKeyIdentifier(WSSHandlerConstants.SKI_KEY_IDENTIFIER);
        
        ofc.setEncryptionPropRefId("key1");
        
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
        
        Hashtable table = new Hashtable();
        table.put("key1", prop1);
        
        return table;
        
    }
}
