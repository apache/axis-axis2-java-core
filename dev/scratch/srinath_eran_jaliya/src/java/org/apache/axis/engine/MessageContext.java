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

import javax.xml.namespace.QName;

import org.apache.axis.context.GlobalContext;
import org.apache.axis.context.SessionContext;
import org.apache.axis.context.SimpleSessionContext;
import org.apache.axis.registry.EngineRegistry;
/**
 *  The palce where all the service specific states are kept. 
 *  All the Global states kept in the <code>EngineRegistry</code> and all the 
 *  Service states kept in the <code>MessageContext</code>. Other runtime
 *  artifacts does not keep states foward from the execution.  
 */
public class MessageContext {
    public MessageContext(EngineRegistry er){
        globalContext = new GlobalContext(er);
        sessionContext = new SimpleSessionContext();
    }
    
    
    private boolean processingFault = false;
    private QName currentTansport = null; 
    private QName currentService = null;
    private QName currentOperation = null;
    
    private SessionContext sessionContext;
    private GlobalContext globalContext;
    
    
    public boolean isProcessingFault(){
        return processingFault;
    }
    public void setProcessingFault(boolean processingFault){
        this.processingFault = processingFault;
    }
    /**
     * @return
     */
    public QName getCurrentTansport() {
        return currentTansport;
    }

   
    /**
     * @param name
     */
    public void setCurrentTansport(QName name) {
        currentTansport = name;
    }

    /**
     * @return
     */
    public QName getCurrentOperation() {
        return currentOperation;
    }


    /**
     * @param name
     */
    public void setCurrentOperation(QName name) {
        currentOperation = name;
    }

    /**
     * @param name
     */
    public void setCurrentService(QName name) {
        currentService = name;
    }

    /**
     * @return
     */
    public QName getCurrentService() {
        return currentService;
    }

    /**
     * @return
     */
    public GlobalContext getGlobalContext() {
        return globalContext;
    }

    /**
     * @return
     */
    public SessionContext getSessionContext() {
        return sessionContext;
    }

    /**
     * @param context
     */
    public void setGlobalContext(GlobalContext context) {
        globalContext = context;
    }

    /**
     * @param context
     */
    public void setSessionContext(SessionContext context) {
        sessionContext = context;
    }

}
