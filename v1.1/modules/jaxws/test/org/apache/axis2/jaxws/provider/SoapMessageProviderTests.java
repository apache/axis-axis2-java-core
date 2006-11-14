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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public class SoapMessageProviderTests extends ProviderTestCase {

    private String endpointUrl = "http://localhost:8080/axis2/services/SoapMessageProviderService";
    private QName serviceName = new QName("http://ws.apache.org/axis2", "SoapMessageProviderService");
    private String xmlDir = "xml";
    private String reqMsg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns2:invoke xmlns:ns2=\"http://org.test.soapmessage\"><invoke_str>some request</invoke_str></ns2:invoke></soap:Body></soap:Envelope>";


    protected void setUp() throws Exception {
            super.setUp();
    }

    protected void tearDown() throws Exception {
            super.tearDown();
    }

    public SoapMessageProviderTests(String name) {
        super(name);
    }
    
    public void testProviderSource(){
        try{
//        	String resourceDir = new File(providerResourceDir, xmlDir).getAbsolutePath();
//        	String fileName = resourceDir+File.separator+"web.xml";
//        	
//        	File file = new File(fileName);
//        	InputStream inputStream = new FileInputStream(file);
//        	StreamSource xmlStreamSource = new StreamSource(inputStream);
//        	
        	Service svc = Service.create(serviceName);
        	svc.addPort(portName,null, endpointUrl);
        	Dispatch<SOAPMessage> dispatch = svc.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
        	System.out.println(">> Invoking SourceMessageProviderDispatch");
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage outboundMessage = factory.createMessage(null, 
                    new ByteArrayInputStream(reqMsg.getBytes()));
        	SOAPMessage response = dispatch.invoke(outboundMessage);

        	System.out.println(">> Response [" + response.toString() + "]");
            response.writeTo(System.out);
        	
        }catch(Exception e){
        	e.printStackTrace();
            fail("Caught exception " + e);
        }
        
    }
}
