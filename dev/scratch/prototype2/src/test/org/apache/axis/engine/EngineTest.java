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

package org.apache.axis.engine;

import junit.framework.TestCase;
import org.apache.axis.context.MessageContext;
import org.apache.axis.impl.description.AxisService;

import javax.xml.namespace.QName;
import java.io.OutputStream;

public class EngineTest extends TestCase{
    private QName serviceName = new QName("","EchoService");
    private QName operationName = new QName("","echoVoid");
    private QName transportName = new QName("","NullTransport");
    private EngineRegistry engineRegistry;
    private MessageContext mc;
    
    public EngineTest() {
        super();
    }
    
    public EngineTest(String arg0) {
        super(arg0);
    }
    protected void setUp() throws Exception {
        engineRegistry = EngineUtils.createMockRegistry(serviceName,operationName,transportName);
        mc = new MessageContext(engineRegistry);
        AxisService service = engineRegistry.getService(serviceName);
        mc.setProperty(MessageContext.REQUEST_URL,"/axis/services/EchoService");
        mc.setOperation(service.getOperation(operationName));
        
        OutputStream out = System.out;
        mc.setProperty(MessageContext.TRANSPORT_TYPE,
                                TransportSenderLocator.TRANSPORT_TCP);
        mc.setProperty(MessageContext.TRANSPORT_DATA,out);
        out.flush();
    }

    public void testSend()throws Exception{
        AxisEngine engine = new AxisEngine(engineRegistry);
        engine.send(mc);
    }
    public void testReceive()throws Exception{
        AxisEngine engine = new AxisEngine(engineRegistry);
        engine.receive(mc);
    }
    protected void tearDown() throws Exception {
    }

}
