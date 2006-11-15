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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.integration.UtilServer;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
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
        UtilServer.start(Constants.TESTING_PATH + getServiceRepo(), null);
    }

    protected void tearDown() throws Exception {
        UtilServer.stop();
    }

    /**
     */
    public void testRequest() {
        try {

            // Get the repository location from the args
            String repo = Constants.TESTING_PATH + "rahas_client_repo";

            ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repo,
                                                                                                                      null);
            ServiceClient serviceClient = new ServiceClient(configContext, null);
            Options options = new Options();

            System.setProperty("javax.net.ssl.keyStorePassword", "password");
            System.setProperty("javax.net.ssl.keyStoreType", "JKS");
            System.setProperty("javax.net.ssl.trustStore", "/home/ruchith/Desktop/interop/certs/interop2.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "password");
            System.setProperty("javax.net.ssl.trustStoreType","JKS");

            options.setTo(new EndpointReference("http://127.0.0.1:" + port + "/axis2/services/SecureService"));
//            options.setTo(new EndpointReference("http://127.0.0.1:" + 9090 + "/axis2/services/UTSAMLHoK"));
//            options.setTo(new EndpointReference("https://www-lk.wso2.com:8443/axis2/services/UTSAMLHoK"));
//            options.setTo(new EndpointReference("https://192.18.49.133:2343/jaxws-s1-sts/sts"));
//            options.setTo(new EndpointReference("https://207.200.37.116/SxSts/Scenario_1_IssuedTokenOverTransport_UsernameOverTransport"));
//            options.setTo(new EndpointReference("http://localhost:9090/SxSts/Scenario_4_IssuedToken_MutualCertificate10"));

//            options.setTo(new EndpointReference("http://127.0.0.1:" + 9090 + "/axis2/services/MutualCertsSAMLHoK"));
//            options.setTo(new EndpointReference("http://www-lk.wso2.com:8888/axis2/services/MutualCertsSAMLHoK"));
//            options.setTo(new EndpointReference("https://131.107.72.15/trust/Addressing2004/UserName"));
//            options.setTo(new EndpointReference("https://131.107.72.15/trust/UserName"));
//            options.setTo(new EndpointReference("http://127.0.0.1:" + 9090 + "/trust/X509WSS10"));
//            options.setTo(new EndpointReference("https://131.107.72.15/trust/UserName"));
//            options.setTo(new EndpointReference("http://127.0.0.1:" + 9090 + "/jaxws-s4-sts/sts"));
//            options.setTo(new EndpointReference("http://127.0.0.1:9090/jaxws-s4/simple"));
//            options.setTo(new EndpointReference("http://127.0.0.1:" + 9090 + "/axis2/services/UTSAMLBearer"));

            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setAction(this.getRequestAction());
//            options.setProperty(AddressingConstants.WS_ADDRESSING_VERSION, this.getWSANamespace());


            OutflowConfiguration clientOutflowConfiguration = getClientOutflowConfiguration();
            if (clientOutflowConfiguration != null) {
                options.setProperty(WSSHandlerConstants.OUTFLOW_SECURITY, clientOutflowConfiguration.getProperty());
            }
            InflowConfiguration clientInflowConfiguration = getClientInflowConfiguration();
            if (clientInflowConfiguration != null) {
                options.setProperty(WSSHandlerConstants.INFLOW_SECURITY, clientInflowConfiguration.getProperty());
            }

            serviceClient.engageModule(new QName("addressing"));
            serviceClient.engageModule(new QName("rampart"));

            serviceClient.setOptions(options);

            //Blocking invocation

            OMElement result = serviceClient.sendReceive(getRequest());

            this.validateRsponse(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    protected String getWSANamespace() {
        return AddressingConstants.Submission.WSA_NAMESPACE;
    }

    public abstract OMElement getRequest();

    public abstract OutflowConfiguration getClientOutflowConfiguration();

    public abstract InflowConfiguration getClientInflowConfiguration();

    public abstract String getServiceRepo();

    public abstract String getRequestAction() throws TrustException;

    public abstract void validateRsponse(OMElement resp);

//
//    /**
//     * This test will use WS-SecPolicy
//     */
//    public void testWithStsClient() {
//
//        // Get the repository location from the args
//        String repo = Constants.TESTING_PATH + "rahas_client_repo";
//
//        try {
//            ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repo,
//                                                                                                                      null);
//
//            STSClient client = new STSClient(configContext);
//
//            client.setAction(this.getRequestAction());
//
//            client.setRstTemplate(this.getRSTTemplate());
//            client.setVersion(this.getTrstVersion());
//
//            Token tok =
//                    client.requestSecurityToken(this.getServicePolicy(),
//                                                "http://127.0.0.1:" + port + "/axis2/services/SecureService",
//                                                this.getSTSPolicy(),
//                                                "http://localhost:5555/axis2/services/SecureService");
//
//            assertNotNull("Response token missing", tok);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail(e.getMessage());
//        }
//
//    }

    public abstract int getTrstVersion();

    public abstract Policy getServicePolicy() throws Exception;

    public abstract Policy getSTSPolicy() throws Exception;

    public abstract OMElement getRSTTemplate() throws TrustException;

    protected Policy getPolicy(String filePath) throws Exception {
        StAXOMBuilder builder = new StAXOMBuilder(filePath);
        OMElement elem = builder.getDocumentElement();
        return PolicyEngine.getPolicy(elem);
    }

}
