/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.axis2.jaxws.dispatch.server;

import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPBinding;

/**
 * A Provider&lt;String&gt; implementation used to test sending and 
 * receiving SOAP 1.2 messages.
 */
@WebServiceProvider()
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class SOAP12Provider implements Provider<String> {

    private static final String sampleResponse = 
        "<test:echoStringResponse xmlns:test=\"http://org/apache/axis2/jaxws/test/SOAP12\">" +
        "<test:output>SAMPLE REQUEST MESSAGE</test:output>" +
        "</test:echoStringResponse>";
    
    public String invoke(String obj) {
        System.out.println(">> request received");
        System.out.println(obj);
        return sampleResponse;
    }

}
