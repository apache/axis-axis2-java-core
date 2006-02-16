package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
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

public class ListenerManager {

    private Log log = LogFactory.getLog(getClass());

    private ConfigurationContext configctx;
    private HashMap startedTranports = new HashMap();
    private boolean stoped;

    public void init(ConfigurationContext configCtx) {
        configCtx.setTransportManager(this);
        this.configctx = configCtx;
    }

    public ConfigurationContext getConfigctx() {
        return configctx;
    }

    /**
     * To get an EPR for a given service
     *
     * @param serviceName  : Name of the service
     * @param tranportName : name of the trasport can be null , if it is null then
     * @return String
     */
    public EndpointReference getERPforService(String serviceName, String opName,
                                              String tranportName) throws AxisFault {
        if (tranportName == null || "".equals(tranportName)) {
            AxisService service = configctx.getAxisConfiguration().getService(serviceName);
            if (service == null) {
                throw new AxisFault("Invalid service " +
                        serviceName + " , not found in the system");
            }
            if (service.isEnableAllTransport()) {
                Iterator itr_st = startedTranports.values().iterator();
                if (itr_st.hasNext()) {
                    TransportListener transportListener = (TransportListener) itr_st.next();
                    return transportListener.getEPRForService(serviceName);
                } else {
                    return null;
                }
            } else {
                String exposeTransport [] = service.getExposeTransports();
                TransportListener listener = (TransportListener)
                        startedTranports.get(exposeTransport[0]);
                if (opName == null) {
                    return listener.getEPRForService(serviceName);
                } else return listener.getEPRForService(serviceName + "/" + opName);
            }

        } else {
            TransportInDescription trsIN = configctx.getAxisConfiguration()
                    .getTransportIn(new QName(tranportName));
            TransportListener listener = trsIN.getReceiver();
            if (opName == null) {
                return listener.getEPRForService(serviceName);
            } else return listener.getEPRForService(serviceName + "/" + opName);
        }
    }

    /**
     * To start all the tranports
     */
    public void start() {
        Iterator tranportNames = configctx.getAxisConfiguration().
                getTransportsIn().values().iterator();
        while (tranportNames.hasNext()) {
            try {
                TransportInDescription tranportIn = (TransportInDescription) tranportNames.next();
                TransportListener listener = tranportIn.getReceiver();
                if (listener != null) {
                    listener.init(configctx, tranportIn);
                    listener.start();
                    if (startedTranports.get(tranportIn.getName().getLocalPart()) == null) {
                        startedTranports.put(tranportIn.getName().getLocalPart(), listener);
                    }
                }
            } catch (AxisFault axisFault) {
                log.info(axisFault.getMessage());
            }
        }
        stoped = false;
    }

    public void startSystem(ConfigurationContext configurationContext) {
        init(configurationContext);
        start();
    }

    /**
     * To stop all the tranport
     */
    public void stop() throws AxisFault {
        Iterator itr_st = startedTranports.values().iterator();
        while (itr_st.hasNext()) {
            TransportListener transportListener = (TransportListener) itr_st.next();
            transportListener.stop();
        }
        stoped = true;
    }

    public void addListener(TransportInDescription trsIn, boolean start) throws AxisFault {
        configctx.getAxisConfiguration().addTransportIn(trsIn);
        TransportListener transportListener = trsIn.getReceiver();
        if (transportListener != null) {
            if (!start) {
                transportListener.start();
            }
            startedTranports.put(trsIn.getName().getLocalPart(), transportListener);
        }
    }

    public boolean isStoped() {
        return stoped;
    }
}
