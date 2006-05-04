package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.modules.Module;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportSender;
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

	private static final Log log = LogFactory.getLog(ListenerManager.class);

    public static ConfigurationContext defaultConfigurationContext;

    private ConfigurationContext configctx;
    private HashMap startedTransports = new HashMap();
    private boolean stopped = true;

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
     * @param serviceName   : Name of the service
     * @param transportName : name of the trasport can be null , if it is null then
     * @return String
     */
    public synchronized EndpointReference getEPRforService(String serviceName, String opName,
                                                           String transportName) throws AxisFault {
        if (transportName == null || "".equals(transportName)) {
            AxisService service = configctx.getAxisConfiguration().getService(serviceName);
            if (service == null) {
                throw new AxisFault(Messages.getMessage(
                        "servicenotfoundinthesystem", serviceName));
            }
            if (service.isEnableAllTransport()) {
                Iterator itr_st = startedTransports.values().iterator();
                if (itr_st.hasNext()) {
                    TransportListener transportListener = (TransportListener) itr_st.next();
                    return transportListener.getEPRForService(serviceName, null);
                } else {
                    return null;
                }
            } else {
                String exposeTransport [] = service.getExposeTransports();
                TransportListener listener = (TransportListener)
                        startedTransports.get(exposeTransport[0]);
                if (opName == null) {
                    return listener.getEPRForService(serviceName, null);
                } else return listener.getEPRForService(serviceName + "/" + opName, null);
            }

        } else {
            TransportInDescription trsIN = configctx.getAxisConfiguration()
                    .getTransportIn(new QName(transportName));
            TransportListener listener = trsIN.getReceiver();
            if (opName == null) {
                return listener.getEPRForService(serviceName, null);
            } else return listener.getEPRForService(serviceName + "/" + opName, null);
        }
    }

    /**
     * To start all the transports
     */
    public synchronized void start() {
        Iterator transportNames = configctx.getAxisConfiguration().
                getTransportsIn().values().iterator();
        while (transportNames.hasNext()) {
            try {
                TransportInDescription transportIn = (TransportInDescription) transportNames.next();
                TransportListener listener = transportIn.getReceiver();
                if (listener != null && startedTransports.get(transportIn.getName().getLocalPart()) == null) {
                    listener.init(configctx, transportIn);
                    listener.start();
                    if (startedTransports.get(transportIn.getName().getLocalPart()) == null) {
                        startedTransports.put(transportIn.getName().getLocalPart(), listener);
                    }
                }
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }
        stopped = false;
    }

    public synchronized void startSystem(ConfigurationContext configurationContext) {
        init(configurationContext);
        start();
    }

    /**
     * To stop all the transports
     */
    public synchronized void stop() throws AxisFault {
        Iterator itr_st = startedTransports.values().iterator();
        while (itr_st.hasNext()) {
            TransportListener transportListener = (TransportListener) itr_st.next();
            transportListener.stop();
        }

        // Stoping Transport senders
        HashMap transportOut = configctx.getAxisConfiguration().getTransportsOut();
        if (transportOut.size() > 0) {
            Iterator trsItr = transportOut.values().iterator();
            while (trsItr.hasNext()) {
                TransportOutDescription outDescription = (TransportOutDescription) trsItr.next();
                TransportSender trsSededer = outDescription.getSender();
                if (trsSededer != null) {
                    trsSededer.stop();
                }
            }
        }
        //calling module shoutdown method
        HashMap modules = configctx.getAxisConfiguration().getModules();
        if (modules != null) {
            Iterator moduleitr = modules.values().iterator();
            while (moduleitr.hasNext()) {
                AxisModule axisModule = (AxisModule) moduleitr.next();
                Module module = axisModule.getModule();
                if (module != null) {
                    module.shutdown(configctx);
                }
            }
        }
        configctx.cleanupContexts();
        stopped = true;
    }

    /**
     * @param trsIn   : Transport in description (which contains Transport Listener)
     * @param started : whether transport Listener running or not
     * @throws AxisFault : will throw AxisFault if something goes wrong
     */
    public synchronized void addListener(TransportInDescription trsIn, boolean started) throws AxisFault {
        configctx.getAxisConfiguration().addTransportIn(trsIn);
        TransportListener transportListener = trsIn.getReceiver();
        if (transportListener != null) {
            if (!started) {
                transportListener.init(configctx, trsIn);
                transportListener.start();
            }
            stopped = false;
            startedTransports.put(trsIn.getName().getLocalPart(), transportListener);
        }
    }

    public synchronized boolean isListenerRunning(String transportName) {
        return startedTransports.get(transportName) != null;
    }

    public boolean isStopped() {
        return stopped;
    }
}
