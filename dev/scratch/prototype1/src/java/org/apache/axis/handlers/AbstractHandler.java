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
package org.apache.axis.handlers;

import javax.xml.namespace.QName;

import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Handler;
import org.apache.axis.engine.context.MessageContext;
import org.apache.axis.engine.registry.Parameter;

/**
 * @author Srinath Perera (hemapani@opensource.lk)
 */
public abstract class AbstractHandler implements Handler{
    private QName name;
    public QName getName() {
        return name;
    }
    public void invoke(MessageContext msgContext) throws AxisFault {
    }
    public void revoke(MessageContext msgContext){
    }
    public void setName(QName name) {
        this.name = name;
    }
    public void addParameter(Parameter param) {
            //TODO
    }
    public void cleanup() throws AxisFault {
    }
    public Parameter getParameter(String key) {
        //TODO
        return null;
    }
    public void init() throws AxisFault {
    }
}
