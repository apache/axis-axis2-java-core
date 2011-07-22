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
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.apache.axis2.engine.DefaultObjectSupplier;
import org.apache.axis2.engine.ObjectSupplier;

import junit.framework.TestCase;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;


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

    private SOAPFactory omFactory;
    private OMElement omElement;
    private OMNamespace xsiNamespace;
    
    @Override
    protected void setUp() throws Exception {
        objectSupplier = new DefaultObjectSupplier();
        
        omFactory = OMAbstractFactory.getSOAP12Factory();
        xsiNamespace = omFactory.createOMNamespace(Constants.XSI_NAMESPACE, "xsi");
        omElement = omFactory.createOMElement(new QName("hello"));

        MessageContext msgContext = new MessageContext();
        msgContext.setEnvelope(omFactory.createSOAPEnvelope());
        MessageContext.setCurrentMessageContext(msgContext);
    }

    @Override
    protected void tearDown() throws Exception {
        MessageContext.setCurrentMessageContext(null);
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
        
        Object result = BeanUtil.processObject(omElement.getFirstElement(), List.class, new MultirefHelper(omElement), false, objectSupplier, List.class);
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
    
    /**
     * Test that for a {@link DataHandler} object, {@link BeanUtil} creates sequence of
     * events that allows Axiom to recognize the optimized binary.
     */
    public void testGetOMElementWithDataHandlerArg() {
        DataHandler dh = new DataHandler(new ByteArrayDataSource(new byte[4096],
                "application/octet-stream"));
        OMElement element = BeanUtil.getOMElement(new QName("urn:ns1", "myop"),
                new Object[] { dh }, new QName("urn:ns1", "part"), true, new TypeTable());
        OMText text = (OMText)element.getFirstElement().getFirstOMChild();
        assertTrue(text.isOptimized());
        assertSame(dh, text.getDataHandler());
    }

    public void testProcessObjectWithWrongType() throws Exception {
        omElement.setLocalName("Queensland");
        omElement.setText("Brisbane");

        try {
            BeanUtil.processObject(omElement, int.class, new MultirefHelper(omElement), true, objectSupplier, null);
        } catch (AxisFault e) {
            assertEquals(org.apache.axis2.Constants.FAULT_SOAP12_SENDER, e.getFaultCode());
            assertTrue(e.getMessage().contains("Queensland"));
            assertTrue(e.getMessage().contains("Brisbane"));
        }
    }

    public void testDeserializeWithWrongType() throws Exception {
        omElement.setLocalName("Queensland");
        omElement.setText("Brisbane");

        try {
            BeanUtil.deserialize(int.class, omElement, objectSupplier, "Queensland");
        } catch (AxisFault e) {
            assertEquals(org.apache.axis2.Constants.FAULT_SOAP12_SENDER, e.getFaultCode());
            assertTrue(e.getMessage().contains("Queensland"));
            assertTrue(e.getMessage().contains("Brisbane"));
        }
    }
    
    public void testDeserializeWithArrayLocalNameForString() throws Exception {    	
    	   omElement.declareNamespace(omFactory.createOMNamespace(Constants.XSD_NAMESPACE, "xs"));

           omElement.setText("World");
           omElement.addAttribute(createTypeAttribute("xs:string"));
           
           Object result = BeanUtil.deserialize(String.class, omElement, objectSupplier, null);
           assertNotNull("Result can not be null",result);
           assertEquals("Not the expected Class",String.class,result.getClass());
           assertEquals("Not the expected value","World",result);
	   
   }
    
    public void testDeserializeWithArrayLocalNameForInt() throws Exception {    	
 	   omElement.declareNamespace(omFactory.createOMNamespace(Constants.XSD_NAMESPACE, "xs"));

        omElement.setText("1000");
        omElement.addAttribute(createTypeAttribute("xs:int"));
        
        Object result = BeanUtil.deserialize(Integer.class, omElement, objectSupplier, null);
        assertNotNull("Result can not be null",result);
        assertEquals("Not the expected Class",Integer.class,result.getClass());
        assertEquals("Not the expected value",1000,result);
	   
   } 
    public void testDeserializeWithArrayLocalNameForInteger() throws Exception {    	
  	   omElement.declareNamespace(omFactory.createOMNamespace(Constants.XSD_NAMESPACE, "xs"));

         omElement.setText("100000");
         omElement.addAttribute(createTypeAttribute("xs:integer"));
         
         Object result = BeanUtil.deserialize(BigInteger.class, omElement, objectSupplier, null);
         assertNotNull("Result can not be null",result);
         assertEquals("Not the expected Class",BigInteger.class,result.getClass());
         assertEquals("Not the expected value",new BigInteger("100000"),result);
 	   
    } 
    public void testDeserializeWithArrayLocalNameForBase64Binary() throws Exception {    	
  	   omElement.declareNamespace(omFactory.createOMNamespace(Constants.XSD_NAMESPACE, "xs"));

         omElement.setText("SGVsbG8gV29ybGQ=");
         omElement.addAttribute(createTypeAttribute("xs:base64Binary"));
         
         Object result = BeanUtil.deserialize(DataHandler.class, omElement, objectSupplier, null);
         assertNotNull("Result can not be null",result);
         assertEquals("Not the expected Class",DataHandler.class,result.getClass());
         assertEquals("Not the expected value","Hello World",toStr((ByteArrayInputStream) ((DataHandler)result).getContent()));
 	   
    } 
    public void testDeserializeWithArrayLocalNameForHexBinary() throws Exception {
    	 AxisService service = new AxisService();
    	 service.setTypeTable(new TypeTable());
    	 MessageContext.getCurrentMessageContext().setAxisService(service);
  	     omElement.declareNamespace(omFactory.createOMNamespace(Constants.XSD_NAMESPACE, "xs"));

         omElement.setText("48656c6c6f20576f726c64");
         omElement.addAttribute(createTypeAttribute("xs:hexBinary"));
         
         Object result = BeanUtil.deserialize(DataHandler.class, omElement, objectSupplier, null);
         assertNotNull("Result can not be null",result);
         assertEquals("Not the expected Class",DataHandler.class,result.getClass());
         assertEquals("Not the expected value","Hello World",toStr((ByteArrayInputStream) ((DataHandler)result).getContent()));
 	   
    } 
    
    public void testProcessSimpleMap() throws Exception {
    	OMNamespace ns = omFactory.createOMNamespace(org.apache.axis2.Constants.AXIS2_MAP_NAMESPACE_URI,
    			org.apache.axis2.Constants.AXIS2_MAP_NAMESPACE_PREFIX);
    	OMElement entry = omFactory.createOMElement(org.apache.axis2.Constants.MAP_ENTRY_ELEMENT_NAME,ns);
    	OMElement key = omFactory.createOMElement(org.apache.axis2.Constants.MAP_KEY_ELEMENT_NAME,ns);
    	OMElement value = omFactory.createOMElement(org.apache.axis2.Constants.MAP_VALUE_ELEMENT_NAME,ns);
    	key.setText("key1");
    	value.setText("value1");
    	entry.addChild(key);
    	entry.addChild(value);
    	omElement.addChild(entry);   
    	
        Object result = BeanUtil.processObject(omElement, Map.class, new MultirefHelper(omElement), false, objectSupplier, Map.class);
        assertTrue(result instanceof Map);       
    }
    
    private static String toStr(ByteArrayInputStream is) {
	    int size = is.available();
	    char[] theChars = new char[size];
	    byte[] bytes    = new byte[size];

	    is.read(bytes, 0, size);
	    for (int i = 0; i < size;)
	        theChars[i] = (char)(bytes[i++]&0xff);
	    
	    return new String(theChars);
	      }
  
   
}
