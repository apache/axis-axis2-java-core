/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.axis.samples.userguide.sample2.server;

import java.io.FileReader;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.Call;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.engine.Echo;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.EngineUtils;
import org.apache.axis.impl.description.AxisService;
import org.apache.axis.impl.description.SimpleAxisOperationImpl;
import org.apache.axis.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.impl.providers.SimpleJavaProvider;
import org.apache.axis.impl.transport.http.SimpleHTTPReceiver;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author chathura@opensource.lk
 * 
 */
public class SampleEnvironmentCreator extends  AbstractTestCase{
	
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("","EchoXMLService");
    private QName operationName1 = new QName("http://localhost/my","echoInt");
    private QName operationName2 = new QName("http://localhost/my","echoString");
    private QName transportName = new QName("http://localhost/my","NullTransport");

    private EngineRegistry engineRegistry;
    private MessageContext mc;
    private Thread thisThread = null;
    private SimpleHTTPReceiver sas;
    
    public SampleEnvironmentCreator(){
        super(SampleEnvironmentCreator.class.getName());
//        try {
//			this.setUp();
//			this.testInt();
//			this.tearDown();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        
    }

    public SampleEnvironmentCreator(String testName) {
        super(testName);
    }
    
    public void setUp() throws Exception {
        AxisGlobal global = new AxisGlobal();
        engineRegistry = new org.apache.axis.impl.engine.EngineRegistryImpl(global);
        
        AxisService service = new AxisService(serviceName);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.setServiceClass(Echo.class);
        service.setProvider(new SimpleJavaProvider());

        AxisOperation operation1 = new SimpleAxisOperationImpl(operationName1);
        service.addOperation(operation1);
        
        AxisOperation operation2 = new SimpleAxisOperationImpl(operationName2);
        service.addOperation(operation2);

        EngineUtils.createExecutionChains(service);
        engineRegistry.addService(service);
        
        sas = EngineUtils.startServer(engineRegistry);        
    }
    
    
    protected void tearDown() throws Exception {
        EngineUtils.stopServer();    
        Thread.sleep(1000);
}


public void testInt() throws Exception{
    try{
   
        Call call = new Call();
        URL url = new URL("http","127.0.0.1",EngineUtils.TESTING_PORT,"/axis/services/EchoXMLService");
        EndpointReference epr = new EndpointReference(AddressingConstants.WSA_TO, url.toString());
        call.setTo(epr);
        SOAPEnvelope reply = call.sendReceive(this.getechoIntEnvelope());
        reply.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out), false);
        
    }catch(Exception e){
        e.printStackTrace();
        tearDown();
        throw e;
    }    
}
public void testString() throws Exception{
//    try{
//        OMFactory fac = OMFactory.newInstance();
//
//        OMNamespace omNs = fac.createOMNamespace("http://localhost/my","my");
//        OMElement method =  fac.createOMElement("echoOMElement",omNs) ;
//        OMElement value =  fac.createOMElement("myValue",omNs) ;
//        value.setValue("Isaac Assimov, the foundation Sega");
//        method.addChild(value);
//        
//        Call call = new Call();
//        URL url = new URL("http","127.0.0.1",EngineUtils.TESTING_PORT,"/axis/services/EchoXMLService");
//        
//        CallBack callback = new CallBack() {
//            public void doWork(OMElement ele) {
//                System.out.print("got the result = " + ele +" :)");
//
//            }
//            public void reportError(Exception e) {
//                log.info("reporting error from callback !");
//                e.printStackTrace();
//            }
//        };
//        
//        call.asyncCall(method,url,callback);
//        log.info("send the reqest");
//        
//        Thread.sleep(1000);
//    }catch(Exception e){
//        e.printStackTrace();
//        tearDown();
//        throw e;
//    }    
}

 public static void main(String[] args) {
 	new SampleEnvironmentCreator();
	
}

private SOAPEnvelope getechoIntEnvelope() throws Exception {

    SOAPEnvelope envelope = new StAXSOAPModelBuilder(XMLInputFactory.newInstance().createXMLStreamReader(
            new FileReader(getTestResourceFile("echo/echoInt.xml")))).getSOAPEnvelope();
    envelope.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out), false);
    return envelope;
}
	
    
}
