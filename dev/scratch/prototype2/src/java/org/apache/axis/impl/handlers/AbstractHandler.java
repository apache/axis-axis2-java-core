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
package org.apache.axis.impl.handlers;

import org.apache.axis.context.MessageContext;
import org.apache.axis.description.HandlerMetaData;
import org.apache.axis.description.Parameter;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Handler;

import javax.xml.namespace.QName;

public abstract class AbstractHandler implements Handler {
    protected HandlerMetaData handlerDesc;
    
    public AbstractHandler(){
    }

    public QName getName() {
        return handlerDesc.getName();
    }

    public abstract void invoke(MessageContext msgContext) throws AxisFault;

    public void revoke(MessageContext msgContext) {
    }


 
    public void cleanup() throws AxisFault {
    }


    public Parameter getParameter(String name) {
        return handlerDesc.getParameter(name);
    }

    public void init(HandlerMetaData handlerdesc) {
        this.handlerDesc = handlerdesc;
    }
}
