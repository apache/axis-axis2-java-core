/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.transport.local;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;

public class LocalTransportTest {
    @Test
    public void test() throws Exception {
        ConfigurationContext configurationContext =
            ConfigurationContextFactory.createConfigurationContextFromURIs(
                    LocalTransportTest.class.getResource("axis2.xml"), null);
        
        AxisService service = new AxisService("Echo");
        AxisOperation operation = new InOutAxisOperation(new QName("echo"));
        operation.setMessageReceiver(new EchoMessageReceiver());
        service.addOperation(operation);
        service.addParameter(AxisService.SUPPORT_SINGLE_OP, true);
        configurationContext.getAxisConfiguration().addService(service);
        
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement requestElement = factory.createOMElement("test", "urn:test", "t");
        requestElement.setText("Hi there!");
        
        Options options = new Options();
        options.setTo(new EndpointReference("local://localhost/axis2/services/Echo"));
        ServiceClient serviceClient = new ServiceClient(configurationContext, null);
        serviceClient.setOptions(options);
        OMElement responseElement = serviceClient.sendReceive(requestElement);
        
        XMLAssert.assertXMLEqual(requestElement.toString(), responseElement.toString());
    }
}
