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

package org.apache.axis2.security.rahas;

import org.apache.axis2.description.Parameter;
import org.apache.axis2.security.handler.config.InflowConfiguration;
import org.apache.axis2.security.handler.config.OutflowConfiguration;

public class RahasScenario3Test extends TestClient {

    public RahasScenario3Test(String name) {
        super(name);
    }

    public Parameter getClientRahasConfiguration() {
        RahasConfiguration config = new RahasConfiguration();

        config.setCryptoPropertiesFile("sec.properties");
        config.setScope(RahasConfiguration.SCOPE_SERVICE);
        config.setPasswordCallbackClass(PWCallback.class.getName());
        config.setEncryptionUser("sts");

        return config.getParameter();
    }

    public OutflowConfiguration getClientOutflowConfiguration() {
        return null;
    }

    public InflowConfiguration getClientInflowConfiguration() {
        return null;
    }

    public String getServiceRepo() {
        return "rahas_service_repo_1";
    }

}
