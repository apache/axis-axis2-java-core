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
import org.apache.axis2.engine.DependencyManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * All the engine components are stateless across the executions and all the states should be kept in the
 * Contexts, there are three context Global, Session and Message.
 */
public class SessionContext extends AbstractContext {

    private transient HashMap serviceContextMap = new HashMap();
    private transient HashMap serviceGroupContextMap = new HashMap();
    private transient String cookieID;
	private static final Log log = LogFactory.getLog(SessionContext.class);

    // current time out interval is 30 secs. Need to make this configurable
    public transient long sessionContextTimeoutInterval = 30 * 1000;

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

    public void addServiceGroupContext(ServiceGroupContext serviceGroupContext,
                                       String serviceGroupID) {
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
     * ServiceContext and ServiceGroupContext are not getting automatically garbage collectible. And there
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

    public Iterator getServiceGroupContext() {
        if (serviceGroupContextMap != null) {
            return serviceGroupContextMap.values().iterator();
        } else {
            return null;
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (serviceGroupContextMap != null && !serviceGroupContextMap.isEmpty()) {
            Iterator valuse = serviceGroupContextMap.values().iterator();
            while (valuse.hasNext()) {
                ServiceGroupContext serviceGroupContext = (ServiceGroupContext) valuse.next();
                cleanupServiceContextes(serviceGroupContext);
            }
        }
    }

    private void cleanupServiceContextes(ServiceGroupContext serviceGroupContext) {
        Iterator serviceContecxtes = serviceGroupContext.getServiceContexts();
        while (serviceContecxtes.hasNext()) {
            ServiceContext serviceContext = (ServiceContext) serviceContecxtes.next();
            try {
                DependencyManager.destroyServiceObject(serviceContext);
            } catch (AxisFault axisFault) {
                log.info(axisFault.getMessage());
            }
        }
    }


}
