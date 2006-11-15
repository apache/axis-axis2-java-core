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

import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

@WebServiceProvider()
@ServiceMode(value=Service.Mode.MESSAGE)
public class SoapMessageProvider implements Provider<SOAPMessage> {
    String responseAsString = new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header/><soapenv:Body><ns2:ReturnType xmlns:ns2=\"http://test\"><return_str>some response</return_str></ns2:ReturnType></soapenv:Body></soapenv:Envelope>");
    public SOAPMessage invoke(SOAPMessage soapMessage) {
    	System.out.println(">> SoapMessageProvider: Request received.");
    	
    	try{
    	    // Look at the incoming request message
            System.out.println(">> Request on Server:");
            soapMessage.writeTo(System.out);
            System.out.println("\n");
            
            // Build the outgoing response message
            SOAPMessage message = null;
            MessageFactory factory = MessageFactory.newInstance();
            message = factory.createMessage(null, new ByteArrayInputStream(responseAsString.getBytes()));

            System.out.println(">> Response being sent by Server:");
            message.writeTo(System.out);
            System.out.println("\n");
            return message;
    	}catch(Exception e){
            System.out.println("***ERROR: In SoapMessageProvider.invoke: Caught exception " + e);
    		e.printStackTrace();
    	}
    	return null;
    }
}
