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

package org.apache.axis2.groovy;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

public class GroovyServiceTest extends UtilServerBasedTestCase {


    private EndpointReference targetEPR =
            new EndpointReference("http://127.0.0.1:"
                    + (UtilServer.TESTING_PORT)
                    + "/axis2/services/GroovyService/echo");
    private QName serviceName = new QName("GroovyService");
    private QName operationName = new QName("echo");


    public GroovyServiceTest() {
        super(GroovyServiceTest.class.getName());
    }

    public GroovyServiceTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup2(new TestSuite(GroovyServiceTest.class), "target/groovyRepo");
    }

    public void testServiceExists() throws Exception {
        AxisService desc = UtilServer.getConfigurationContext().
                getAxisConfiguration().getService(serviceName.getLocalPart());
        assertNotNull(desc);
    }

    public void testEchoXMLSync() throws Exception {
        OMElement payload = getpayLoad();

//        Call call =
//                new Call("target/test-resources/integrationRepo");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setAction(operationName.getLocalPart());

//        call.setClientOptions(options);


        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        "target/test-resources/integrationRepo", null);
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);
        OMElement result = sender.sendReceive(payload);

        assertNotNull(result);
        OMElement person = (OMElement)result.getFirstOMChild();
        assertEquals(person.getLocalName(), "person");

        StringWriter writer = new StringWriter();
        result.build();
        result.serialize(writer);
        writer.flush();
    }


    private OMElement getpayLoad() throws XMLStreamException {
        String str = "<ADDRESS><DET><NAME>Ponnampalam Thayaparan</NAME> <OCC>Student</OCC>" +
                "<ADD>3-2/1,Hudson Road,Colombo-03</ADD><GENDER>Male</GENDER>" +
                "</DET><DET><NAME>Eranka Samaraweera</NAME><OCC>Student</OCC><ADD>Martara</ADD>" +
                "<GENDER>Male</GENDER></DET><DET><NAME>Sriskantharaja Ahilan</NAME>" +
                "<OCC>Student</OCC><ADD>Trincomalee</ADD><GENDER>Male</GENDER>" +
                "</DET></ADDRESS>";
        XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new
                ByteArrayInputStream(str.getBytes()));
        OMFactory fac = OMAbstractFactory.getOMFactory();

        StAXOMBuilder staxOMBuilder = new
                StAXOMBuilder(fac, xmlReader);
        return staxOMBuilder.getDocumentElement();
    }


}
