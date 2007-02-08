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
package org.apache.axis2.jaxws.client;

import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service.Mode;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.BindingProvider;
import org.apache.axis2.jaxws.client.soapaction.BookStoreService;
import org.apache.axis2.jaxws.client.soapaction.GetPriceResponseType;
import org.apache.axis2.jaxws.client.soapaction.GetPriceType;
import org.apache.axis2.jaxws.client.soapaction.ObjectFactory;

/**
 * A suite of SOAPAction related tests for the dispatch client 
 */
public class DispatchSoapActionTests extends TestCase {
    
    private static final String targetNamespace = "http://jaxws.axis2.apache.org/client/soapaction";
    private static final String portName = "BookStorePort";
        
    /**
     * Invoke an operation this is defined in the WSDL as having a SOAPAction.
     * Since this is a Dispatch client, we'll need to specify that SOAPAction
     * ourselves for the invoke.
     */
    public void testSendRequestWithSoapAction() throws Exception {
        System.out.println("----------------------------------");
        System.out.println("test: " + getName());
        
        BookStoreService service = new BookStoreService();
        
        JAXBContext ctx = JAXBContext.newInstance("org.apache.axis2.jaxws.client.soapaction");
        Dispatch dispatch = service.createDispatch(new QName(targetNamespace, portName), 
                ctx, Mode.PAYLOAD);
        
        Map<String, Object> requestCtx = dispatch.getRequestContext();
        requestCtx.put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        requestCtx.put(BindingProvider.SOAPACTION_URI_PROPERTY, "http://jaxws.axis2.apache.org/client/soapaction/getPrice");
        
        ObjectFactory of = new ObjectFactory();
        GetPriceType gpt = of.createGetPriceType();
        gpt.setItem("TEST");
        
        // The element that is sent should be <getPriceWithAction>
        // so it will resolve to the getPriceWithAction operation
        // defined in the WSDL.
        JAXBElement<GetPriceType> getPrice = of.createGetPriceWithAction(gpt);
        JAXBElement<GetPriceResponseType> getPriceResponse = (JAXBElement<GetPriceResponseType>) dispatch.invoke(getPrice);
        
        GetPriceResponseType value = getPriceResponse.getValue();
        assertNotNull("The response was null", value);
        
        float price = value.getPrice();
        System.out.println("return value [" + price + "]");
        //assertTrue("The return value was invalid", price > 0);
    }

}
