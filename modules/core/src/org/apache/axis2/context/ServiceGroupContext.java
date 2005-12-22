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
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServiceGroupContext extends AbstractContext {

    private transient AxisServiceGroup axisServiceGroup;
    private String id;
    private Map serviceContextMap;

    public ServiceGroupContext(ConfigurationContext parent, AxisServiceGroup axisServiceGroup) {
        super(parent);
        this.axisServiceGroup = axisServiceGroup;
        serviceContextMap = new HashMap();
    }

    public AxisServiceGroup getDescription() {
        return axisServiceGroup;
    }

    public String getId() {
        return id;
    }

    /**
     * At each time you ask for a service context this will create a new one by
     * passing AxisService into it , and no need to store service context inside serviceGroup
     * context as well
     *
     * @param service
     * @return
     * @throws AxisFault
     */
    public ServiceContext getServiceContext(AxisService service) throws AxisFault {
        AxisService axisService = axisServiceGroup.getService(service.getName());
        if (axisService == null) {
            throw new AxisFault("Invalid service " + service.getName() + " not belong to " +
                    "service group " + axisServiceGroup.getServiceGroupName());
        }
        String scope = axisService.getScope();
        ServiceContext serviceContext;
        if (Constants.APPLICATION_SCOPE.equals(scope) || Constants.SOAP_SESSION_SCOPE.equals(scope)) {
            //since the session scope is longer that trasport or request we need to store that some where
            serviceContext = (ServiceContext) serviceContextMap.get(service.getName());
            if (serviceContext == null) {
                serviceContext = new ServiceContext(service, this);
            }
            serviceContextMap.put(service.getName(), serviceContext);
        } else {
            serviceContext = new ServiceContext(service, this);
        }
        return serviceContext;
    }

    public Iterator getServiceContexts() {
        return serviceContextMap.values().iterator();
    }

    public void setId(String id) {
        this.id = id;
    }
}
