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
package org.apache.axis.clientapi;

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.EngineContextFactory;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.GlobalDescription;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.AxisSystemImpl;
import org.apache.axis.om.OMElement;
import org.apache.axis.soap.SOAPEnvelope;

/**
 * This class is the pretty convineance class for the user without see the comlplexites of Axis2.
 */
public class Call extends InOutMEPClient {
    private HashMap properties;

    public Call() throws AxisFault {
        super(assumeServiceContext());
    }

    public Call(ServiceContext service) {
        super(service);
    }

    /**
     * Invoke the blocking/Synchronous call
     * @param axisop
     * @param toSend - This can be just OM Element (payload) or the SOAPEnvelope and the
     * invocation behaves accordingly
     * @return
     * @throws AxisFault
     */

    public OMElement invokeBlocking(String axisop, OMElement toSend) throws AxisFault {

        OperationDescription axisConfig =
            serviceContext.getServiceConfig().getOperation(new QName(axisop));
        if (axisConfig == null) {
            axisConfig = new OperationDescription(new QName(axisop));
            serviceContext.getServiceConfig().addOperation(axisConfig);
        }
        MessageContext msgctx = prepareTheSystem(toSend);

        MessageContext responseContext = super.invokeBlocking(axisConfig, msgctx);
        SOAPEnvelope resEnvelope = responseContext.getEnvelope();
        return resEnvelope.getBody().getFirstElement();
    }
    /**
     * Invoke the nonblocking/Asynchronous call
     * @param axisop
     * @param toSend - This can be just OM Element (payload) or the SOAPEnvelope and the
     * invocation behaves accordingly
     * @param callback
     * @throws AxisFault
     */

    public void invokeNonBlocking(String axisop, OMElement toSend, Callback callback)
        throws AxisFault {
        OperationDescription axisConfig =
            serviceContext.getServiceConfig().getOperation(new QName(axisop));
        if (axisConfig == null) {
            axisConfig = new OperationDescription(new QName(axisop));
            serviceContext.getServiceConfig().addOperation(axisConfig);
        }
        MessageContext msgctx = prepareTheSystem(toSend);

        super.invokeNonBlocking(axisConfig, msgctx, callback);
    }

  

    /**
     * Assume the values for the ConfigurationContext and ServiceContext to make the NON WSDL cases simple.
     * @return ServiceContext that has a ConfigurationContext set in and has assumed values.
     * @throws AxisFault
     */
    private static ServiceContext assumeServiceContext() throws AxisFault {
        EngineContextFactory efac = new EngineContextFactory();
        ConfigurationContext sysContext = efac.buildClientEngineContext(null);
        new ConfigurationContext(new AxisSystemImpl(new GlobalDescription()));

        //create new service
        QName assumedServiceName = new QName("AnonnoymousService");
        ServiceDescription axisService = new ServiceDescription(assumedServiceName);
        sysContext.getEngineConfig().addService(axisService);
        ServiceContext service = sysContext.createServiceContext(assumedServiceName);
        return service;
    }

    /**
     * @param key
     * @return
     */
    public Object get(Object key) {
        return properties.get(key);
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public Object set(Object key, Object value) {
        return properties.put(key, value);
    }
}
