/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.engine;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.Call;
import org.apache.axis.impl.transport.http.SimpleHTTPReceiver;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.SOAPEnvelope;

import java.net.URL;


public class EngineAndDeploymentTest extends AbstractTestCase{
    private EngineRegistry er;
    private SimpleHTTPReceiver sas;
    /**
     * @param testName
     */
    public EngineAndDeploymentTest(String testName) throws Exception{
        super(testName);
        er = EngineRegistryFactory.createEngineRegistry("target/test-resources/deployment");
        
    }

    public void testRunServiceWithDeployment() throws Exception{
            OMFactory fac = OMFactory.newInstance();
            
            SOAPEnvelope envelope = fac.getDefaultEnvelope(); 

            OMNamespace omNs = fac.createOMNamespace("http://localhost/my","my");
            OMElement method =  fac.createOMElement("echo",omNs) ;
            OMElement value =  fac.createOMElement("myValue",omNs) ;
            value.setValue("Isaac Assimov, the foundation Sega");
            method.addChild(value);
            
            envelope.getBody().addChild(method);
            
            Call call = new Call();
            URL url = new URL("http","127.0.0.1",EngineUtils.TESTING_PORT,"/axis/services/EchoXMLService");
            call.setTo(new EndpointReference(AddressingConstants.WSA_TO,url.toString()));
            OMElement omele = call.sendReceive(envelope);
            assertNotNull(omele);
    }
    protected void setUp() throws Exception {
        sas = EngineUtils.startServer(er);
    }

    protected void tearDown() throws Exception {
        EngineUtils.stopServer();  
    }

}
