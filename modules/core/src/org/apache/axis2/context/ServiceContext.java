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

import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.addressing.EndpointReference;

import javax.xml.namespace.QName;
import java.util.ArrayList;

/**
 * Well this is never clearly defined, what it does or the lifecycle.
 * So do NOT use this as it might not live up to your expectation.
 */
public class ServiceContext extends AbstractContext {

    private String myEPRAddress;
    private EndpointReference targetEPR;

    private transient AxisService axisService;
    private String serviceInstanceID;
    private ServiceGroupContext serviceGroupContext;

    //to store service implementation class , to handler session
    private Object serviceImpl;
    private ArrayList replyTorefpars;

    public ServiceContext(AxisService serviceConfig, ServiceGroupContext serviceGroupContext) {
        super(serviceGroupContext);
        this.serviceGroupContext = serviceGroupContext;
        this.axisService = serviceConfig;
        this.replyTorefpars = new ArrayList();
        if (serviceConfig != null) {
            serviceInstanceID = serviceConfig.getName();
        }
    }

    public OperationContext createOperationContext(QName name) {
        AxisOperation axisOp = axisService.getOperation(name);

        return new OperationContext(axisOp, this);
    }

    public AxisService getAxisService() {
        return axisService;
    }

    public ConfigurationContext getConfigurationContext() {
        return (ConfigurationContext) parent.getParent();
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

        // todo we do not need this , this ID should equal to serviceName
        this.serviceInstanceID = serviceInstanceID;
    }

    public Object getServiceImpl() {
        return serviceImpl;
    }

    public void setServiceImpl(Object serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    public ServiceGroupContext getServiceGroupContext() {
        return serviceGroupContext;
    }

    public ArrayList getReplyTorefpars() {
        return replyTorefpars;
    }

    public void setReplyTorefpars(ArrayList replyTorefpars) {
        this.replyTorefpars = replyTorefpars;
    }

    public String getMyEPRAddress() {
        return myEPRAddress;
    }

    public void setMyEPRAddress(String myEPRAddress) {
        this.myEPRAddress = myEPRAddress;
    }

    public EndpointReference getTargetEPR() {
        return targetEPR;
    }

    public void setTargetEPR(EndpointReference targetEPR) {
        this.targetEPR = targetEPR;
    }
}
