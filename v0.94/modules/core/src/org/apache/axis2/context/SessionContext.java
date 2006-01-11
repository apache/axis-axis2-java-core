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
import org.apache.axis2.engine.AxisConfiguration;

import java.util.Date;
import java.util.HashMap;

/**
 * All the engine components are stateless across the executions and all the states should be kept in the
 * Contexts, there are three context Global, Session and Message.
 */
public class SessionContext extends AbstractContext {

    private HashMap serviceContextMap = new HashMap();
    private HashMap serviceGroupContextMap = new HashMap();
    private String cookieID;

    // current time out interval is 30 secs. Need to make this configurable
    public long sessionContextTimeoutInterval = 30 * 1000;

    /**
     * @param parent
     */
    public SessionContext(AbstractContext parent) {
        super(parent);
    }

    public void init(AxisConfiguration axisConfiguration) throws AxisFault {
    }

    public ServiceContext getServiceContext(AxisService axisService) {
        return (ServiceContext) serviceContextMap.get(axisService.getName());
    }

    public void addServiceContext(ServiceContext serviceContext) {
        serviceContextMap.put(serviceContext.getAxisService().getName(), serviceContext);
    }

    public void addServiceGroupContext(ServiceGroupContext serviceGroupContext, String serviceGroupID) {
        serviceGroupContextMap.put(serviceGroupID, serviceGroupContext);
    }

    public ServiceGroupContext getServiceGroupContext(String serviceGroupID) {
        return (ServiceGroupContext) serviceGroupContextMap.get(serviceGroupID);
    }

    public String getCookieID() {
        return cookieID;
    }

    public void setCookieID(String cookieID) {
        this.cookieID = cookieID;
    }

    /**
     * ServiceContext and ServiceGroupContext are not getting automatically garbage collected. And there
     * is no specific way for some one to go and make it garbage collectable.
     * So the current solution is to make them time out. So the logic is that, there is a timer task
     * in each and every service group which will check for the last touched time. And if it has not
     * been touched for some time, the timer task will remove it from the memory.
     * The touching logic happens like this. Whenever there is a call to addMessageContext in the operationContext
     * it will go and update operationCOntext -> serviceContext -> serviceGroupContext.
     */
    public void touch() {
        lastTouchedTime = new Date().getTime();
        if (parent != null) {
            parent.touch();
        }
    }

    public long getLastTouchedTime() {
        return lastTouchedTime;
    }


}
