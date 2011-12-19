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

package org.apache.axis2.engine.map;

import javax.xml.stream.XMLStreamException;
import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisServer;

/**
 * The Class MapServiceTest.
 */
public class MapServiceTest extends TestCase {
    private AxisServer server;
    
    /** The service. */
    protected AxisService service;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        server = new AxisServer();
        server.deployService(MapService.class.getName());
    }   

    @Override
    protected void tearDown() throws Exception {
        server.stop();
    }

    /**
     * Test string generics map service.
     * 
     * @throws XMLStreamException
     *             the xML stream exception
     * @throws AxisFault
     *             the axis fault
     */
    public void testStringGenericsMapService() throws XMLStreamException,
            AxisFault {
        String epr = "http://localhost:6060/axis2/services/MapService/stringGenericsMapService";
        Options options = new Options();
        options.setTo(new EndpointReference(epr));
        ServiceClient sender = new ServiceClient();
        sender.setOptions(options);
        OMElement msg = createMessageBody("stringGenericsMapService");
        OMElement ret = sender.sendReceive(msg);
        assertNotNull("Response can not be null", ret);
        assertEquals("Not the expeacted value",
                "stringGenericsMapServiceResponse", ret.getLocalName());
        assertNotNull("Element can not be null", ret.getFirstElement()
                .getFirstElement());
        assertEquals("Not the expeacted value", msg.getFirstElement()
                .getFirstElement().toString(), ret.getFirstElement()
                .getFirstElement().toString());

    }

    /**
     * Test string generics tree map service.
     * 
     * @throws XMLStreamException
     *             the xML stream exception
     * @throws AxisFault
     *             the axis fault
     */
    public void testStringGenericsTreeMapService() throws XMLStreamException,
            AxisFault {

        String epr = "http://localhost:6060/axis2/services/MapService/stringGenericsTreeMapService";
        Options options = new Options();
        options.setTo(new EndpointReference(epr));
        ServiceClient sender = new ServiceClient();
        sender.setOptions(options);
        OMElement msg = createMessageBody("stringGenericsTreeMapService");
        OMElement ret = sender.sendReceive(msg);
        assertNotNull("Response can not be null", ret);
        assertEquals("Not the expeacted value",
                "stringGenericsTreeMapServiceResponse", ret.getLocalName());
        assertNotNull("Element can not be null", ret.getFirstElement()
                .getFirstElement());
        assertEquals("Not the expeacted value", msg.getFirstElement()
                .getFirstElement().toString(), ret.getFirstElement()
                .getFirstElement().toString());

    }

    /**
     * Creates the message body.
     * 
     * @param opName
     *            the op name
     * @return the oM element
     */
    public static OMElement createMessageBody(String opName) {
        try {
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMElement map = AXIOMUtil
                    .stringToOM("<map><entry><key>key1</key><value>value1</value></entry></map>");
            OMNamespace omNs = fac.createOMNamespace(
                    "http://map.engine.axis2.apache.org", "map");
            OMElement msg = fac.createOMElement(opName, omNs);
            msg.addChild(map);
            return msg;

        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return null;
    }

}
