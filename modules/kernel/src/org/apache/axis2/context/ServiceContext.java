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
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.i18n.Messages;

import javax.xml.namespace.QName;

/**
 * Well this is never clearly defined, what it does or the life-cycle.
 * So do NOT use this as it might not live up to your expectation.
 */
public class ServiceContext extends AbstractContext {

    public static final String SERVICE_OBJECT = "serviceObject";
    private EndpointReference targetEPR;
    private EndpointReference myEPR;

    private transient AxisService axisService;
    private ServiceGroupContext serviceGroupContext;
    private ConfigurationContext configContext;

    public ServiceContext(AxisService serviceConfig, ServiceGroupContext serviceGroupContext) {
        super(serviceGroupContext);
        this.serviceGroupContext = serviceGroupContext;
        this.axisService = serviceConfig;
        this.configContext = (ConfigurationContext) parent.getParent();
    }

    public OperationContext createOperationContext(QName name) {
        AxisOperation axisOp = axisService.getOperation(name);

        return new OperationContext(axisOp, this);
    }

    public AxisService getAxisService() {
        return axisService;
    }

    public ConfigurationContext getConfigurationContext() {
        return configContext;
    }

    public ServiceGroupContext getServiceGroupContext() {
        return serviceGroupContext;
    }

    /**
     * To get the ERP for a given service , if the transport is present and not
     * running then it will add as a listener to ListenerManager , there it will
     * init that and start the listener , and finally ask the EPR from transport
     * for a given service
     *
     * @param transport : Name of the transport
     * @throws AxisFault
     */
    public EndpointReference getMyEPR(String transport) throws AxisFault {
        axisService.isEnableAllTransports();
        ConfigurationContext configctx = this.configContext;
        if (configctx != null) {
            ListenerManager lm = configctx.getListenerManager();
            if (!lm.isListenerRunning(transport)) {
                TransportInDescription trsin = configctx.getAxisConfiguration().
                        getTransportIn(new QName(transport));
                if (trsin != null) {
                    lm.addListener(trsin, false);
                } else {
                    throw new AxisFault(Messages.getMessage("transportnotfound",
                            transport));
                }
            }
            if (!lm.isStopped()) {
                return lm.getEPRforService(axisService.getName(), null, transport);
            }
        }
        return null;
    }

    public EndpointReference getTargetEPR() {
        return targetEPR;
    }

    public void setTargetEPR(EndpointReference targetEPR) {
        this.targetEPR = targetEPR;
    }

    public EndpointReference getMyEPR() {
        if (myEPR == null) {
            try {
                if (ListenerManager.defaultConfigurationContext != null) {
                    ListenerManager listenerManager =
                            ListenerManager.defaultConfigurationContext.getListenerManager();
                    myEPR = listenerManager.getEPRforService(axisService.getName(), null, null);
                }
            } catch (AxisFault axisFault) {
                // what else I can do 
                myEPR = null;
            }
        }
        return myEPR;
    }

    public void setMyEPR(EndpointReference myEPR) {
        this.myEPR = myEPR;
    }
}
