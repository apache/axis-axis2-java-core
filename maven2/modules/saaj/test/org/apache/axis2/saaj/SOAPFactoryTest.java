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

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 */
public class SOAPFactoryTest extends TestCase {
    public void testCreateDetail() {
        try {
            SOAPFactory sf = SOAPFactory.newInstance();
            if (sf == null) {
                fail("SOAPFactory was null");
            }
            Detail d = sf.createDetail();
            if (d == null) {
                fail("Detail was null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception " + e);
        }
    }
    
    public void testCreateElement(){
    	try 
    	{
    		//SOAPFactory sf = SOAPFactory.newInstance();
    		SOAPFactory sf = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
    		if(sf == null) {
    			fail("createElementTest4() could not create SOAPFactory object");
    		}
    		System.out.println("Create QName object with localName=MyName1, " +
    		"prefix=MyPrefix1, uri=MyUri1");
    		QName name =  new QName("MyUri1", "MyName1", "MyPrefix1");
    		System.out.println("Create SOAPElement object with above QName object");
    		SOAPElement se = sf.createElement(name);
    		if(se == null) {
    			fail("createElementTest4() could not create SOAPElement object");
    		} else {
    			name = se.getElementQName();
    			String localName = name.getLocalPart();
    			String prefix = name.getPrefix();
    			String uri = name.getNamespaceURI();
    			System.out.println("localName=" + localName);
    			System.out.println("prefix=" + prefix);
    			System.out.println("uri=" + uri);
    			if(localName == null) {
    				fail("localName is null (expected MyName1)");
    			} else if(!localName.equals("MyName1")) {
    				fail("localName is wrong (expected MyName1)");
    			} else if(prefix == null) {
    				fail("prefix is null (expected MyPrefix1)");
    			} else if(!prefix.equals("MyPrefix1")) {
    				fail("prefix is wrong (expected MyPrefix1)");
    			} else if(uri == null) {
    				fail("uri is null (expected MyUri1)");
    			} else if(!uri.equals("MyUri1")) {
    				fail("uri is wrong (expected MyUri1)");
    			}
    		} 
    	}
    	catch(Exception e){
    		fail();
    	}
    }
    
    
    public void testCreateElement2(){
    	try 
    	{
    		SOAPFactory sf = SOAPFactory.newInstance();
    		//SOAPFactory sf = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);    		
    	    if(sf == null) {
    	    	fail("could not create SOAPFactory object");
    	    }
    	    System.out.println("Create a DOMElement");
    	    DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
    	    DocumentBuilder builder = dbfactory.newDocumentBuilder();
    	    Document document = builder.newDocument();
    	    Element de = document.createElementNS("http://MyNamespace.org/", "MyTag");
    	    System.out.println("Create a SOAPElement from a DOMElement");
    	    System.out.println("Calling SOAPFactory.createElement(org.w3c.dom.Element)");
    	    SOAPElement se = sf.createElement(de);
    	    System.out.println("Check that DOMElement and SOAPElement names are equal");
    	    System.out.println("DOMElement name="+de.getNodeName());
    	    System.out.println("DOMElement prefix="+de.getPrefix());
    	    System.out.println("DOMElement uri="+de.getNamespaceURI());
    	    System.out.println("SOAPElement name="+se.getNodeName());
    	    System.out.println("SOAPElement prefix="+se.getPrefix());
    	    System.out.println("SOAPElement uri="+se.getNamespaceURI());
    	    if(!de.getNodeName().equals(se.getNodeName()) || !de.getNamespaceURI().equals(
    		se.getNamespaceURI())) {
    		System.out.println("Node names are not equal");
    		System.out.println("Got: <URI="+se.getNamespaceURI()+", PREFIX="+
    			se.getPrefix()+", NAME="+se.getNodeName()+">");
    		System.out.println("Expected: <URI="+de.getNamespaceURI()+", PREFIX="+
    			de.getPrefix()+", NAME="+de.getNodeName()+">");
    	    }
    	} catch(Exception e) {
    	    fail("Exception: " + e);
    	}
    }
    
    public void testCreateElement3(){
    	try {
    		SOAPFactory factory = SOAPFactory.newInstance();
    		if(factory == null) {
    			fail("createFaultTest1() could not create SOAPFactory object");
    		}
    		SOAPFault sf = factory.createFault();
    		if(sf == null) {
    			fail("createFault() returned null");
    		} else if(!(sf instanceof SOAPFault)) {
    			fail("createFault() did not create a SOAPFault object");
    		}
    	} catch(Exception e) {
    		fail();
    	}
    }
    
    public void testCreateElement4(){
    	try 
    	{
    		SOAPFactory sf = SOAPFactory.newInstance();
    		if(sf == null) {
    			fail("createElementTest6() could not create SOAPFactory object");

    		}
    		System.out.println("Create first SOAPElement");
    		QName qname = new QName("http://MyNamespace.org/", "MyTag");
    		SOAPElement se1 = sf.createElement(qname);

    		System.out.println("Create second SOAPElement from first SOAPElement");
    		System.out.println("Calling SOAPFactory.createElement(SOAPElement)");
    		SOAPElement se2 = sf.createElement(se1);
    		System.out.println("Check the two SOAPElement's for equality and sameness");
    		if(!se1.isEqualNode(se2) && !se1.isSameNode(se2)) {
    			System.out.println(
    			"The SOAPElement's are not equal and not the same (unexpected)");
    		} else{
    			System.out.println("The SOAPElement's are equal and the same (expected)");
    		}

    		System.out.println("Check that SOAPElement names are equal");
    		System.out.println("SOAPElement1 name="+se1.getNodeName());
    		System.out.println("SOAPElement1 prefix="+se1.getPrefix());
    		System.out.println("SOAPElement1 uri="+se1.getNamespaceURI());
    		System.out.println("SOAPElement2 name="+se2.getNodeName());
    		System.out.println("SOAPElement2 prefix="+se2.getPrefix());
    		System.out.println("SOAPElement2 uri="+se2.getNamespaceURI());
    		if(!se1.getNodeName().equals(se2.getNodeName()) || !se1.getNamespaceURI().equals(
    				se2.getNamespaceURI())) {
    			System.out.println("Node names are not equal");
    			System.out.println("Got: <URI="+se1.getNamespaceURI()+", PREFIX="+
    					se1.getPrefix()+", NAME="+se1.getNodeName()+">");
    			System.out.println("Expected: <URI="+se2.getNamespaceURI()+", PREFIX="+
    					se2.getPrefix()+", NAME="+se2.getNodeName()+">");
    		}	
    	} catch(Exception e) {
    		fail();
    	}
    }

    public void testCreateFault(){
    	try {
    		SOAPFactory factory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
    		//SOAPFactory factory = SOAPFactory.newInstance();
    		if(factory == null) {
    			System.out.println(
    			"createFaultTest2() could not create SOAPFactory object");
    		}
    		SOAPFault sf = factory.createFault("This is the fault reason.",  
    				SOAPConstants.SOAP_RECEIVER_FAULT);
    		if(sf == null) {
    			System.out.println("createFault() returned null");
    		} else if(!(sf instanceof SOAPFault)) {
    			System.out.println("createFault() did not create a SOAPFault object");
    		}
    		QName fc = sf.getFaultCodeAsQName();
    		System.out.println("Expected FaultCode="+SOAPConstants.SOAP_RECEIVER_FAULT);
    		System.out.println("Expected ReasonText=This is the fault reason.");
    		System.out.println("Actual FaultCode="+fc);
    		Iterator i = sf.getFaultReasonTexts();
    		if(i == null) {
    			System.out.println("Call to getFaultReasonTexts() returned null iterator");
    		}
    		String reason = "";
    		while(i.hasNext()) reason += (String)i.next();
    		System.out.println("Actual ReasonText="+reason);
    		if(reason == null || !reason.contains("This is the fault reason.")) {
    			System.out.println("Actual ReasonText is not equal expected ReasonText");
    		}
    		if(!fc.equals(SOAPConstants.SOAP_RECEIVER_FAULT)) {
    			System.out.println("Actual FaultCode is not equal expected FaultCode");
    		}
    	} catch(SOAPException e) {
    		System.out.println("Caught unexpected SOAPException");
    	}
    }
    
    public void testCreateFault1(){
    	try 
    	{
    		//SOAPFactory factory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
    		SOAPFactory factory = SOAPFactory.newInstance();

    		if(factory == null) {
    			System.out.println(
    			"createFaultTest2() could not create SOAPFactory object");
    		}
    		SOAPFault sf = factory.createFault("This is the fault reason.",  
    				SOAPConstants.SOAP_RECEIVER_FAULT);
    		if(sf == null) {
    			System.out.println("createFault() returned null");
    		} else if(!(sf instanceof SOAPFault)) {
    			System.out.println("createFault() did not create a SOAPFault object");
    		}
    		QName fc = sf.getFaultCodeAsQName();
    		System.out.println("Expected FaultCode="+SOAPConstants.SOAP_RECEIVER_FAULT);
    		System.out.println("Expected ReasonText=This is the fault reason.");
    		System.out.println("Actual FaultCode="+fc);
    		Iterator i = sf.getFaultReasonTexts();
    		if(i == null) {
    			System.out.println("Call to getFaultReasonTexts() returned null iterator");
    		}
    		String reason = "";
    		while(i.hasNext()) reason += (String)i.next();
    		System.out.println("Actual ReasonText="+reason);
    		if(reason == null || !reason.contains("This is the fault reason.")) {
    			System.out.println("Actual ReasonText is not equal expected ReasonText");
    		}
    		if(!fc.equals(SOAPConstants.SOAP_RECEIVER_FAULT)) {
    			System.out.println("Actual FaultCode is not equal expected FaultCode");
    		}
    	} catch(SOAPException e) {
    		System.out.println("Caught expected SOAPException");
    	} catch(Exception e) {
    		System.out.println("Exception: " + e);
    	}
    }
    
    /**
     * for soap 1.1
     */
    public void testSOAPFaultException1(){
    	try {
    		SOAPFactory factory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
    		if(factory == null) {
    			System.out.println(
    			"createFaultSOAPExceptionTest1() could not create SOAPFactory object");
    		}
    		SOAPFault sf = factory.createFault("This is the fault reason.",  
    				new QName("http://MyNamespaceURI.org/", "My Fault Code"));
    	} catch(UnsupportedOperationException e) {
    		System.out.println("Caught expected UnsupportedOperationException");
    	} catch(SOAPException e) {
    		System.out.println("Caught expected SOAPException");
    	} catch(IllegalArgumentException e) {
    		System.out.println("Caught expected IllegalArgumentException");
    	} catch(Exception e) {
    		System.out.println("Exception: " + e);
    	}    	
    }

    /**
     * for soap 1.2
     */
    public void testSOAPFaultException2(){
    	try {
    		SOAPFactory factory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
    		if(factory == null) {
    			System.out.println(
    			"createFaultSOAPExceptionTest1() could not create SOAPFactory object");
    		}
    		SOAPFault sf = factory.createFault("This is the fault reason.",  
    				new QName("http://MyNamespaceURI.org/", "My Fault Code"));
    		System.out.println("Did not throw expected SOAPException");
    	} catch(UnsupportedOperationException e) {
    		System.out.println("Caught expected UnsupportedOperationException");
    	} catch(SOAPException e) {
    		System.out.println("Caught expected SOAPException");
    	} catch(IllegalArgumentException e) {
    		System.out.println("Caught expected IllegalArgumentException");
    	} catch(Exception e) {
    		System.out.println("Exception: " + e);
    	}    	
    }
}
