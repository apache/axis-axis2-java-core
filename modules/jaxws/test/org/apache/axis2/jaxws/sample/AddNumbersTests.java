/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.sample;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.sample.addnumbers.AddNumbersPortType;
import org.apache.axis2.jaxws.sample.addnumbers.AddNumbersService;

public class AddNumbersTests extends TestCase {
	
    String axisEndpoint = "http://localhost:8080/axis2/services/AddNumbersService";
	
    public void testAddNumbers() {
		try{
			System.out.println("----------------------------------");
		    System.out.println("test: " + getName());
			
            AddNumbersService service = new AddNumbersService();
			AddNumbersPortType proxy = service.getAddNumbersPort();
			
            BindingProvider p =	(BindingProvider)proxy;
			p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);	
			int total = proxy.addNumbers(10,10);
			
            assertEquals("With handler manipulation, total should be 2 less than a proper sumation.", 18, total);
			System.out.println("Total (after handler manipulation) = " +total);
			System.out.println("----------------------------------");
		} catch(Exception e) {
			e.printStackTrace();
			fail();
		}
	}
    
    public void testAddNumbersWithFault() {
        try{
            System.out.println("----------------------------------");
            System.out.println("test: " + getName());
            
            AddNumbersService service = new AddNumbersService();
            AddNumbersPortType proxy = service.getAddNumbersPort();
            
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);  
            // value 99 triggers the handler to throw an exception, but does
            // NOT trigger the AddNumbersHandler.handlefault method.
            // The spec does not call the handlefault method of a handler that
            // causes a flow reversal
            int total = proxy.addNumbers(99,10);
            
            fail("We should have got an exception due to the handler.");
        } catch(Exception e) {
            e.printStackTrace();
            assertTrue("Exception should be SOAPFaultException", e instanceof SOAPFaultException);
            assertEquals(((SOAPFaultException)e).getMessage(), "I don't like the value 99");
        }
        System.out.println("----------------------------------");
    }
    
    public void testOneWay() {
        try {
            System.out.println("----------------------------------");
            System.out.println("test: " + getName());
            
            AddNumbersService service = new AddNumbersService();
            AddNumbersPortType proxy = service.getAddNumbersPort();
            
            BindingProvider bp = (BindingProvider) proxy;
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);
            proxy.oneWayInt(11);
            System.out.println("----------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }       
    }
}
