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
 
package org.apache.axis.deployment;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.description.TransportInDescription;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.engine.AxisConfiguration;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.phaseresolver.PhaseException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class TransportDeploymentTest extends AbstractTestCase {
    /**
     * Constructor.
     */
    public TransportDeploymentTest(String testName) {
        super(testName);
    }

    public void testTransports() throws AxisFault, PhaseException, DeploymentException, XMLStreamException {
        DeploymentEngine engine = new DeploymentEngine(testResourceDir + "/deployment", "server-transport.xml");
        engine.load();
        AxisConfiguration er = engine.getAxisConfig();
        TransportInDescription transport = er.getTransportIn(new QName("http"));
        assertNotNull(transport);
        assertNotNull(transport.getInFlow());
        TransportOutDescription transport1 = er.getTransportOut(new QName("http"));
        assertNotNull(transport1);
        assertNotNull(transport1.getOutFlow());
    }
}
