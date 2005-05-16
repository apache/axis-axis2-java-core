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
 *  Runtime state of the engine
 */
package org.apache.axis.clientapi;

import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.engine.AxisFault;

/**
 * This is the Super Class for all the MEPClients, All the MEPClient will extend this.
 */
public abstract class MEPClient {
    protected ServiceContext serviceContext;
    public MEPClient(ServiceContext service){
        this.serviceContext = service;
    }
    
    protected void verifyInvocation(OperationDescription axisop) throws AxisFault{
        if(axisop == null){
            throw new AxisFault("OperationDescription can not be null");
         }   
    }

}
