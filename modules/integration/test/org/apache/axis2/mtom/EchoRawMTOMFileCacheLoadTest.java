/*
 * Copyright  2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.axis2.mtom;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:thilina@opensource.lk"> Thilina Gunarathne </a>
 */
public class EchoRawMTOMFileCacheLoadTest extends EchoRawMTOMLoadTest {

    private QName serviceName = new QName("EchoXMLService");

    private QName operationName = new QName("echoOMElement");

    private ServiceContext serviceContext;

    private ServiceDescription service;

    public EchoRawMTOMFileCacheLoadTest() {
        super(EchoRawMTOMFileCacheLoadTest.class.getName());
    }

    public EchoRawMTOMFileCacheLoadTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start(Constants.TESTING_PATH + "MTOM-fileCache-enabledRepository");
        service = Utils.createSimpleService(serviceName, Echo.class.getName(),
                operationName);
        UtilServer.deployService(service);
        serviceContext = service.getParent().getServiceGroupContext().getServiceContext(service.getName().getLocalPart());
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }

   
    public void testEchoXMLSync() throws Exception {
        super.testEchoXMLSync();
    }

}
