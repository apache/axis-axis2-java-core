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

import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;

import javax.xml.namespace.QName;


public class ServiceContext extends AbstractContext {
    private AxisService serviceConfig;

    private String serviceInstanceID;

    public ServiceContext(AxisService serviceConfig, SystemContext engineContext) {
        super(engineContext);
        this.serviceConfig = serviceConfig;


    }


    /**
     * @return Returns the serviceInstanceID.
     */
    public String getServiceInstanceID() {
        return serviceInstanceID;
    }

    /**
     * @param serviceInstanceID The serviceInstanceID to set.
     */
    public void setServiceInstanceID(String serviceInstanceID) {
        this.serviceInstanceID = serviceInstanceID;
    }

    /**
     * @return
     */
    public AxisService getServiceConfig() {
        return serviceConfig;
    }

    public SystemContext getEngineContext() {
        return (SystemContext) parent;
    }

    public OperationContext createOperationContext(QName name) {
        AxisOperation axisOp = serviceConfig.getOperation(name);
        return new OperationContext(axisOp, this);
    }
}
