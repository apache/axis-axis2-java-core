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
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.impl.engine.EngineRegistryImpl;
import org.apache.axis.om.OMFactory;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class MessageContextTest extends AbstractTestCase{
    public MessageContextTest(String testName) {
        super(testName);
    }
    public void testMesssageContext() throws AxisFault{
        EngineRegistry er = new EngineRegistryImpl(new AxisGlobal());
        MessageContext msgctx = new MessageContext(er);
        
        msgctx.setEnvelope(OMFactory.newInstance().getDefaultEnvelope());
        assertNotNull(msgctx.getEnvelope());
        
        msgctx.setFaultTo(null);
        assertNull(msgctx.getFaultTo());
        
        msgctx.setFrom(null);
        assertNull(msgctx.getFrom());

        msgctx.setRelatesTo(null);
        assertNull(msgctx.getRelatesTo());

        msgctx.setReplyTo(null);
        assertNull(msgctx.getReplyTo());
        
        msgctx.setMessageID(null);
        assertNull(msgctx.getMessageID());
    }
}
