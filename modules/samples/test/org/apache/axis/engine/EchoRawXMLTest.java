/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

//todo

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.AsyncResult;
import org.apache.axis.clientapi.Callback;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.integration.UtilServer;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.transport.http.SimpleHTTPServer;
import org.apache.axis.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EchoRawXMLTest extends TestCase {
    private EndpointReference targetEPR =
        new EndpointReference(
            AddressingConstants.WSA_TO,
            "http://127.0.0.1:"
                + (UtilServer.TESTING_PORT + 1)
                + "/axis/services/EchoXMLService/echoOMElement");
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoOMElement");
    private QName transportName = new QName("http://localhost/my", "NullTransport");

    private AxisConfiguration engineRegistry;
    private MessageContext mc;
    private Thread thisThread;
    private SimpleHTTPServer sas;
    private ServiceContext serviceContext;


    private boolean finish = false;

    public EchoRawXMLTest() {
        super(EchoRawXMLTest.class.getName());
    }

    public EchoRawXMLTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();
        ServiceDescription service =
            Utils.createSimpleService(
                serviceName,
                org.apache.axis.engine.Echo.class.getName(),
                operationName);
        UtilServer.deployService(service);
        serviceContext = UtilServer.getConfigurationContext().createServiceContext(service.getName());
        
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }

    private SOAPEnvelope createEnvelope(SOAPFactory fac) {
        SOAPEnvelope reqEnv = fac.getDefaultEnvelope();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(fac.createText(value, "Isaac Assimov, the foundation Sega"));
        method.addChild(value);
        reqEnv.getBody().addChild(method);
        return reqEnv;
    }

        public void testEchoXMLASync() throws Exception {
            SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
    
            SOAPEnvelope reqEnv = createEnvelope(fac);
    
            org.apache.axis.clientapi.Call call = new org.apache.axis.clientapi.Call();
    
            call.setTo(targetEPR);
            call.setTransportInfo(Constants.TRANSPORT_HTTP,Constants.TRANSPORT_HTTP, false);
    
            Callback callback = new Callback() {
                public void onComplete(AsyncResult result) {
                    try {
                        result.getResponseEnvelope().serializeWithCache(
                            XMLOutputFactory.newInstance().createXMLStreamWriter(System.out));
                    } catch (XMLStreamException e) {
                        reportError(e);
                    } finally {
                        finish = true;
                    }
                }
    
                public void reportError(Exception e) {
                    e.printStackTrace();
                    finish = true;
                }
            };
    
            call.invokeNonBlocking(operationName.getLocalPart(),reqEnv, callback);
            while (!finish) {
                Thread.sleep(1000);
            }
    
            log.info("send the reqest");
        }
    
        public void testEchoXMLSync() throws Exception {
            SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
    
            SOAPEnvelope reqEnv = createEnvelope(fac);
    
            org.apache.axis.clientapi.Call call = new org.apache.axis.clientapi.Call();
    
            call.setTo(targetEPR);
            call.setTransportInfo(Constants.TRANSPORT_HTTP,Constants.TRANSPORT_HTTP, false);
    
            SOAPEnvelope result = (SOAPEnvelope)call.invokeBlocking(operationName.getLocalPart(),reqEnv);
            result.serializeWithCache(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out));
        }
        

    public void testEchoXMLCompleteASync() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        SOAPEnvelope reqEnv = fac.getDefaultEnvelope();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.setText("Isaac Assimov, the foundation Sega");
        method.addChild(value);
        reqEnv.getBody().addChild(method);

        org.apache.axis.clientapi.Call call = new org.apache.axis.clientapi.Call();

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);
        Callback callback = new Callback() {
            public void onComplete(AsyncResult result) {
                try {
                    result.getResponseEnvelope().serialize(
                        XMLOutputFactory.newInstance().createXMLStreamWriter(System.out));
                } catch (XMLStreamException e) {
                    reportError(e);
                } finally {
                    finish = true;
                }
            }

            public void reportError(Exception e) {
                e.printStackTrace();
                finish = true;
            }
        };

        call.invokeNonBlocking(operationName.getLocalPart(), reqEnv, callback);
        while (!finish) {
            Thread.sleep(1000);
        }

        log.info("send the reqest");
    }

}
