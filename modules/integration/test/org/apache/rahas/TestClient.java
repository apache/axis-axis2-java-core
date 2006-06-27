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

package org.apache.rahas;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.integration.UtilServer;
import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.rampart.handler.config.InflowConfiguration;
import org.apache.rampart.handler.config.OutflowConfiguration;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public abstract class TestClient extends TestCase {
    
    protected int port = UtilServer.TESTING_PORT;
    
    public TestClient(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        UtilServer.start(Constants.TESTING_PATH + getServiceRepo() ,null);
    }
    
    protected void tearDown() throws Exception {
        UtilServer.stop();
    }

    /**
     * @param args
     */
    public void testConversation() {
        try {

            // Get the repository location from the args
            String repo = Constants.TESTING_PATH + "rahas_client_repo";

            OMElement payload = getRequest();
            ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repo,
                    null);
            ServiceClient serviceClient = new ServiceClient(configContext, null);
            Options options = new Options();
            options.setTo(new EndpointReference("http://127.0.0.1:" + port + "/axis2/services/SecureService"));
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setAction(org.apache.rahas.Constants.RST_ACTON_SCT);
            


            OutflowConfiguration clientOutflowConfiguration = getClientOutflowConfiguration();
            if(clientOutflowConfiguration != null) {
                options.setProperty(WSSHandlerConstants.OUTFLOW_SECURITY, clientOutflowConfiguration.getProperty());
            }
            InflowConfiguration clientInflowConfiguration = getClientInflowConfiguration();
            if(clientInflowConfiguration != null) {
                options.setProperty(WSSHandlerConstants.INFLOW_SECURITY, clientInflowConfiguration.getProperty());
            }

            serviceClient.engageModule(new QName("rampart"));

            serviceClient.setOptions(options);

            //Blocking invocation
            serviceClient.sendReceive(payload);

            serviceClient.sendReceive(getRequest());

            OMElement result = serviceClient.sendReceive(getRequest());

            this.validateRsponse(result);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            fail(axisFault.getMessage());
        }
    }

    public abstract OMElement getRequest();

    public abstract OutflowConfiguration getClientOutflowConfiguration();

    public abstract InflowConfiguration getClientInflowConfiguration();
    
    public abstract String getServiceRepo();
    
    public abstract void validateRsponse(OMElement resp);
}
