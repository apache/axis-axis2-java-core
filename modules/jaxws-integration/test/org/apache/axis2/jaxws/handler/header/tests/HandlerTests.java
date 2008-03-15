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

package org.apache.axis2.jaxws.handler.header.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.framework.AbstractTestCase;

import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

public class HandlerTests extends AbstractTestCase {
    public static Test suite() {
        return getTestSetup(new TestSuite(HandlerTests.class));
    }
    
    public void testHandler_getHeader_invocation(){
        System.out.println("----------------------------------");
        System.out.println("test: " + getName());
        Object res = null;
        //Add myHeader to SOAPMessage that will be injected by handler.getHeader().
        String soapMessage = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:demo=\"http://demo/\"><soap:Header> <demo:myheader soap:mustUnderstand=\"1\"/></soap:Header><soap:Body><demo:echo><arg0>test</arg0></demo:echo></soap:Body></soap:Envelope>";
        String url = "http://localhost:6060/axis2/services/DemoService.DemoServicePort";
        QName name = new QName("http://demo/", "DemoService");
        QName portName = new QName("http://demo/", "DemoServicePort");
        try{
                //Create Service
                Service s = Service.create(name);
                assertNotNull(s);
                //add port
                s.addPort(portName, null, url);
                
                //Create Dispatch
                Dispatch<String> dispatch = s.createDispatch(portName, String.class, Service.Mode.MESSAGE);
                assertNotNull(dispatch);
                res = dispatch.invoke(soapMessage);
                assertNotNull(res);
                System.out.println("----------------------------------");
        }catch(Exception e){
                e.printStackTrace();
                fail();
        }       
    }
    
    public void test_MU_Failure(){
        System.out.println("----------------------------------");
        System.out.println("test: " + getName());
        Object res = null;
        //Add bad header to SOAPMessage, we expect MU to fail 
        String soapMessage = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:demo=\"http://demo/\"><soap:Header> <demo:badHeader soap:mustUnderstand=\"1\"/></soap:Header><soap:Body><demo:echo><arg0>test</arg0></demo:echo></soap:Body></soap:Envelope>";
        String url = "http://localhost:6060/axis2/services/DemoService.DemoServicePort";
        QName name = new QName("http://demo/", "DemoService");
        QName portName = new QName("http://demo/", "DemoServicePort");
        try{
                //Create Service
                Service s = Service.create(name);
                assertNotNull(s);
                //add port
                s.addPort(portName, null, url);                
                //Create Dispatch
                Dispatch<String> dispatch = s.createDispatch(portName, String.class, Service.Mode.MESSAGE);
                assertNotNull(dispatch);
                res = dispatch.invoke(soapMessage);
                System.out.println("Expecting SOAPFaultException with MustUnderstand failed");
                fail();
                System.out.println("----------------------------------");
        }catch(SOAPFaultException e){       	
            e.printStackTrace();
            System.out.println("MustUnderstand failed as exptected");
            System.out.println("----------------------------------");
                
        }       
    }
}
