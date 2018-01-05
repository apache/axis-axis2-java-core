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

package org.apache.axis2.jaxws.provider;

import static org.junit.Assert.fail;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

public class SoapMessageNullProviderTests extends ProviderTestCase {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    private QName serviceName = new QName("http://ws.apache.org/axis2", "SoapMessageNullProviderService");
    public static final QName portName =
        new QName("http://ws.apache.org/axis2", "SoapMessageNullProviderPort");


    public static final String bindingID = SOAPBinding.SOAP11HTTP_BINDING;
    public static final Service.Mode mode = Service.Mode.MESSAGE;

    /*
     * Test that the custom property jaxws.provider.interpretNullAsOneway when set to true
     * correctly causes the jaxws runtime to treat a null return from a provider as a one-way
     */
    @Test
    public void testProviderReturnsNull() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Service svc = Service.create(serviceName);
        svc.addPort(portName, bindingID, server.getEndpoint("SoapMessageNullProviderService.SoapMessageNullProviderPort"));

        javax.xml.ws.Dispatch<SOAPMessage> dispatch = null;
        dispatch = svc.createDispatch(portName, SOAPMessage.class, mode);

        SOAPMessage message = AttachmentUtil.toSOAPMessage(AttachmentUtil.msgEnvPlain);
        try {
            dispatch.invokeOneWay(message);
        }catch(Exception e){
            e.printStackTrace();
            fail("Caught exception " + e);
        }
        
        // Try again to verify
        try {
            dispatch.invokeOneWay(message);
        }catch(Exception e){
            e.printStackTrace();
            fail("Caught exception " + e);
        }
    } 

            
            }
