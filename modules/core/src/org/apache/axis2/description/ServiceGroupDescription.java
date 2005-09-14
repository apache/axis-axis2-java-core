package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.phaseresolver.PhaseResolver;

import javax.xml.namespace.QName;
import java.util.HashMap;
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

public class ServiceGroupDescription {
    private final HashMap services = new HashMap();
    private AxisConfiguration axisDescription;


    public ServiceGroupDescription(AxisConfiguration axisDescription) {
        this.axisDescription = axisDescription;
    }

    public synchronized void addService(ServiceDescription service) throws AxisFault {
        services.put(service.getName(), service);
        PhaseResolver handlerResolver = new PhaseResolver(this.axisDescription, service);
        handlerResolver.buildchains();
        service.setLastupdate();
        this.axisDescription.notifyObservers(AxisEvent.SERVICE_DEPLOY ,service);
        service.setParent(this);
    }

    public AxisConfiguration getAxisDescription() {
        return axisDescription;
    }

    public void setAxisDescription(AxisConfiguration axisDescription) {
        this.axisDescription = axisDescription;
    }

    public ServiceDescription getService(QName name) throws AxisFault {
        return (ServiceDescription) services.get(name);
    }

    public HashMap getServices() {
        return services;
    }


    public synchronized void removeService(QName name) throws AxisFault {
        ServiceDescription service = getService(name);
        if (service != null) {
            this.axisDescription.notifyObservers(AxisEvent.SERVICE_DEPLOY , service);
        }
        services.remove(name);
    }
}
