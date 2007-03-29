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

import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

public class StringMessageProviderTests extends ProviderTestCase {

    String endpointUrl = "http://localhost:8080/axis2/services/StringMessageProviderService";
    String xmlString = "<test>test input</test>";
    private QName serviceName = new QName("http://ws.apache.org/axis2", "StringMessageProviderService");

    protected void setUp() throws Exception {
            super.setUp();
    }

    protected void tearDown() throws Exception {
            super.tearDown();
    }

    public StringMessageProviderTests(String name) {
        super(name);
    }
    
    public void testProviderString() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, endpointUrl);
        
        Dispatch<String> dispatch = svc
                .createDispatch(portName, String.class, Service.Mode.PAYLOAD);
        
        System.out.println(">> Invoking Dispatch<String> StringMessageProviderService");
        String retVal = dispatch.invoke(xmlString);
        System.out.println(">> Response [" + retVal + "]");
    }
}
