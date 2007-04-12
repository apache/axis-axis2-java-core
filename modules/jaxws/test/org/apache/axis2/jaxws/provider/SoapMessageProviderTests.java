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
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Binding;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.axis2.jaxws.provider.soapmsg.SoapMessageProvider;
import org.apache.axis2.jaxws.TestLogger;

/**
 * Tests Dispatch<SOAPMessage> client and a Provider<SOAPMessage> service.
 * The client and service interaction tests various xml and attachment scenarios
 *
 */
public class SoapMessageProviderTests extends ProviderTestCase {

    private String endpointUrl = "http://localhost:8080/axis2/services/SoapMessageProviderService";
    private QName serviceName = new QName("http://ws.apache.org/axis2", "SoapMessageProviderService");
    
    private String reqMsgStart = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>";
    ;
    
    private String reqMsgEnd = "</soap:Body></soap:Envelope>";
   
    private String XML_INVOKE = "<ns2:invokeOp xmlns:ns2=\"http://org.test.soapmessage\"><invoke_str>" + 
        SoapMessageProvider.XML_REQUEST +
        "</invoke_str></ns2:invokeOp>";
    private String EMPTYBODY_INVOKE = "<ns2:invokeOp xmlns:ns2=\"http://org.test.soapmessage\"><invoke_str>" + 
        SoapMessageProvider.XML_EMPTYBODY_REQUEST +
        "</invoke_str></ns2:invokeOp>";
    private String ATTACHMENT_INVOKE = "<ns2:invokeOp xmlns:ns2=\"http://org.test.soapmessage\"><invoke_str>" + 
        SoapMessageProvider.XML_ATTACHMENT_REQUEST +
        "</invoke_str></ns2:invokeOp>";
    private String MTOM_INVOKE = "<ns2:invokeOp xmlns:ns2=\"http://org.test.soapmessage\"><invoke_str>" + 
        SoapMessageProvider.XML_MTOM_REQUEST +
        "</invoke_str>" + 
        SoapMessageProvider.MTOM_REF +
        "</ns2:invokeOp>";
    private String SWAREF_INVOKE = "<ns2:invokeOp xmlns:ns2=\"http://org.test.soapmessage\"><invoke_str>" + 
        SoapMessageProvider.XML_SWAREF_REQUEST +
        "</invoke_str>" + 
        SoapMessageProvider.SWAREF_REF +
        "</ns2:invokeOp>";   
    private String XML_FAULT_INVOKE = "<ns2:invokeOp xmlns:ns2=\"http://org.test.soapmessage\"><invoke_str>" + 
    SoapMessageProvider.XML_FAULT_REQUEST +
    "</invoke_str></ns2:invokeOp>";
    private String XML_WSE_INVOKE = "<ns2:invokeOp xmlns:ns2=\"http://org.test.soapmessage\"><invoke_str>" + 
    SoapMessageProvider.XML_WSE_REQUEST +
    "</invoke_str></ns2:invokeOp>";
                
    
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
    public void testProviderSourceXMLOnly(){
        try{       
            // Create the dispatch
            Dispatch<SOAPMessage> dispatch = createDispatch();
            
            // Create the SOAPMessage
            String msg = reqMsgStart + XML_INVOKE + reqMsgEnd;
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage request = factory.createMessage(null, 
                    new ByteArrayInputStream(msg.getBytes()));
            
            // Test the transport headers by sending a content description
            request.setContentDescription(SoapMessageProvider.XML_REQUEST);
            
            // Dispatch
            TestLogger.logger.debug(">> Invoking SourceMessageProviderDispatch");
        	SOAPMessage response = dispatch.invoke(request);

            // Check for valid content description
            assert(response.getContentDescription() != null);
            assert(response.getContentDescription().equals(SoapMessageProvider.XML_RESPONSE));
            
            // Check assertions and get the data element
            SOAPElement dataElement = assertResponseXML(response, SoapMessageProvider.XML_RESPONSE);
            
            assertTrue(countAttachments(response) == 0);
            
            // Print out the response
            TestLogger.logger.debug(">> Response [" + response.toString() + "]");
            response.writeTo(System.out);
        	
        }catch(Exception e){
        	e.printStackTrace();
            fail("Caught exception " + e);
        }
        
    }
    
