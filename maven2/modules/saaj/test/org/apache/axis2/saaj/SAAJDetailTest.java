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
package org.apache.axis2.saaj;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import junit.framework.TestCase;

/**
 * 
 */
public class SAAJDetailTest extends TestCase {
    private SOAPMessage msg = null;
    private SOAPPart sp = null;
    private SOAPBody body = null;
    private SOAPEnvelope envelope = null;
    private SOAPHeader header = null;
    private SOAPHeaderElement headerEle = null;

    protected void setUp() throws Exception {
        msg = MessageFactory.newInstance().createMessage();
        sp = msg.getSOAPPart();
        envelope = sp.getEnvelope();
        body = envelope.getBody();
        header = envelope.getHeader();
        headerEle = header.addHeaderElement(envelope.createName("foo", "f", "foo-URI"));
        headerEle.setActor("actor-URI");
    }

    

    public void testAddDetailEntry() {
    	try 
    	{
    		System.out.println("Add a SOAPFault object to the SOAPBody");
    		SOAPFault sf = body.addFault();
    		System.out.println("Add a Detail object to the SOAPFault object");
    		Detail d = sf.addDetail();
    		QName name = new QName("http://www.wombat.org/trader", 
    				"GetLastTradePrice", "WOMBAT");
    		System.out.println("Add a DetailEntry object to the Detail object");
    		DetailEntry de = d.addDetailEntry(name);
    		System.out.println("Successfully created DetailEntry object");
    		if(de == null) {
    			System.out.println("addDetailEntry() returned null");
    		} else if(!(de instanceof DetailEntry)) {
    			System.out.println(
    			"addDetailEntry() did not return a DetailEntry object");
    		}
    		else 
    			System.out.println("Successfully created DetailEntry object");

    	} catch (Exception e) {
    		fail("Exception: " + e);
    	}
    }

    /*
     * for soap version 1.2
     */
    public void testAddDetailEntry2() {
    	try 
    	{
            msg = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
            sp = msg.getSOAPPart();
            envelope = sp.getEnvelope();
            body = envelope.getBody();
    		
    		System.out.println("Add a SOAPFault object to the SOAPBody");
    		SOAPFault sf = body.addFault();
    		System.out.println("Add a Detail object to the SOAPFault object");
    		Detail d = sf.addDetail();
    		QName name = new QName("http://www.wombat.org/trader", 
    				"GetLastTradePrice", "WOMBAT");
    		System.out.println("Add a DetailEntry object to the Detail object");
    		DetailEntry de = d.addDetailEntry(name);
    		System.out.println("Successfully created DetailEntry object");
    		if(de == null) {
    			System.out.println("addDetailEntry() returned null");
    		} else if(!(de instanceof DetailEntry)) {
    			System.out.println("addDetailEntry() did not return a DetailEntry object");
    		}
    		else 
    			System.out.println("Successfully created DetailEntry object");

    	} catch (Exception e) {
    		fail("Exception: " + e);
    	}
    }
    
    
}
