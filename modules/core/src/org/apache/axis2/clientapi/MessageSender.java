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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.om.OMElement;

import javax.xml.namespace.QName;

/**
 *    Message Sender is the class simmiler to the Call, one that provides much simpler API
 *    to users to work with. 
 */
public class MessageSender extends InOnlyMEPClient {
    
    /**
     * Service context of the Service this MessageSender handles, compare this with the Call, simpler method.
     * @param service
     */
    public MessageSender(ServiceContext service) {
        super(service);
    }

    public MessageSender() throws AxisFault {
        super(assumeServiceContext());
    }
    /**
     * Send the SOAP Message, the actual worker
     * @param opName
     * @param toSend
     * @throws AxisFault
     */
    public void send(String opName, OMElement toSend) throws AxisFault {
        OperationDescription axisOp = serviceContext.getServiceConfig()
                .getOperation(opName);
        if (axisOp == null) {
            axisOp = new OperationDescription(new QName(opName));
            serviceContext.getServiceConfig().addOperation(axisOp);
        }
        super.send(axisOp, prepareTheSOAPEnvelope(toSend));
    }

    /**
     * create a default service Context if the users are not intersted in the lower levels of control
     * @return
     * @throws AxisFault
     */
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
        return sysContext.createServiceContext(
                assumedServiceName);
    }
}
