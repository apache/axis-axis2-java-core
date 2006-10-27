/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.provider;


import java.io.ByteArrayInputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.apache.axis2.jaxws.provider.soapmsg.SoapMessageProvider;

/**
 * Tests Dispatch<SOAPMessage> client and a Provider<SOAPMessage> service.
 * The client and service interaction tests various xml and attachment scenarios
 *
 */
public class SoapMessageProviderTests extends ProviderTestCase {

    private String endpointUrl = "http://localhost:8080/axis2/services/SoapMessageProviderService";
    private QName serviceName = new QName("http://ws.apache.org/axis2", "SoapMessageProviderService");
    
    private String reqMsgStart = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>" +
    "<ns2:invoke xmlns:ns2=\"http://org.test.soapmessage\"><invoke_str>";
    
    private String reqMsgEnd = "</invoke_str></ns2:invoke></soap:Body></soap:Envelope>";
   
    protected void setUp() throws Exception {
            super.setUp();
    }

    protected void tearDown() throws Exception {
            super.tearDown();
    }

    public SoapMessageProviderTests(String name) {
        super(name);
    }
    
    /**
     * Sends an SOAPMessage containing only xml data to the web service.  
     * Receives a response containing just xml data.
     */
    public void testProviderSource1(){
        try{       
            // Create the dispatch
            Dispatch<SOAPMessage> dispatch = createDispatch();
            
            // Create the SOAPMessage
            String msg = reqMsgStart + SoapMessageProvider.XML_REQUEST + reqMsgEnd;
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage request = factory.createMessage(null, 
                    new ByteArrayInputStream(msg.getBytes()));
            
            // Dispatch
        	System.out.println(">> Invoking SourceMessageProviderDispatch");
        	SOAPMessage response = dispatch.invoke(request);

            // Check assertions and get the data element
            SOAPElement dataElement = assertResponseXML(response, SoapMessageProvider.XML_RESPONSE);
            
            assertTrue(countAttachments(response) == 0);
            
            // Print out the response
        	System.out.println(">> Response [" + response.toString() + "]");
            response.writeTo(System.out);
        	
        }catch(Exception e){
        	e.printStackTrace();
            fail("Caught exception " + e);
        }
        
    }
    
    /**
     * Sends an SOAPMessage containing xml data and raw attachments to the web service.  
     * Receives a response containing xml data and the same raw attachments.
     */
    /** TODO Disable while I implement the code
    public void testProviderSource2(){
        try{       
            // Create the dispatch
            Dispatch<SOAPMessage> dispatch = createDispatch();
            
            // Create the SOAPMessage
            String msg = reqMsgStart + SoapMessageProvider.XML_ATTACHMENT_REQUEST + reqMsgEnd;
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage request = factory.createMessage(null, 
                    new ByteArrayInputStream(msg.getBytes()));
            
            // Add the Attachment
            AttachmentPart ap = request.createAttachmentPart(SoapMessageProvider.TEXT_XML_ATTACHMENT, "text/xml");
            ap.setContentId(SoapMessageProvider.ID);
            request.addAttachmentPart(ap);
            
            System.out.println("Request Message:");
            request.writeTo(System.out);
            
            // Dispatch
            System.out.println(">> Invoking SourceMessageProviderDispatch");
            SOAPMessage response = dispatch.invoke(request);

            // Check assertions and get the data element
            SOAPElement dataElement = assertResponseXML(response, SoapMessageProvider.XML_ATTACHMENT_RESPONSE);
            assertTrue(countAttachments(response) == 1);
            
            // Get the Attachment
            AttachmentPart attachmentPart = (AttachmentPart) response.getAttachments().next();
            
            // Check the attachment
            String content = (String) attachmentPart.getContent();
            assertTrue(content != null);
            assertTrue(SoapMessageProvider.TEXT_XML_ATTACHMENT.equals(content));
            
            // Print out the response
            System.out.println(">> Response [" + response.toString() + "]");
            response.writeTo(System.out);
            
        }catch(Exception e){
            e.printStackTrace();
            fail("Caught exception " + e);
        }
        
    }
    */
    
    /**
     * @return
     * @throws Exception
     */
    private Dispatch<SOAPMessage> createDispatch() throws Exception {
        Service svc = Service.create(serviceName);
        svc.addPort(portName,null, endpointUrl);
        Dispatch<SOAPMessage> dispatch = svc.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
        return dispatch;
    }
    
    /**
     * Common assertion checking of the response
     * @param msg
     * @param expectedText
     * @return SOAPElement representing the data element
     */
    private SOAPElement assertResponseXML(SOAPMessage msg, String expectedText) throws Exception {
        assertTrue(msg != null);
        SOAPBody body = msg.getSOAPBody();
        assertTrue(body != null);
        
        Node invokeElement = (Node) body.getFirstChild();
        assert(invokeElement instanceof SOAPElement);
        assert(SoapMessageProvider.RESPONSE_NAME.equals(invokeElement.getLocalName()));
        
        Node dataElement = (Node) invokeElement.getFirstChild();
        assert(dataElement instanceof SOAPElement);
        assert(SoapMessageProvider.RESPONSE_DATA_NAME.equals(dataElement.getLocalName()));
        
        // TODO AXIS2 SAAJ should (but does not) support the getTextContent();
        // String text = dataElement.getTextContent();
        String text = dataElement.getValue();
        assertEquals("Found ("+ text + ") but expected (" + expectedText + ")", expectedText, text);
        
        return (SOAPElement) dataElement;
    }
    
    /**
     * Count Attachments
     * @param msg
     * @return
     */
    private int countAttachments(SOAPMessage msg) {
        Iterator it = msg.getAttachments();
        int count = 0;
        assert(it != null);
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }
}
