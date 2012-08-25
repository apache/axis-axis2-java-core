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

import javax.xml.namespace.QName;

import java.io.ByteArrayOutputStream;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.Constants;
import org.apache.axis2.dataretrieval.WSDL11SupplierTemplate;

import junit.framework.TestCase;

/**
 * 
 */
public class WSDLSupplierTest extends TestCase {

    private AxisService axisService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        axisService = new AxisService();
        axisService.setName("TestWSDLService");
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement documentation = factory.createOMElement(new QName("documentation"));
        axisService.setDocumentation(documentation);

        
    }

    @Override
    protected void tearDown() throws Exception {
        axisService = null;
        super.tearDown();
    }

    public void testWSDLSupplierWSDL11() throws Exception {
        Object value = new TestWSDL11Supplier();
        axisService.addParameter(Constants.WSDL_SUPPLIER_PARAM, value);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        axisService.printWSDL(outputStream);
        String wsdl = outputStream.toString();
        assertTrue(wsdl
                .contains("<wsdl:definitions name=\"TestWSDL11SupplierDefinition\" xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\">"));
        assertTrue(wsdl.contains("</wsdl:definitions>"));
    }

    public void testWSDLSupplierWSDL1SupplierClass() throws Exception {
        String value = TestWSDL11Supplier.class.getName();
        axisService.addParameter(Constants.WSDL_11_SUPPLIER_CLASS_PARAM, value);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        axisService.printWSDL(outputStream);
        String wsdl = outputStream.toString();
        assertTrue(wsdl
                .contains("<wsdl:definitions name=\"TestWSDL11SupplierDefinition\" xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\">"));
        assertTrue(wsdl.contains("</wsdl:definitions>"));
    }

    public void testWSDLSupplierWSDL20() throws Exception {
        String value = TestWSDL20Supplier.class.getName();
        axisService.addParameter(Constants.WSDL_20_SUPPLIER_CLASS_PARAM, value);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        axisService.printWSDL2(outputStream);
        String wsdl = outputStream.toString();
        assertTrue(wsdl.contains("<wsdl:description xmlns:wsdl=\"http://www.w3.org/ns/wsdl\">"));
        assertTrue(wsdl.contains("</wsdl:description>"));
    }

    public void testWSDLSupplierWSDL2SupplierClass() throws Exception {
        Object value = new TestWSDL20Supplier();
        axisService.addParameter(Constants.WSDL_SUPPLIER_PARAM, value);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        axisService.printWSDL2(outputStream);
        String wsdl = outputStream.toString();
        assertTrue(wsdl.contains("<wsdl:description xmlns:wsdl=\"http://www.w3.org/ns/wsdl\">"));
        assertTrue(wsdl.contains("</wsdl:description>"));
    }

    public void testWSDL11SupplierTemplate() throws Exception {
        WSDL11SupplierTemplate value = new TestWSDL11SupplierTemplate();
        axisService.addParameter(Constants.WSDL_SUPPLIER_PARAM, value);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        axisService.printWSDL(outputStream);
        String wsdl = outputStream.toString();
        assertTrue(wsdl.contains("<wsdl:definitions"));
        assertTrue(wsdl.contains("xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" "));
        assertTrue(wsdl.contains("xmlns:wsaw=\"http://www.w3.org/2006/05/"));
        assertTrue(wsdl.contains("xmlns:tns=\"http://ws.apache.org/axis2\""));
        assertTrue(wsdl.contains("xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" "));
        assertTrue(wsdl.contains("<wsdl:documentation>"));
        assertTrue(wsdl.contains("<ap:detail xmlns:ap=\"http://axis.apache.org\">"));
        assertTrue(wsdl.contains("<ap:name>Apache Axis2</ap:name>"));
        assertTrue(wsdl.contains("<ap:email>user@axis.apache.org</ap:email>"));
        assertTrue(wsdl.contains(" </ap:detail>"));
        assertTrue(wsdl.contains("</wsdl:documentation>"));
        assertFalse(wsdl.contains("<documentation/>"));
    }

    public void testWSDL11SupplierTemplateWSDL1SupplierClass() throws Exception {
        String value = TestWSDL11SupplierTemplate.class.getName();
        axisService.addParameter(Constants.WSDL_11_SUPPLIER_CLASS_PARAM, value);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        axisService.printWSDL(outputStream);
        String wsdl = outputStream.toString();
        assertTrue(wsdl.contains("<wsdl:definitions"));
        assertTrue(wsdl.contains("xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" "));
        assertTrue(wsdl.contains("xmlns:wsaw=\"http://www.w3.org/2006/05/"));
        assertTrue(wsdl.contains("xmlns:tns=\"http://ws.apache.org/axis2\""));
        assertTrue(wsdl.contains("xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" "));
        assertTrue(wsdl.contains("<wsdl:documentation>"));
        assertTrue(wsdl.contains("<ap:detail xmlns:ap=\"http://axis.apache.org\">"));
        assertTrue(wsdl.contains("<ap:name>Apache Axis2</ap:name>"));
        assertTrue(wsdl.contains("<ap:email>user@axis.apache.org</ap:email>"));
        assertTrue(wsdl.contains(" </ap:detail>"));
        assertTrue(wsdl.contains("</wsdl:documentation>"));
        assertFalse(wsdl.contains("<documentation/>"));
    }

    public void testWSDL20SupplierTemplate() throws Exception {
        TestWSDL20SupplierTemplate value = new TestWSDL20SupplierTemplate();
        axisService.addParameter(Constants.WSDL_SUPPLIER_PARAM, value);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        axisService.printWSDL2(outputStream);
        String wsdl = outputStream.toString();
        assertTrue(wsdl.contains("<wsdl2:description"));
        assertTrue(wsdl.contains("xmlns:wsdl2=\"http://www.w3.org/ns/wsdl\""));
        assertTrue(wsdl.contains("xmlns:wsaw=\"http://www.w3.org/2006/05/addressing/wsd"));
        assertTrue(wsdl.contains("xmlns:tns=\"http://ws.apache.org/axis2\""));
        assertTrue(wsdl.contains("xmlns:wsoap=\"http://www.w3.org/ns/wsdl/soap\" "));
        assertTrue(wsdl.contains("<wsdl2:documentation>"));
        assertTrue(wsdl.contains("<ap:detail xmlns:ap=\"http://axis.apache.org\">"));
        assertTrue(wsdl.contains("<ap:name>Apache Axis2</ap:name>"));
        assertTrue(wsdl.contains("<ap:email>user@axis.apache.org</ap:email>"));
        assertTrue(wsdl.contains(" </ap:detail>"));
        assertTrue(wsdl.contains("</wsdl2:documentation>"));
        assertFalse(wsdl.contains("<documentation/>"));
    }

    public void testWSDL11SupplierTemplateWSDL20SupplierClass() throws Exception {
        String value = TestWSDL20SupplierTemplate.class.getName();
        axisService.addParameter(Constants.WSDL_20_SUPPLIER_CLASS_PARAM, value);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        axisService.printWSDL2(outputStream);
        String wsdl = outputStream.toString();
        assertTrue(wsdl.contains("<wsdl2:description"));
        assertTrue(wsdl.contains("xmlns:wsdl2=\"http://www.w3.org/ns/wsdl\""));
        assertTrue(wsdl.contains("xmlns:wsaw=\"http://www.w3.org/2006/05/addressing/wsd"));
        assertTrue(wsdl.contains("xmlns:tns=\"http://ws.apache.org/axis2\""));
        assertTrue(wsdl.contains("xmlns:wsoap=\"http://www.w3.org/ns/wsdl/soap\" "));
        assertTrue(wsdl.contains("<wsdl2:documentation>"));
        assertTrue(wsdl.contains("<ap:detail xmlns:ap=\"http://axis.apache.org\">"));
        assertTrue(wsdl.contains("<ap:name>Apache Axis2</ap:name>"));
        assertTrue(wsdl.contains("<ap:email>user@axis.apache.org</ap:email>"));
        assertTrue(wsdl.contains(" </ap:detail>"));
        assertTrue(wsdl.contains("</wsdl2:documentation>"));
        assertFalse(wsdl.contains("<documentation/>"));
    }

}
