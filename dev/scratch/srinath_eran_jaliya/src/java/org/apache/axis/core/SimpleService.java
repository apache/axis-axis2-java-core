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

package org.apache.axis.core;

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.core.context.MessageContext;
import org.apache.axis.core.exec.ExecutionChain;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleService extends AbstractContainer implements Service{
    private ExecutionChain inExecChain;
    private ExecutionChain outexecChain;
    private ExecutionChain faultExecChain;
    
    public ExecutionChain getFaultExecutionChain() {
        return faultExecChain;
    }
    public ExecutionChain getOutputExecutionChain() {
        return outexecChain;
    }
    public void setFaultExecutionChain(ExecutionChain faultExecChain) {
        this.faultExecChain = faultExecChain;
    }

    public void setOutExecutionChain(ExecutionChain outexecChain) {
        this.outexecChain = outexecChain;
    }

    
    public ExecutionChain getInputExecutionChain() {
        return inExecChain;
    }
    public void setInputExecutionChain(ExecutionChain execChain) {
        this.inExecChain = execChain;

    }
    private Log log = LogFactory.getLog(getClass());     
    private HashMap operations = new HashMap();
    private QName name;
    private Handler provider;
    private Handler sender;    
    private ClassLoader classLoader;
    
    public SimpleService(QName name){
        this.name = name;
    } 
    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public Operation getOperation(QName index) {
        return (Operation)operations.get(index);
    }

    public int getOperationCount() {
        return operations.size();
    }


    public QName getName() {
        return name;
    }

    public void addOperation(Operation op) {
        operations.put(op.getName(),op);
    }

    public Handler getProvider() {
        return provider;
    }

    public void setProvider(Handler provider) {
        this.provider = provider;

    }

    public void setClassLoader(ClassLoader cl) {
        this.classLoader = cl;

    }

    /**
     * @return
     */
    public Handler getSender() {
        return sender;
    }

    /**
     * @param handler
     */
    public void setSender(Handler handler) {
        sender = handler;
    }

}
