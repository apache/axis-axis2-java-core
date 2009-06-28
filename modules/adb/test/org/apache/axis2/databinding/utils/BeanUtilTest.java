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

package org.apache.axis2.databinding.utils;

import org.apache.axiom.om.*;
import org.apache.axis2.engine.DefaultObjectSupplier;
import org.apache.axis2.engine.ObjectSupplier;

import junit.framework.TestCase;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;

import java.util.List;


public class BeanUtilTest extends TestCase {

    public class ComplexType {
        private String child;

        public void setChild(String child) {
            this.child = child;
        }

        public String getChild() {
            return child;
        }
    }
    
    private ObjectSupplier objectSupplier;

    private OMFactory omFactory;
    private OMElement omElement;
    private OMNamespace xsiNamespace;
    
    @Override
    protected void setUp() throws Exception {
        objectSupplier = new DefaultObjectSupplier();
        
        omFactory = OMAbstractFactory.getOMFactory();
        xsiNamespace = omFactory.createOMNamespace(Constants.XSI_NAMESPACE, "xsi");
        omElement = omFactory.createOMElement(new QName("hello"));
    }
    
    public void testProcessObjectAsSimpleType() throws Exception {
        omElement.setText("World");
        
        Object result = BeanUtil.processObject(omElement, String.class, new MultirefHelper(omElement), false, objectSupplier, null);
        assertTrue(result instanceof String);
        assertEquals("World", result);
    }

    public void testProcessObjectAsOmElement() throws Exception {
        omElement.setText("World");
        
        Object result = BeanUtil.processObject(omElement, OMElement.class, new MultirefHelper(omElement), false, objectSupplier, null);
        assertTrue(result instanceof OMElement);
        assertEquals(omElement, result);
    }
    
    public void testProcessObjectAsNull() throws Exception {
        OMAttribute nilAttribute = omFactory.createOMAttribute("nil", xsiNamespace, "true");
        omElement.addAttribute(nilAttribute);
        
        Object result = BeanUtil.processObject(omElement, String.class, new MultirefHelper(omElement), false, objectSupplier, null);
        assertNull(result);
    }

    public void testProcessObjectAsByteArray() throws Exception {
        omElement.setText("Word");
        
        Object result = BeanUtil.processObject(omElement, byte.class, new MultirefHelper(omElement), true, objectSupplier, null);
        assertTrue(result instanceof byte[]);
        assertEquals(3, ((byte[]) result).length);
    }

    public void testProcessObjectAsList() throws Exception {
        OMElement child = omFactory.createOMElement(new QName("child"), omElement);
        child.setText("World");
        
        Object result = BeanUtil.processObject(omElement, List.class, new MultirefHelper(omElement), false, objectSupplier, null);
        assertTrue(result instanceof List);
        assertEquals(1, ((List) result).size());
    }

    public void testProcessObjectAsDataHandler() throws Exception {
        omElement.setText("Word");
        
        Object result = BeanUtil.processObject(omElement, DataHandler.class, new MultirefHelper(omElement), false, objectSupplier, null);
        assertTrue(result instanceof DataHandler);
    }

    public void testProcessObjectAsComplexType() throws Exception {
        OMElement child = omFactory.createOMElement(new QName("child"), omElement);
        child.setText("World");
        
        Object result = BeanUtil.processObject(omElement, ComplexType.class, new MultirefHelper(omElement), false, objectSupplier, null);
        assertTrue(result instanceof ComplexType);
        assertEquals("World", ((ComplexType) result).getChild());
    }

    public void testProcessObjectAsObject() throws Exception {
        omElement.declareNamespace(omFactory.createOMNamespace(Constants.XSD_NAMESPACE, "xs"));

        omElement.setText("World");
        omElement.addAttribute(createTypeAttribute("xs:string"));
        
        Object result = BeanUtil.processObject(omElement, Object.class, new MultirefHelper(omElement), false, objectSupplier, null);
        assertTrue(result instanceof OMText);
        assertEquals("World", ((OMText) result).getText());
    }
    
    private OMAttribute createTypeAttribute(String value) {
        return omFactory.createOMAttribute("type", xsiNamespace, value);
    }
}
