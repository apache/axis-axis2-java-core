package org.apache.axis.context;

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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.description.AxisOperation;

public class OperationContext extends AbstractContext{
    private Map mepContextMap;
    private AxisOperation operationConfig;

     public OperationContext(AxisOperation operationConfig) {
        super();
        this.mepContextMap = new HashMap();
        this.operationConfig = operationConfig;
    }

    public void addMepContext(MEPContext ctxt){
        this.mepContextMap.put(ctxt.getMepId(),ctxt);
    }

    public MEPContext getMepContext(String mepId){
        return (MEPContext)mepContextMap.get(mepId);
    }

    public MEPContext removeMepContext(MEPContext ctxt){
        mepContextMap.remove(ctxt.getMepId());
        return ctxt;
    }
    /**
     * @return
     */
    public AxisOperation getOperationConfig() {
        return operationConfig;
    }

    /**
     * @param operation
     */
    public void setOperationConfig(AxisOperation operation) {
        operationConfig = operation;
    }
    
    public QName getName(){
        return operationConfig.getName();
    }

}
