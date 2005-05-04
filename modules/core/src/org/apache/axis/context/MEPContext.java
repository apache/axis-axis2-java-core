package org.apache.axis.context;

import java.util.List;

import org.apache.axis.engine.AxisFault;

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
 *
 * 
 */

public interface MEPContext {
    public String getMepId();
    public void setMepId(String mepId);
    public boolean isComplete();

  
//    public MessageContext getInMessageContext(String messageID);
//    public List getInMessageContexts();
//
//    public MessageContext getOutMessageContext(String messageID);
//    public List getOutMessageContexts();
    
//    public void addInMessageContext(MessageContext msgctx);
//    public void addOutMessageContext(MessageContext msgctx);
    
//    public boolean isComplete();    
    
    public MessageContext getMessageContext(String msgID) throws AxisFault;
    
    public void addMessageContext(MessageContext msgContext) throws AxisFault;
    
    public List getAllMessageContexts();
}

