package org.apache.axis2.context;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.description.ServiceGroupDescription;
import org.apache.axis2.description.ServiceDescription;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
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

/**
 * Author: Deepal Jayasinghe
 * : Eran Chinthaka
 */
public class ServiceGroupContext extends AbstractContext {

    private String id;
    private Map serviceContextMap;
    private ServiceGroupDescription description;


    public ServiceGroupContext(ConfigurationContext parent ,ServiceGroupDescription description) {
        super(parent);
        this.description = description;
        serviceContextMap = new HashMap();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    //if the servic name is foo:bar , you should pass only bar
    public ServiceContext getServiceContext(String serviceName) {
        return (ServiceContext) serviceContextMap.get(serviceName);
    }

    /**
     * This will create one ServiceContext per each serviceDesc in descrpition
     * if serviceGroup desc has 2 service init , then two serviceContext will be
     * created
     */
    public void fillServiceContexts(){
        Iterator services = description.getServices();
        while (services.hasNext()) {
            ServiceDescription serviceDescription = (ServiceDescription) services.next();
            ServiceContext serviceContext = new ServiceContext(serviceDescription,this);
            String [] servicNams = AxisConfigurationImpl.splitServiceName(
                    serviceDescription.getName().getLocalPart());
            serviceContextMap.put(servicNams[1],serviceContext);
        }
    }



//    public void registerServiceContext(ServiceContext serviceContext) throws AxisFault {
//        String serviceName = serviceContext.getServiceConfig().
//                getName().getLocalPart();
//        ServiceContext serviceContextAlreadyRegistered = (ServiceContext) serviceContextMap.get(serviceName);
//        if (serviceContextAlreadyRegistered == null) {
//            serviceContextMap.put(serviceName, serviceContext);
//            serviceContext.setParent(this);
//        }
//    }

    public ServiceGroupDescription getDescription() {
        return description;
    }
}
