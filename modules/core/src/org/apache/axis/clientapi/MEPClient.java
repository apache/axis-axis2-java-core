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

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFactory;

/**
 * This is the Super Class for all the MEPClients, All the MEPClient will extend this.
 */
public abstract class MEPClient {
    protected ServiceContext serviceContext;
    protected final String mep;

    public MEPClient(ServiceContext service, String mep) {
        this.serviceContext = service;
        this.mep = mep;
    }

    protected void verifyInvocation(OperationDescription axisop) throws AxisFault {
        if (axisop == null) {
            throw new AxisFault("OperationDescription can not be null");
        }

        if (mep.equals(axisop.getMessageExchangePattern())) {
            throw new AxisFault(
                "This mepClient supports only "
                    + mep
                    + " And the Axis Operations suppiled supports "
                    + axisop.getMessageExchangePattern());
        }
        
        if(serviceContext.getServiceConfig().getOperation(axisop.getName()) == null){
            serviceContext.getServiceConfig().addOperation(axisop);
        }
    }

    protected MessageContext prepareTheSystem(OMElement toSend) throws AxisFault {
        MessageContext msgctx =
            new MessageContext(null, null, null, serviceContext.getEngineContext());

        SOAPEnvelope envelope = null;
        SOAPFactory omfac = OMAbstractFactory.getSOAP11Factory();
        envelope = omfac.getDefaultEnvelope();
        envelope.getBody().addChild(toSend);
        msgctx.setEnvelope(envelope);
        return msgctx;
    }

    public String inferTransport(EndpointReference epr) {
        String transport = null;
        if (epr != null) {
            String toURL = epr.getAddress();
            int index = toURL.indexOf(':');
            if (index > 0) {
                transport = toURL.substring(0, index);
            }
        }
        return transport;
    }

}
