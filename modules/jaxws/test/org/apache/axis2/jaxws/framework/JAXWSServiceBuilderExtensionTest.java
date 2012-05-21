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

package org.apache.axis2.jaxws.framework;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

import junit.framework.TestCase;

public class JAXWSServiceBuilderExtensionTest extends TestCase {

    JAXWSServiceBuilderExtension builderExtension;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        builderExtension = new JAXWSServiceBuilderExtension();
    }

    @Override
    protected void tearDown() throws Exception {
        builderExtension = null;
        super.tearDown();
    }

    public void testCheckPreconditionsNull() throws Exception {
        boolean result = builderExtension.checkPreconditions(null);
        assertEquals(false, result);
    }

    public void testCheckPreconditionsStringValue() throws Exception {
        boolean result = builderExtension.checkPreconditions("StringValue");
        assertEquals(false, result);
    }

    public void testCheckPreconditionsOMElement() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = getOMElement(factory, "my-service");
        boolean result = builderExtension.checkPreconditions(element);
        assertEquals(false, result);
    }

    public void testCheckPreconditionsServiceWithNoMessageReceivers() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = getOMElement(factory, "service");
        element.addAttribute("name", "EchoService", null);
        boolean result = builderExtension.checkPreconditions(element);
        assertEquals(false, result);
    }

    public void testCheckPreconditionsServiceGroupWithNoMessageReceivers() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = getOMElement(factory, "serviceGroup");
        boolean result = builderExtension.checkPreconditions(element);
        assertEquals(false, result);
    }

    public void testCheckPreconditionsServiceWithEmptyMessageReceivers1() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = getOMElement(factory, "service");
        element.addAttribute("name", "EchoService", null);
        element.addChild(getOMElement(factory, "messageReceivers"));
        boolean result = builderExtension.checkPreconditions(element);
        assertEquals(false, result);
    }

    public void testCheckPreconditionsServiceWithEmptyMessageReceivers2() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = getOMElement(factory, "service");
        element.addAttribute("name", "EchoService", null);
        OMElement mrs = getOMElement(factory, "messageReceivers");
        mrs.addChild(getOMElement(factory, "messageReceiver"));
        mrs.addChild(getOMElement(factory, "messageReceiver"));
        element.addChild(mrs);
        boolean result = builderExtension.checkPreconditions(element);
        assertEquals(false, result);
    }

    public void testCheckPreconditionsServiceWithWrongMessageReceivers1() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = getOMElement(factory, "service");
        element.addAttribute("name", "EchoService", null);
        OMElement mrs = getOMElement(factory, "messageReceivers");
        OMElement mr1 = getOMElement(factory, "messageReceiver");
        OMElement mr2 = getOMElement(factory, "messageReceiver");
        mr1.addAttribute("class", "DummyCalss", null);
        mr2.addAttribute("class", "DummyCalss", null);
        mrs.addChild(mr1);
        mrs.addChild(mr2);
        element.addChild(mrs);
        boolean result = builderExtension.checkPreconditions(element);
        assertEquals(false, result);
    }

    public void testCheckPreconditionsServiceWithWrongMessageReceivers2() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = getOMElement(factory, "service");
        element.addAttribute("name", "EchoService", null);
        OMElement mrs = getOMElement(factory, "messageReceivers");
        OMElement mr1 = getOMElement(factory, "messageReceiver");
        OMElement mr2 = getOMElement(factory, "messageReceiver");
        mr1.addAttribute("class", "org.apache.axis2.jaxws.server.JAXWSMessageReceiver", null);
        mr2.addAttribute("class", "DummyCalss", null);
        mrs.addChild(mr1);
        mrs.addChild(mr2);
        element.addChild(mrs);
        boolean result = builderExtension.checkPreconditions(element);
        assertEquals(false, result);
    }

    public void testCheckPreconditionsServiceWithMessageReceivers() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = getOMElement(factory, "service");
        element.addAttribute("name", "EchoService", null);
        OMElement mrs = getOMElement(factory, "messageReceivers");
        OMElement mr1 = getOMElement(factory, "messageReceiver");
        OMElement mr2 = getOMElement(factory, "messageReceiver");
        mr1.addAttribute("class", "org.apache.axis2.jaxws.server.JAXWSMessageReceiver", null);
        mr2.addAttribute("class", "org.apache.axis2.jaxws.server.JAXWSMessageReceiver", null);
        mrs.addChild(mr1);
        mrs.addChild(mr2);
        element.addChild(mrs);
        boolean result = builderExtension.checkPreconditions(element);
        assertEquals(true, result);
    }

    public void testCheckPreconditionsServiceGroupWithWrongMessageReceivers() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = getOMElement(factory, "serviceGroup");
        element.addAttribute("name", "EchoService", null);
        OMElement mrs = getOMElement(factory, "messageReceivers");
        OMElement mr1 = getOMElement(factory, "messageReceiver");
        OMElement mr2 = getOMElement(factory, "messageReceiver");
        mr1.addAttribute("class", "org.apache.axis2.jaxws.server.JAXWSMessageReceiver", null);
        mr2.addAttribute("class", "DummyCalss", null);
        mrs.addChild(mr1);
        mrs.addChild(mr2);
        element.addChild(mrs);
        boolean result = builderExtension.checkPreconditions(element);
        assertEquals(false, result);
    }

    public void testCheckPreconditionsServiceGroupWithMessageReceivers() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = getOMElement(factory, "serviceGroup");
        element.addAttribute("name", "EchoService", null);
        OMElement mrs = getOMElement(factory, "messageReceivers");
        OMElement mr1 = getOMElement(factory, "messageReceiver");
        OMElement mr2 = getOMElement(factory, "messageReceiver");
        mr1.addAttribute("class", "org.apache.axis2.jaxws.server.JAXWSMessageReceiver", null);
        mr2.addAttribute("class", "org.apache.axis2.jaxws.server.JAXWSMessageReceiver", null);
        mrs.addChild(mr1);
        mrs.addChild(mr2);
        element.addChild(mrs);
        boolean result = builderExtension.checkPreconditions(element);
        assertEquals(false, result);
    }

    public void testCheckPreconditionsServiceGroupWithMessageReceivers2() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement group = getOMElement(factory, "serviceGroup");
        OMElement element1 = getOMElement(factory, "service");
        element1.addAttribute("name", "EchoService", null);
        OMElement mrs = getOMElement(factory, "messageReceivers");
        OMElement mr1 = getOMElement(factory, "messageReceiver");
        OMElement mr2 = getOMElement(factory, "messageReceiver");
        mr1.addAttribute("class", "org.apache.axis2.jaxws.server.JAXWSMessageReceiver", null);
        mr2.addAttribute("class", "org.apache.axis2.jaxws.server.JAXWSMessageReceiver", null);
        mrs.addChild(mr1);
        mrs.addChild(mr2);
        element1.addChild(mrs);
        group.addChild(element1);

        OMElement element2 = getOMElement(factory, "service");
        element2.addAttribute("name", "EchoService2", null);
        OMElement mrs2 = getOMElement(factory, "messageReceivers");
        OMElement mr12 = getOMElement(factory, "messageReceiver");
        OMElement mr22 = getOMElement(factory, "messageReceiver");
        mr12.addAttribute("class", "org.apache.axis2.jaxws.server.JAXWSMessageReceiver", null);
        mr22.addAttribute("class", "org.apache.axis2.jaxws.server.JAXWSMessageReceiver", null);
        mrs2.addChild(mr12);
        mrs2.addChild(mr22);
        element2.addChild(mrs2);

        group.addChild(element1);
        group.addChild(element2);

        boolean result = builderExtension.checkPreconditions(group);
        assertEquals(true, result);
    }

    public void testCheckPreconditionsServiceGroupWithMessageReceivers3() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement group = getOMElement(factory, "serviceGroup");
        OMElement element1 = getOMElement(factory, "service");
        element1.addAttribute("name", "EchoService", null);
        OMElement mrs = getOMElement(factory, "messageReceivers");
        OMElement mr1 = getOMElement(factory, "messageReceiver");
        OMElement mr2 = getOMElement(factory, "messageReceiver");
        mr1.addAttribute("class", "org.apache.axis2.jaxws.server.JAXWSMessageReceiver", null);
        mr2.addAttribute("class", "org.apache.axis2.jaxws.server.JAXWSMessageReceiver", null);
        mrs.addChild(mr1);
        mrs.addChild(mr2);
        element1.addChild(mrs);
        group.addChild(element1);

        OMElement element2 = getOMElement(factory, "service");
        element2.addAttribute("name", "EchoService2", null);
        OMElement mrs2 = getOMElement(factory, "messageReceivers");
        OMElement mr12 = getOMElement(factory, "messageReceiver");
        OMElement mr22 = getOMElement(factory, "messageReceiver");
        mr12.addAttribute("class", "org.apache.axis2.jaxws.server.JAXWSMessageReceiver", null);
        mr22.addAttribute("class", "Dummy", null);
        mrs2.addChild(mr12);
        mrs2.addChild(mr22);
        element2.addChild(mrs2);

        group.addChild(element1);
        group.addChild(element2);

        boolean result = builderExtension.checkPreconditions(group);
        assertEquals(false, result);
    }

    private OMElement getOMElement(OMFactory factory, String elementName) {
        if (factory == null) {
            factory = OMAbstractFactory.getOMFactory();
        }
        return factory.createOMElement(new QName(elementName));
    }

}
