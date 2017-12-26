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

package org.apache.axis2.jaxws.xmlhttp;

import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.http.HTTPBinding;

import static com.google.common.truth.Truth.assertAbout;
import static org.apache.axiom.truth.xml.XMLTruth.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class DispatchXPayloadSourceTests {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo");

    private QName SERVICE_NAME  = new QName("http://ws.apache.org/axis2", "XPayloadSourceProvider");
    private QName PORT_NAME  = new QName("http://ws.apache.org/axis2", "XPayloadSourceProviderPort");
 
    private static String XML_TEXT = "<p:echo xmlns:p=\"http://sample\">hello world</p:echo>";
    private static String XML_TEXT_NPE = "<p:echo xmlns:p=\"http://sample\">NPE</p:echo>";
    
    public Dispatch<Source> getDispatch() {
       Service service = Service.create(SERVICE_NAME);
       service.addPort(PORT_NAME, HTTPBinding.HTTP_BINDING, "http://localhost:" + server.getPort() + "/axis2/services/XMessageSourceProvider.XMessageSourceProviderPort");
       Dispatch<Source> dispatch = service.createDispatch(PORT_NAME, Source.class, Service.Mode.PAYLOAD);
       return dispatch;
    }
    
    /**
     * Simple XML/HTTP Payload Test
     * @throws Exception
     */
    @Test
    public void testSimple() throws Exception {
        Dispatch<Source> dispatch = getDispatch();
        String request = XML_TEXT;
        ByteArrayInputStream stream = new ByteArrayInputStream(request.getBytes());
        Source inSource = new StreamSource((InputStream) stream);
        
        Source outSource = dispatch.invoke(inSource);
        
        // Prepare the response content for checking
        assertAbout(xml()).that(outSource).hasSameContentAs(XML_TEXT);
        
        // Try a second time to verify
        stream = new ByteArrayInputStream(request.getBytes());
        inSource = new StreamSource((InputStream) stream);
        
        outSource = dispatch.invoke(inSource);
        
        // Prepare the response content for checking
        assertAbout(xml()).that(outSource).hasSameContentAs(XML_TEXT);
    }
    
   
}

