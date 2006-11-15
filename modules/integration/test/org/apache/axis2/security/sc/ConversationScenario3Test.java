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

package org.apache.axis2.security.sc;

import org.apache.axis2.description.Parameter;
import org.apache.rampart.conversation.ConversationConfiguration;
import org.apache.rampart.handler.config.InflowConfiguration;
import org.apache.rampart.handler.config.OutflowConfiguration;

/**
 * This tests security context establishment when there's no STS involved.
 * Note that we are not setting the STS endpoint address in the rahas config.
 * 
 * The client side rahas outflow handler will create the RSTR with the SCT
 * and RPT with a secret in a EncryptedKey and will send it over to the service.
 */
public class ConversationScenario3Test extends TestClient {

    public ConversationScenario3Test(String name) {
        super(name);
    }

    public Parameter getClientConversationConfiguration() {
        ConversationConfiguration config = new ConversationConfiguration();

        config.setCryptoPropertiesFile("sec.properties");
        config.setScope(ConversationConfiguration.SCOPE_SERVICE);
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
        return "sc_service_repo_3";
    }

}