    /**
     * Sends an SOAPMessage containing only xml data to the web service.  
     * Receives a response containing an empty body
     */
    public void testProviderSourceXMLEmptyBody(){
        try{       
            // Create the dispatch
            Dispatch<SOAPMessage> dispatch = createDispatch();
            
            // Create the SOAPMessage
            String msg = reqMsgStart + EMPTYBODY_INVOKE + reqMsgEnd;
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage request = factory.createMessage(null, 
                    new ByteArrayInputStream(msg.getBytes()));
            
            // Test the transport headers by sending a content description
            request.setContentDescription(SoapMessageProvider.XML_EMPTYBODY_REQUEST);
            
            // Dispatch
            TestLogger.logger.debug(">> Invoking SourceMessageProviderDispatch");
            SOAPMessage response = dispatch.invoke(request);
            
            // Check assertions
            assertTrue(response !=null);
            assertTrue(response.getSOAPBody() != null);
            assertTrue(response.getSOAPBody().getFirstChild() == null);  // There should be nothing in the body
            
            assertTrue(countAttachments(response) == 0);
            
            // Print out the response
            TestLogger.logger.debug(">> Response [" + response.toString() + "]");
            response.writeTo(System.out);
            
        }catch(Exception e){
            e.printStackTrace();
            fail("Caught exception " + e);
        }
        
    }
    
    /**
     * Sends an SOAPMessage containing only xml data 
     * Provider will throw a Fault
     */
    public void testProviderSOAPFault() throws Exception {
             
            // Create the dispatch
            Dispatch<SOAPMessage> dispatch = createDispatch();
            
            // Create the SOAPMessage
            String msg = reqMsgStart + XML_FAULT_INVOKE + reqMsgEnd;
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage request = factory.createMessage(null, 
                    new ByteArrayInputStream(msg.getBytes()));
            
            // Test the transport headers by sending a content description
            request.setContentDescription(SoapMessageProvider.XML_FAULT_REQUEST);
            
            try {
                // Dispatch
                TestLogger.logger.debug(">> Invoking SourceMessageProviderDispatch");
                SOAPMessage response = dispatch.invoke(request);
                assertTrue("Expected failure", false);
            } catch (SOAPFaultException e) {
                // Okay
                SOAPFault fault = e.getFault();
                assertTrue(fault != null);
                assertTrue(fault.getFaultString().equals("sample fault"));
                QName expectedFaultCode = new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Client"); 
                assertTrue(fault.getFaultCodeAsQName().equals(expectedFaultCode));
                assertTrue(fault.getDetail() != null);
                DetailEntry de = (DetailEntry) fault.getDetail().getDetailEntries().next();
                assertTrue(de != null);
                assertTrue(de.getLocalName().equals("detailEntry"));
                assertTrue(de.getValue().equals("sample detail"));
                assertTrue(fault.getFaultActor().equals("sample actor"));
            }    
    }
    
    /**
     * Sends an SOAPMessage containing only xml data 
     * Provider will throw a generic WebServicesException
     */
    public void testProviderWebServiceException() throws Exception {
             
            // Create the dispatch
            Dispatch<SOAPMessage> dispatch = createDispatch();
            
            // Create the SOAPMessage
            String msg = reqMsgStart + XML_WSE_INVOKE + reqMsgEnd;
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage request = factory.createMessage(null, 
                    new ByteArrayInputStream(msg.getBytes()));
            
            // Test the transport headers by sending a content description
            request.setContentDescription(SoapMessageProvider.XML_WSE_REQUEST);
            
            try {
                // Dispatch
                TestLogger.logger.debug(">> Invoking SourceMessageProviderDispatch");
                SOAPMessage response = dispatch.invoke(request);
                assertTrue("Expected failure", false);
            } catch (SOAPFaultException e) {
                // Okay...SOAPFaultException should be thrown
                SOAPFault fault = e.getFault();
                assertTrue(fault != null);
                assertTrue(fault.getFaultString().equals("A WSE was thrown"));
            }    
    }
    
    /**
     * Sends an SOAPMessage containing xml data and raw attachments to the web service.  
     * Receives a response containing xml data and the same raw attachments.
     */
    
    public void testProviderSourceRawAttachment(){
        // Raw Attachments are attachments that are not referenced in the xml with MTOM or SWARef.
        // Currently there is no support in Axis 2 for these kinds of attachments.
        // The belief is that most customers will use MTOM.  Some legacy customers will use SWARef.
        // Raw Attachments may be so old that no customers need this behavior.
        try{       
            // Create the dispatch
            Dispatch<SOAPMessage> dispatch = createDispatch();
            
            // Create the SOAPMessage
            String msg = reqMsgStart + ATTACHMENT_INVOKE + reqMsgEnd;
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage request = factory.createMessage(null, 
                    new ByteArrayInputStream(msg.getBytes()));
            
            // Add the Attachment
            AttachmentPart ap = request.createAttachmentPart(SoapMessageProvider.TEXT_XML_ATTACHMENT, "text/xml");
            ap.setContentId(SoapMessageProvider.ID);
            request.addAttachmentPart(ap);

            TestLogger.logger.debug("Request Message:");
            request.writeTo(System.out);
            
            // Dispatch
            TestLogger.logger.debug(">> Invoking SourceMessageProviderDispatch");
            SOAPMessage response = dispatch.invoke(request);

            // Check assertions and get the data element
            SOAPElement dataElement = assertResponseXML(response, SoapMessageProvider.XML_ATTACHMENT_RESPONSE);
            assertTrue(countAttachments(response) == 1);
            
            // Get the Attachment
            AttachmentPart attachmentPart = (AttachmentPart) response.getAttachments().next();
            
            // Check the attachment
            StreamSource contentSS = (StreamSource) attachmentPart.getContent();
            String content = SoapMessageProvider.getAsString(contentSS);
            assertTrue(content != null);
            assertTrue(content.contains(SoapMessageProvider.TEXT_XML_ATTACHMENT));
            
            // Print out the response
            TestLogger.logger.debug(">> Response [" + response.toString() + "]");
            response.writeTo(System.out);
            
        }catch(Exception e){
            e.printStackTrace();
            fail("Caught exception " + e);
        }
        
    }
    
