package org.apache.axis.deployment;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.description.AxisTransport;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.phaseresolver.PhaseException;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * Dec 24, 2004
 * 10:15:33 AM
 */
public class TransportDeploymentTest extends AbstractTestCase {
    /**
     * Constructor.
     */
    public TransportDeploymentTest(String testName) {
        super(testName);
    }

    public void testTransports() throws AxisFault, PhaseException, DeploymentException, XMLStreamException  {
        DeploymentEngine engine = new DeploymentEngine(testResourceDir+ "/deployment","server-transport.xml");
        engine.start();
        EngineRegistry er = engine.getEngineRegistry();
        AxisTransport transport = er.getTransport(new QName("http"));
        assertNotNull(transport);
        assertNotNull(transport.getInFlow());
        assertNotNull(transport.getOutFlow());
        AxisTransport transport1 = er.getTransport(new QName("smtp"));
        assertNotNull(transport1);
        assertNotNull(transport1.getFaultFlow());
    }
}
