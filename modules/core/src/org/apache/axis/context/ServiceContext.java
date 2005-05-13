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

import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.ServiceDescription;

import javax.xml.namespace.QName;


public class ServiceContext extends AbstractContext {
    private ServiceDescription serviceConfig;

    private String serviceInstanceID;

    public ServiceContext(ServiceDescription serviceConfig, ConfigurationContext engineContext) {
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
    public ServiceDescription getServiceConfig() {
        return serviceConfig;
    }

    public ConfigurationContext getEngineContext() {
        return (ConfigurationContext) parent;
    }

    public OperationContext createOperationContext(QName name) {
        OperationDescription axisOp = serviceConfig.getOperation(name);
        return new OperationContext(axisOp, this);
    }
}
