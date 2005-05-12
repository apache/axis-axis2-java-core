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

import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.context.SystemContext;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.AxisTransportIn;
import org.apache.axis.description.AxisTransportOut;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.AxisSystemImpl;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.SOAPFactory;

/**
 * This class is the pretty convineance class for the user without see the comlplexites of Axis2.
 */
public class Call extends InOutMEPClient {
    private HashMap properties;
    private boolean isEnvelope = false;

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

        AxisOperation axisConfig =
            serviceContext.getServiceConfig().getOperation(new QName(axisop));
      MessageContext msgctx = prepareTheSystem(axisConfig,toSend);

  
        MessageContext responseContext = super.invokeBlocking(axisConfig, msgctx);
        SOAPEnvelope resEnvelope = responseContext.getEnvelope();
        if (isEnvelope) {
            return resEnvelope;
        } else {
            return resEnvelope.getBody().getFirstElement();
        }
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
            AxisOperation axisConfig =
                        serviceContext.getServiceConfig().getOperation(new QName(axisop));
        MessageContext msgctx = prepareTheSystem(axisConfig,toSend);
        

        super.invokeNonBlocking(axisConfig, msgctx, callback);
    }
    
    /**
     * Prepare the MessageContext, this is Utility method
     * @param axisOp
     * @param toSend
     * @return
     * @throws AxisFault
     */

    private MessageContext prepareTheSystem(AxisOperation axisOp,OMElement toSend) throws AxisFault {
        SystemContext syscontext = serviceContext.getEngineContext();
        //Make sure the AxisService object has the AxisOperation
        serviceContext.getServiceConfig().addOperation(axisOp);
        
        
        AxisTransportIn transportIn =
            syscontext.getEngineConfig().getTransportIn(new QName(listenertransport));
        AxisTransportOut transportOut =
            syscontext.getEngineConfig().getTransportOut(new QName(senderTransport));

        MessageContext msgctx = new MessageContext(null, transportIn, transportOut, syscontext);

        SOAPEnvelope envelope = null;

        if (toSend instanceof SOAPEnvelope) {
            envelope = (SOAPEnvelope) toSend;
            isEnvelope = true;
        } else {
            SOAPFactory omfac = OMAbstractFactory.getSOAP11Factory();
            envelope = omfac.getDefaultEnvelope();
            envelope.getBody().addChild(toSend);
            isEnvelope = false;
        }

        msgctx.setEnvelope(envelope);
        return msgctx;
    }

    /**
     * Assume the values for the SystemContext and ServiceContext to make the NON WSDL cases simple.
     * @return ServiceContext that has a SystemContext set in and has assumed values.
     * @throws AxisFault
     */
    private static ServiceContext assumeServiceContext() throws AxisFault {
        SystemContext sysContext = new SystemContext(new AxisSystemImpl(new AxisGlobal()));
        QName assumedServiceName = new QName("AnonnoymousService");
        AxisService axisService = new AxisService(assumedServiceName);
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
