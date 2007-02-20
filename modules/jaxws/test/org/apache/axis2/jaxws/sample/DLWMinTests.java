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
package org.apache.axis2.jaxws.sample;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.apache.axis2.jaxws.sample.dlwmin.sei.Greeter;

import junit.framework.TestCase;

public class DLWMinTests extends TestCase {

    private static final String NAMESPACE = "http://apache.org/axis2/jaxws/sample/dlwmin";
    private static final QName QNAME_SERVICE = new QName(
            NAMESPACE, "GreeterService");
    private static final QName QNAME_PORT = new QName(
            NAMESPACE, "GreeterPort");
    private static final String URL_ENDPOINT = "http://localhost:8080/axis2/services/GreeterService";

    /**
     * Test that we can call the simple greetMe method 
     * with style doc/lit wrapped without the presence of wrapper classes.
     */
    public void testGreetMe() {
        Service service = Service.create(QNAME_SERVICE);
        Greeter proxy = service.getPort(QNAME_PORT, Greeter.class);
        BindingProvider p = (BindingProvider) proxy;
        p.getRequestContext().put(
                BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        p.getRequestContext().put(
                BindingProvider.SOAPACTION_URI_PROPERTY, "greetMe");
        p.getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY, URL_ENDPOINT);

        String me = "Scheu";
        String response = proxy.greetMe(me);
        assertTrue("Hello Scheu".equals(response));
    }
}
