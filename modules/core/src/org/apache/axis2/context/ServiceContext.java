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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.AxisConfiguration;

import javax.xml.namespace.QName;

/**
 * Well this is never clearly defined, what is does nor the lifecycle
 * So I advised to NOT to use this .. as it might not live up to your expectation.
 */
public class ServiceContext extends AbstractContext {
	
    private transient ServiceDescription serviceConfig;

    private String serviceInstanceID;

    private QName serviceDescName = null;
    
    /**
     * The method is used to do the intialization of the EngineContext
     * @throws AxisFault
     */
    public void init(AxisConfiguration axisConfiguration) throws AxisFault {
    	serviceConfig = axisConfiguration.getService(serviceDescName.getLocalPart());
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {	
    	if (serviceConfig!=null)
    		this.serviceDescName = serviceConfig.getName();
    	
    	out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
    }
    
    public ServiceContext(
        ServiceDescription serviceConfig,
        ServiceGroupContext serviceGroupContext) {
        super(serviceGroupContext);
        this.serviceConfig = serviceConfig;
        
        if (serviceConfig!=null)
        	this.serviceDescName = serviceConfig.getName();

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
        return (ConfigurationContext) parent.getParent();
    }

    public OperationContext createOperationContext(QName name) {
        OperationDescription axisOp = serviceConfig.getOperation(name);
        return new OperationContext(axisOp, this);
    }
}
