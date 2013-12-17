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

package org.apache.axis2.description;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.description.java2wsdl.XMLSchemaTest;
import org.apache.axis2.util.Utils;
import org.apache.axis2.util.XMLPrettyPrinter;

public class AxisService2WSDLTest extends XMLSchemaTest {

    private AxisService service;
    private ByteArrayOutputStream stream;

    @Override
    protected void setUp() throws Exception {
        service = Utils.createSimpleService(new QName("test"), "", new QName("test"));
        stream = new ByteArrayOutputStream();
    }

    @Override
    protected void tearDown() throws Exception {
        service = null;
        stream = null;
        super.tearDown();
    }

    // This test implicitly test the checkStyle(axisOperation); method
    // This test belongs to AxisService2WSDL20 class
    public void testGenerateInterfaceOperationElement20() throws Exception {
        // if this not null test becomes useless
        assertNull(service.getParameterValue(WSDL2Constants.OPERATION_STYLE));
        AxisService2WSDL20 service2wsdl20 = new AxisService2WSDL20(service);
        OMElement element = service2wsdl20.generateOM();
        // writeToFile("test-resources" + File.separator + "wsdl" +
        // File.separator
        // + "testGenerateInterfaceOperationElement_WSDL.wsdl",
        // element.toString());
        XMLPrettyPrinter.prettify(element, stream);
        assertSimilarXML(readWSDLFromFile("test-resources" + File.separator
                + "wsdl" + File.separator + "testGenerateInterfaceOperationElement_WSDL.wsdl"), stream.toString());

    }

    // This test belongs to AxisService2WSDL11 class
    public void testGenerateInterfaceOperationElement11() throws Exception {
        // if this not null test becomes useless
        assertNull(service.getParameterValue(WSDL2Constants.OPERATION_STYLE));
        AxisService2WSDL11 service2wsdl11 = new AxisService2WSDL11(service);
        OMElement element = service2wsdl11.generateOM();
        // writeToFile("test-resources" + File.separator + "wsdl" +
        // File.separator
        // +
        // "testGenerateInterfaceOperationElement11_WSDL.wsdl",element.toString());
        XMLPrettyPrinter.prettify(element, stream);
        assertSimilarXML(readWSDLFromFile("test-resources" + File.separator
                + "wsdl" + File.separator + "testGenerateInterfaceOperationElement11_WSDL.wsdl"), stream.toString());
    }
    
    

}
