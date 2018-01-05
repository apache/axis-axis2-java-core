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

package org.apache.axis2.jaxws.context.tests;

import org.apache.axis2.jaxws.context.sei.MessageContext;
import org.apache.axis2.jaxws.context.sei.MessageContextService;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceContext;

public class MessageContextTests {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo");

    static final WebServiceClient wsc = MessageContextService.class.getAnnotation(WebServiceClient.class);

    @Test
    public void testWSCtxt_WSDL_SERVICE_read() throws Exception {
        String type_expected = QName.class.getName();
        String value_expected = "{" + wsc.targetNamespace() + "}" + wsc.name();
        runTest(javax.xml.ws.handler.MessageContext.WSDL_SERVICE, type_expected, value_expected, false);
    }

    @Test
    public void testWSCtxt_WSDL_PORT_read() throws Exception {
        String type_expected = QName.class.getName();
        String value_expected = "{" + wsc.targetNamespace() + "}MessageContextPort";
        runTest(javax.xml.ws.handler.MessageContext.WSDL_PORT, type_expected, value_expected, false);
    }

    @Test
    public void testWSCtxt_WSDL_OPERATION_read() throws Exception {
        String type_expected = QName.class.getName();
        String value_expected = "isPropertyPresent";
        runTest(javax.xml.ws.handler.MessageContext.WSDL_OPERATION, type_expected, value_expected, false);
    }

    @Test
    public void testWSCtxt_WSDL_INTERFACE_read() throws Exception {
        String type_expected = QName.class.getName();
        String value_expected = "{" + wsc.targetNamespace() + "}MessageContext";
        runTest(javax.xml.ws.handler.MessageContext.WSDL_INTERFACE, type_expected, value_expected, false);
    }

    @Test
    public void testWSCtxt_WSDL_DESCRIPTION_read() throws Exception {
        String type_expected = java.net.URI.class.getName();
        String value_expected = "META-INF/MessageContext.wsdl";
        runTest(javax.xml.ws.handler.MessageContext.WSDL_DESCRIPTION, type_expected, value_expected, false);
    }

    private void runTest(String propName, String exType, String exValue, boolean isValueFullySpecified) throws Exception {
        MessageContext port = getPort();

        Holder<String> type = new Holder<String>();
        Holder<String> value = new Holder<String>();
        Holder<Boolean> isFound = new Holder<Boolean>();
        Holder<String> propertyName = new Holder<String>(propName);

        port.isPropertyPresent(propertyName, value, type, isFound);

        System.out.println("Property = " + propName + " found=" + isFound.value);
        System.out.println("Value = " + value.value + "/" + exValue);
        System.out.println("Type = " + type.value + "/" + exType);

        assertTrue("WebServiceContext did not expose " + propertyName.value, isFound.value);
        
        // Make sure that the WebServiceContext's contents don't persist after the
        // invocation.  This is necessary to ensure proper gc and avoid accidental misuse.
        WebServiceContext wsc = org.apache.axis2.jaxws.context.MessageContextImpl.webServiceContext;
        
        assertTrue("WebServiceContext resources were not freed",
                   wsc.getMessageContext() == null);

        if (exType != null)
            assertTrue("Type of " + propertyName.value + " does not match [" + type.value + ", " + exType + "]",
                    type.value != null && type.value.indexOf(exType) > -1);

        if (exValue != null) {
            if (isValueFullySpecified) {
                assertEquals("Value of " + propertyName.value + " does not match", exValue, value.value);
            } else {
                assertTrue("Value of " + propertyName.value + " does not contain " + exValue, value.value.indexOf(exValue) != -1);
            }
        }
    }

    public MessageContext getPort() throws Exception {
        MessageContextService service = new MessageContextService();
        MessageContext port = service.getMessageContextPort();
        BindingProvider p = (BindingProvider) port;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                server.getEndpoint("MessageContextService.MessageContextPort"));
        return port;
    }
}
