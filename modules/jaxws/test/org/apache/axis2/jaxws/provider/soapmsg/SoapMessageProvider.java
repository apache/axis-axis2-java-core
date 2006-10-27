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
package org.apache.axis2.jaxws.provider.soapmsg;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;

@WebServiceProvider()
@ServiceMode(value=Service.Mode.MESSAGE)
public class SoapMessageProvider implements Provider<SOAPMessage> {
      
    String responseMsgStart = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header/><soapenv:Body><ns2:ReturnType xmlns:ns2=\"http://test\"><return_str>";
    String responseMsgEnd = "</return_str></ns2:ReturnType></soapenv:Body></soapenv:Envelope>";

    // Requests and Response values of invoke_str and return_str
    // These constants are referenced by the SoapMessageProviderTest and SoapMessageProvider
    public static String RESPONSE_NAME = "ReturnType";
    public static String RESPONSE_DATA_NAME = "return_str";
    public static String REQUEST_NAME = "invoke";
    public static String REQUEST_DATA_NAME = "invoke_str";
    
    public static String XML_REQUEST              = "xml request";
    public static String XML_RESPONSE             = "xml response";
    public static String XML_ATTACHMENT_REQUEST   = "xml and attachment request";
    public static String XML_ATTACHMENT_RESPONSE  = "xml and attachment response";
    
    public static String TEXT_XML_ATTACHMENT = "<myAttachment>Hello World</myAttachment>";
    public static String ID = "cid123";
    
    public SOAPMessage invoke(SOAPMessage soapMessage) {
    	System.out.println(">> SoapMessageProvider: Request received.");
    	
    	try{
    	    // Look at the incoming request message
            System.out.println(">> Request on Server:");
            soapMessage.writeTo(System.out);
            System.out.println("\n");
            
            // Get the data element.  This performs basic assertions on the received message
            SOAPElement dataElement = assertRequestXML(soapMessage);
            
            // Use the data element text to determine the type of response to send
            SOAPMessage response = null;
            // TODO AXIS2 SAAJ should (but does not) support the getTextContent();
            // String text = dataElement.getTextContent();
            String text = dataElement.getValue();
            if (XML_REQUEST.equals(text)) {
                response = getXMLResponse(soapMessage, dataElement);
            } else if (XML_ATTACHMENT_REQUEST.equals(text)) {
                response = getXMLAttachmentResponse(soapMessage, dataElement);
            } else {
                // We should not get here
                assert(false);
            }
            
            // Write out the Message
            System.out.println(">> Response being sent by Server:");
            response.writeTo(System.out);
            System.out.println("\n");
            return response;
    	}catch(Exception e){
            System.out.println("***ERROR: In SoapMessageProvider.invoke: Caught exception " + e);
    		e.printStackTrace();
    	}
    	return null;
    }
    
    /**
     * Common assertion checking of the request
     * @param msg
     * @return SOAPElement representing the data element
     */
    private SOAPElement assertRequestXML(SOAPMessage msg) throws Exception {
        assert(msg != null);
        SOAPBody body = msg.getSOAPBody();
        assert(body != null);
        
        Node invokeElement = (Node) body.getFirstChild();
        assert(invokeElement instanceof SOAPElement);
        assert(SoapMessageProvider.RESPONSE_NAME.equals(invokeElement.getLocalName()));
        
        Node dataElement = (Node) invokeElement.getFirstChild();
        assert(dataElement instanceof SOAPElement);
        assert(SoapMessageProvider.RESPONSE_DATA_NAME.equals(dataElement.getLocalName()));
        
        String text = dataElement.getValue();
        assert(text != null);
        assert(text.length() > 0);
        
        return (SOAPElement) dataElement;
    }
    
    /**
     * Get the response for an XML only request
     * @param request
     * @param dataElement
     * @return SOAPMessage
     */
    private SOAPMessage getXMLResponse(SOAPMessage request, SOAPElement dataElement) throws Exception {
        SOAPMessage response;
        
        // Additional assertion checks
        assert(countAttachments(request) == 0);
        
        // Build the Response
        MessageFactory factory = MessageFactory.newInstance();
        String responseXML = responseMsgStart + XML_RESPONSE + responseMsgEnd;
        response = factory.createMessage(null, new ByteArrayInputStream(responseXML.getBytes()));
        
        return response;
    }
    
    /**
     * Get the response for an XML and an Attachment request
     * @param request
     * @param dataElement
     * @return SOAPMessage
     */
    private SOAPMessage getXMLAttachmentResponse(SOAPMessage request, SOAPElement dataElement) throws Exception {
        SOAPMessage response;
        
        // Additional assertion checks
        assert(countAttachments(request) == 1);
        AttachmentPart requestAP = (AttachmentPart) request.getAttachments().next();
        String content = (String) requestAP.getContent();
        assert(SoapMessageProvider.TEXT_XML_ATTACHMENT.equals(content));
        
        // Build the Response
        MessageFactory factory = MessageFactory.newInstance();
        String responseXML = responseMsgStart + XML_ATTACHMENT_RESPONSE + responseMsgEnd;
        response = factory.createMessage(null, new ByteArrayInputStream(responseXML.getBytes()));
        
        // Create and attach the attachment
        AttachmentPart ap = response.createAttachmentPart(SoapMessageProvider.TEXT_XML_ATTACHMENT, "text/xml");
        ap.setContentId(ID);
        response.addAttachmentPart(ap);
        
        return response;
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
