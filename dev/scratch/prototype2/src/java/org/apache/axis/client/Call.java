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
package org.apache.axis.client;

import java.net.URL;

import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.impl.engine.GlobalImpl;
import org.apache.axis.impl.registry.EngineRegistryImpl;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMEnvelope;
import org.apache.axis.registry.EngineRegistry;

/**
 * This is conveneice API for the User who do not need to see the complexity of the 
 * Engine.  
 * @author Srinath Perera (hemapani@opensource.lk)
 */
public class Call {
    private EngineRegistry registry;
    public Call(){
        this.registry = new EngineRegistryImpl(new GlobalImpl());
    }
    //TODO this a a MOCK call things are subjected to be decided 
    
    public OMElement syncCall(OMElement in,URL url) throws AxisFault{
        OMEnvelope env = null;
        
        env.getBody().addChild(in);
        AxisEngine engine = new AxisEngine(registry);
        MessageContext msgctx = new MessageContext(registry);
        msgctx.setEnvelope(env);
        engine.send(msgctx);
        
        
        return null;
    }    
    
}
