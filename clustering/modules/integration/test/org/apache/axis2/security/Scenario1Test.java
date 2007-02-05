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
import org.apache.rampart.handler.config.InflowConfiguration;
import org.apache.rampart.handler.config.OutflowConfiguration;

import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * WS-Security interop scenario 1
 */
public class Scenario1Test extends InteropTestBase {

    public static Test suite() {
        return getTestSetup2(new TestSuite(Scenario1Test.class),Constants.TESTING_PATH + SCENARIO1_SERVICE_REPOSITORY);
    }
    
	protected OutflowConfiguration getOutflowConfiguration() {
		OutflowConfiguration ofc = new OutflowConfiguration();
		ofc.setActionItems("UsernameToken");
		ofc.setUser("Chris");
		ofc.setPasswordCallbackClass("org.apache.axis2.security.PWCallback");
		ofc.setPasswordType("PasswordText");
		return ofc;
	}

	protected InflowConfiguration getInflowConfiguration() {
		return null;
	}

	protected String getClientRepo() {
		return SCENARIO1_CLIENT_REPOSITORY;
	}

	protected String getServiceRepo() {
		return SCENARIO1_SERVICE_REPOSITORY;
	}

	protected boolean isUseSOAP12InStaticConfigTest() {
		return true;
	}

    protected OutflowConfiguration getOutflowConfigurationWithRefs() {
        return null;
    }

    protected InflowConfiguration getInflowConfigurationWithRefs() {
        return null;
    }

    protected Hashtable getPropertyRefs() {
        return null;
    }

}
