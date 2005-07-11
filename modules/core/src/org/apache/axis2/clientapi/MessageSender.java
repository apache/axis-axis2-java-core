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
 *  Runtime state of the engine
 */
package org.apache.axis2.clientapi;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.AxisFault;
import org.apache.axis2.om.OMElement;

import javax.xml.namespace.QName;

/**
 * @author hemapani
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MessageSender extends InOnlyMEPClient {
    public MessageSender(ServiceContext service) {
        super(service);
    }

    public MessageSender() throws AxisFault {
        super(assumeServiceContext());
    }

    public void send(String opName, OMElement toSend) throws AxisFault {
        OperationDescription axisOp = serviceContext.getServiceConfig()
                .getOperation(opName);
        if (axisOp == null) {
            axisOp = new OperationDescription(new QName(opName));
            serviceContext.getServiceConfig().addOperation(axisOp);
        }
        super.send(axisOp, prepareTheSystem(toSend));
    }

    private static ServiceContext assumeServiceContext() throws AxisFault {
        ConfigurationContext sysContext = null;
        if (ListenerManager.configurationContext == null) {
            ConfigurationContextFactory efac = new ConfigurationContextFactory();
            sysContext = efac.buildClientConfigurationContext(null);
        } else {
            sysContext = ListenerManager.configurationContext;
        }

        //create new service
        QName assumedServiceName = new QName("AnonnoymousService");
        ServiceDescription axisService = new ServiceDescription(
                assumedServiceName);
        sysContext.getAxisConfiguration().addService(axisService);
        ServiceContext service = sysContext.createServiceContext(
                assumedServiceName);
        return service;
    }
}
