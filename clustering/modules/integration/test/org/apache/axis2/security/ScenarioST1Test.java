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
 * SAML Scenario 1
 */
public class ScenarioST1Test extends InteropTestBase {

    public static Test suite() {
        return getTestSetup2(new TestSuite(ScenarioST1Test.class),Constants.TESTING_PATH + SCENARIO_ST1_SERVICE_REPOSITORY);
    }
    
	protected OutflowConfiguration getOutflowConfiguration() {
		// TODO 
		throw new UnsupportedOperationException("TODO");
	}

	protected InflowConfiguration getInflowConfiguration() {
		// TODO 
		throw new UnsupportedOperationException("TODO");
	}

	protected String getClientRepo() {
		return SCENARIO_ST1_CLIENT_REPOSITORY;
	}

	protected String getServiceRepo() {
		return SCENARIO_ST1_SERVICE_REPOSITORY;
	}

	protected boolean isUseSOAP12InStaticConfigTest() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

    /* (non-Javadoc)
     * @see org.apache.axis2.security.InteropTestBase#getOutflowConfigurationWithRefs()
     */
    protected OutflowConfiguration getOutflowConfigurationWithRefs() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.security.InteropTestBase#getInflowConfigurationWithRefs()
     */
    protected InflowConfiguration getInflowConfigurationWithRefs() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.security.InteropTestBase#getPropertyRefs()
     */
    protected Hashtable getPropertyRefs() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

}
