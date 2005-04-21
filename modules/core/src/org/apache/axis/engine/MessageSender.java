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

import java.io.File;
import java.io.InputStream;

import org.apache.axis.addressing.om.MessageInformationHeadersCollection;
import org.apache.axis.context.EngineContext;
import org.apache.axis.context.MEPContext;
import org.apache.axis.context.MessageContext;

/**
 * Class Sender
 */
public class MessageSender {
    private EngineContext engineContext;
    private MessageInformationHeadersCollection messageInfoHeaders;
    private MEPContext mepContext;
    
    
    public MessageSender(EngineContext engineContext){
        this.engineContext = engineContext;
    }              
    
    public MessageSender(InputStream in){
        //TODO create the Engine Context
    }
    
    public MessageSender(File confFile){
        //TODO create the Engine Context
    }
    
    /**
     * Method send
     *
     * @param msgCtx
     * @throws AxisFault
     */
    public void send(MessageContext msgCtx) throws AxisFault {
        AxisEngine engine = new AxisEngine();
        engine.send(msgCtx);
    }
}
