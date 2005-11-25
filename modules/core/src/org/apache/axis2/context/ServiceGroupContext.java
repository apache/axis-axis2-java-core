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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServiceGroupContext extends AbstractContext {

    private String id;
    private Map serviceContextMap;
    private transient AxisServiceGroup axisServiceGroup;
    private String axisServiceGroupName =null;



    public void init(AxisConfiguration axisConfiguration) throws AxisFault {
        if (axisServiceGroupName !=null)
            axisServiceGroup = axisConfiguration.getServiceGroup(axisServiceGroupName);
    }

    public ServiceGroupContext(ConfigurationContext parent ,AxisServiceGroup axisServiceGroup) {
        super(parent);
        this.axisServiceGroup = axisServiceGroup;
        serviceContextMap = new HashMap();

        if (axisServiceGroup !=null)
            this.axisServiceGroupName = axisServiceGroup.getServiceGroupName();
        fillServiceContexts();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    //if the service name is foo:bar , you should pass only bar
    public ServiceContext getServiceContext(String serviceName) {
        return (ServiceContext) serviceContextMap.get(serviceName);
    }

    /**
     * This will create one ServiceContext per each serviceDesc in descrpition
     * if serviceGroup desc has 2 service init , then two serviceContext will be
     * created
     */
    private void fillServiceContexts(){
        Iterator services = axisServiceGroup.getServices();
        while (services.hasNext()) {
            AxisService axisService = (AxisService) services.next();
            ServiceContext serviceContext = new ServiceContext(axisService,this);
            String servicNams = axisService.getName().getLocalPart();
            serviceContextMap.put(servicNams,serviceContext);
        }
    }

    public AxisServiceGroup getDescription() {
        return axisServiceGroup;
    }

    public Iterator getServiceContexts(){
        return serviceContextMap.values().iterator();
    }
}
