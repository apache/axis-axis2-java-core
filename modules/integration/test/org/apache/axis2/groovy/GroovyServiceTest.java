package org.apache.axis2.groovy;

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.EchoRawXMLTest;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.soap.SOAPFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

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
*
*
*/

/**
 * Author : Deepal Jayasinghe
 * Date: Jul 15, 2005
 * Time: 2:20:48 PM
 */
public class GroovyServiceTest extends TestCase {


    private EndpointReference targetEPR =
            new EndpointReference("http://127.0.0.1:"
            + (UtilServer.TESTING_PORT)
            + "/axis/services/GroovyService/echo");
    private QName serviceName = new QName("GroovyService");
    private QName operationName = new QName("echo");


    public GroovyServiceTest() {
        super(EchoRawXMLTest.class.getName());
    }

    public GroovyServiceTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        String repository = "target/groovyRepo";
        UtilServer.start(repository);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }


    public void testServiceExsit() throws Exception {
        ServiceDescription desc = UtilServer.getConfigurationContext().
                getAxisConfiguration().getService(serviceName);
        assertNotNull(desc);
    }


    public void testEchoXMLSync() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        //OMElement payload = createPayLoad();
        OMElement payload = getpayLoad();

        org.apache.axis2.clientapi.Call call = new org.apache.axis2.clientapi.Call();

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);

        OMElement result = (OMElement) call.invokeBlocking(operationName.getLocalPart(),
                payload);
        assertNotNull(result);
        OMElement person = (OMElement)result.getFirstChild();
        assertEquals(person.getLocalName(),"person");

        StringWriter writer = new StringWriter();
        result.build();
        result.serializeWithCache(new OMOutputImpl(XMLOutputFactory.newInstance().createXMLStreamWriter(writer)));
        writer.flush();
        call.close();
    }



    private OMElement getpayLoad() throws XMLStreamException {
        String str= "<ADDRESS><DET><NAME>Ponnampalam Thayaparan</NAME> <OCC>Student</OCC>" +
                "<ADD>3-2/1,Hudson Road,Colombo-03</ADD><GENDER>Male</GENDER>" +
                "</DET><DET><NAME>Eranka Samaraweera</NAME><OCC>Student</OCC><ADD>Martara</ADD>" +
                "<GENDER>Male</GENDER></DET><DET><NAME>Sriskantharaja Ahilan</NAME>" +
                "<OCC>Student</OCC><ADD>Trincomalee</ADD><GENDER>Male</GENDER>" +
                "</DET></ADDRESS>";
        XMLStreamReader xmlReader=  XMLInputFactory.newInstance().createXMLStreamReader(new
                ByteArrayInputStream(str.getBytes()));
        OMFactory fac = OMAbstractFactory.getOMFactory();

        StAXOMBuilder staxOMBuilder = new
                StAXOMBuilder(fac,(XMLStreamReader) xmlReader);
        return   staxOMBuilder.getDocumentElement();
    }


}