    /**
     * Sends an SOAPMessage containing xml data and mtom attachment.  
     * Receives a response containing xml data and the mtom attachment.
     */
    public void testProviderSourceMTOM(){
        try{       
            // Create the dispatch
            Dispatch<SOAPMessage> dispatch = createDispatch();
            
            // MTOM should be automatically detected.  There is no need to set it
            //Binding binding = dispatch.getBinding();
            //SOAPBinding soapBinding = (SOAPBinding) binding;
            //soapBinding.setMTOMEnabled(true);
            
            // Create the SOAPMessage
            String msg = reqMsgStart + MTOM_INVOKE + reqMsgEnd;
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage request = factory.createMessage(null, 
                    new ByteArrayInputStream(msg.getBytes()));
            
            // Add the Attachment
            AttachmentPart ap = request.createAttachmentPart(SoapMessageProvider.TEXT_XML_ATTACHMENT, "text/xml");
            ap.setContentId(SoapMessageProvider.ID);
            request.addAttachmentPart(ap);

            TestLogger.logger.debug("Request Message:");
            request.writeTo(System.out);
            
            // Dispatch
            TestLogger.logger.debug(">> Invoking SourceMessageProviderDispatch");
            SOAPMessage response = dispatch.invoke(request);

            // Check assertions and get the data element
            SOAPElement dataElement = assertResponseXML(response, SoapMessageProvider.XML_MTOM_RESPONSE);
            assertTrue(countAttachments(response) == 1);
            
            // Get the Attachment
            AttachmentPart attachmentPart = (AttachmentPart) response.getAttachments().next();
            
            // Check the attachment
            StreamSource contentSS = (StreamSource) attachmentPart.getContent();
            String content = SoapMessageProvider.getAsString(contentSS);
            assertTrue(content != null);
            assertTrue(content.contains(SoapMessageProvider.TEXT_XML_ATTACHMENT));
            
            // Print out the response
            TestLogger.logger.debug(">> Response [" + response.toString() + "]");
            response.writeTo(System.out);
            
        }catch(Exception e){
            e.printStackTrace();
            fail("Caught exception " + e);
        }
        
    }
    
    /**
     * Sends an SOAPMessage containing xml data and a swaref attachment to the web service.  
     * Receives a response containing xml data and the swaref attachment attachment.
     */
    public void testProviderSourceSWARef(){
        try{       
            // Create the dispatch
            Dispatch<SOAPMessage> dispatch = createDispatch();
            
            // Create the SOAPMessage
            String msg = reqMsgStart + SWAREF_INVOKE + reqMsgEnd;
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage request = factory.createMessage(null, 
                    new ByteArrayInputStream(msg.getBytes()));
            
            // Add the Attachment
            AttachmentPart ap = request.createAttachmentPart(SoapMessageProvider.TEXT_XML_ATTACHMENT, "text/xml");
            ap.setContentId(SoapMessageProvider.ID);
            request.addAttachmentPart(ap);

            TestLogger.logger.debug("Request Message:");
            request.writeTo(System.out);
            
            // Dispatch
            TestLogger.logger.debug(">> Invoking SourceMessageProviderDispatch");
            SOAPMessage response = dispatch.invoke(request);

            // Check assertions and get the data element
            SOAPElement dataElement = assertResponseXML(response, SoapMessageProvider.XML_SWAREF_RESPONSE);
            assertTrue(countAttachments(response) == 1);
            
            // Get the Attachment
            AttachmentPart attachmentPart = (AttachmentPart) response.getAttachments().next();
            
            // Check the attachment
            StreamSource contentSS = (StreamSource) attachmentPart.getContent();
            String content = SoapMessageProvider.getAsString(contentSS);
            assertTrue(content != null);
            assertTrue(content.contains(SoapMessageProvider.TEXT_XML_ATTACHMENT));
            assert(attachmentPart.getContentId().equals(SoapMessageProvider.ID));
            
            // Print out the response
            TestLogger.logger.debug(">> Response [" + response.toString() + "]");
            response.writeTo(System.out);
            
        }catch(Exception e){
            e.printStackTrace();
            fail("Caught exception " + e);
        }
        
    }
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
