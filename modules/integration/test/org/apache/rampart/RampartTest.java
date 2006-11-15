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

package org.apache.rampart;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.integration.UtilServer;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;

import javax.xml.namespace.QName;

import junit.framework.TestCase;


public class RampartTest extends TestCase {
    
    public final static int PORT = UtilServer.TESTING_PORT;
    
    public RampartTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        UtilServer.start(Constants.TESTING_PATH + "rampart_service_repo" ,null);
    }
    

    protected void tearDown() throws Exception {
        UtilServer.stop();
    }

    
    public void testWithPolicy() {
        try {
            String repo = Constants.TESTING_PATH + "rampart_client_repo";
    
            ConfigurationContext configContext = ConfigurationContextFactory.
                        createConfigurationContextFromFileSystem(repo, null);
            ServiceClient serviceClient = new ServiceClient(configContext, null);
            

            serviceClient.engageModule(new QName("addressing"));
            serviceClient.engageModule(new QName("rampart"));

            //TODO : figure this out !!
            boolean basic256Supported = false;
            
            if(basic256Supported) {
                System.out.println("WARNING: We are using key sizes from JCE " +
                        "Unlimited Strength Jurisdiction Policy !!!");
            }
            
            for (int i = 1; i <= 9; i++) { //<-The number of tests we have
                if(!basic256Supported && (i == 3 || i == 4 || i ==5)) {
                    //Skip the Basic256 tests
                    continue;
                }
                Options options = new Options();
                System.out.println("Testing WS-Sec: custom scenario " + i);
                options.setAction("urn:echo");
                options.setTo(new EndpointReference("http://127.0.0.1:" +
                                        PORT + 
                                        "/axis2/services/SecureService" + i));
                options.setProperty(RampartMessageData.KEY_RAMPART_POLICY, 
                        loadPolicy("test-resources/rampart/policy/" + i + ".xml"));
                serviceClient.setOptions(options);

                //Blocking invocation
                serviceClient.sendReceive(getEchoElement());
            }

            
            for (int i = 1; i <= 2; i++) { //<-The number of tests we have

                Options options = new Options();
                System.out.println("Testing WS-SecConv: custom scenario " + i);
                options.setAction("urn:echo");
                options.setTo(new EndpointReference("http://127.0.0.1:" + PORT + "/axis2/services/SecureServiceSC" + i));
                options.setProperty(RampartMessageData.KEY_RAMPART_POLICY, loadPolicy("test-resources/rampart/policy/sc-" + i + ".xml"));
                serviceClient.setOptions(options);

                //Blocking invocation
                serviceClient.sendReceive(getEchoElement());
                serviceClient.sendReceive(getEchoElement());
                
                //Cancel the token
                options.setProperty(RampartMessageData.CANCEL_REQUEST, Constants.VALUE_TRUE);
                serviceClient.sendReceive(getEchoElement());
                
                options.setProperty(RampartMessageData.CANCEL_REQUEST, Constants.VALUE_FALSE);
                serviceClient.sendReceive(getEchoElement());
                options.setProperty(RampartMessageData.CANCEL_REQUEST, Constants.VALUE_TRUE);
                serviceClient.sendReceive(getEchoElement());
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    private OMElement getEchoElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(
                "http://example1.org/example1", "example1");
        OMElement method = fac.createOMElement("echo", omNs);
        OMElement value = fac.createOMElement("Text", omNs);
        value.addChild(fac.createOMText(value, "Testing Rampart with WS-SecPolicy"));
        method.addChild(value);

        return method;
    }
    
    private Policy loadPolicy(String xmlPath) throws Exception {
        StAXOMBuilder builder = new StAXOMBuilder(xmlPath);
        return PolicyEngine.getPolicy(builder.getDocumentElement());
    }
    

    
}
