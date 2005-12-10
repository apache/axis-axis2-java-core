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
 */

package org.apache.axis2.context;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;

import javax.xml.namespace.QName;

/**
 * Well this is never clearly defined, what it does or the lifecycle.
 * So do NOT use this as it might not live up to your expectation.
 */
public class ServiceContext extends AbstractContext {

    private transient AxisService axisService;

    private String serviceInstanceID;

    private QName axisServiceName = null;


    /**
     * Initializes the engine context.
     *
     * @throws AxisFault
     */
    public void init(AxisConfiguration axisConfiguration) throws AxisFault {
        axisService = axisConfiguration.getService(axisServiceName.getLocalPart());
    }

    public ServiceContext(
            AxisService serviceConfig,
            ServiceGroupContext serviceGroupContext) {
        super(serviceGroupContext);
        this.axisService = serviceConfig;

        if (serviceConfig != null) {
            this.axisServiceName = serviceConfig.getName();
            serviceInstanceID = serviceConfig.getName().getLocalPart();
        }

    }

    /**
     * @return Returns the serviceInstanceID.
     */
    public String getServiceInstanceID() {
        return serviceInstanceID;
    }

    /**
     * Sets service instance id.
     *
     * @param serviceInstanceID
     */
    public void setServiceInstanceID(String serviceInstanceID) {
        //todo we do not need this , this ID should equal to serviceName
        this.serviceInstanceID = serviceInstanceID;
    }

    public AxisService getAxisService() {
        return axisService;
    }

    public ConfigurationContext getConfigurationContext() {
        return (ConfigurationContext) parent.getParent();
    }

    public OperationContext createOperationContext(QName name) {
        AxisOperation axisOp = axisService.getOperation(name);
        return new OperationContext(axisOp, this);
    }
}
