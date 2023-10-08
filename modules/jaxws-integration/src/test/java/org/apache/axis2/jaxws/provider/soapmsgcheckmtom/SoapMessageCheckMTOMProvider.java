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

package org.apache.axis2.jaxws.provider.soapmsgcheckmtom;

import org.apache.axis2.jaxws.TestLogger;

import javax.xml.namespace.QName;
import jakarta.xml.soap.AttachmentPart;
import jakarta.xml.soap.Detail;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.Name;
import jakarta.xml.soap.Node;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPBodyElement;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.Provider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.ServiceMode;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceProvider;
import jakarta.xml.ws.soap.SOAPBinding;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

@WebServiceProvider(serviceName="SoapMessageCheckMTOMProviderService",
		targetNamespace="http://soapmsgcheckmtom.provider.jaxws.axis2.apache.org",
		portName="SoapMessageCheckMTOMProviderPort")
@BindingType(SOAPBinding.SOAP11HTTP_BINDING)
@ServiceMode(value=Service.Mode.MESSAGE)
public class SoapMessageCheckMTOMProvider implements Provider<SOAPMessage> {
    
    
    /**
     * Very simple operation.
     * If there are no attachments, an exception is thrown.
     * Otherwise the message is echoed.
     */
    public SOAPMessage invoke(SOAPMessage soapMessage)  {
        System.out.println(">> SoapMessageCheckMTOMProvider: Request received.");


        int numAttachments = soapMessage.countAttachments();
        if (numAttachments == 0) {
            System.out.println(">> SoapMessageCheckMTOMProvider: No Attachments.");
            throw new WebServiceException("No Attachments are detected");
        }
        SOAPMessage response = null;
        try {
            MessageFactory factory = MessageFactory.newInstance();
            response = factory.createMessage();
            SOAPPart soapPart = response.getSOAPPart();
            SOAPBody soapBody = soapPart.getEnvelope().getBody();
            soapBody.addChildElement((SOAPBodyElement) 
                                     soapMessage.
                                     getSOAPBody().getChildElements().next());
            response.addAttachmentPart((AttachmentPart) soapMessage.getAttachments().next());
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        }
        System.out.println(">> SoapMessageCheckMTOMProvider: Returning.");
        return response;  // echo back the same message
    }
    
    /**
     * Count Attachments
     * @param msg
     * @return
     */
    private int countAttachments(SOAPMessage msg) {
        Iterator it = msg.getAttachments();
        int count = 0;
        if (it == null ) {
            return 0;
        }
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }
}
