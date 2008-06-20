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
package org.apache.axis2.osgi.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.engine.ListenerManager;
import static org.apache.axis2.osgi.deployment.OSGiAxis2Constants.PROTOCOL;
import org.apache.axis2.osgi.tx.HttpListener;
import org.apache.axis2.transport.TransportListener;
import org.osgi.framework.*;

import java.util.Dictionary;
import java.util.Properties;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public class OSGiConfigurationContextFactory {

    public static ConfigurationContext createConfigurationContext(
            AxisConfigurator axisConfigurator, BundleContext context) throws AxisFault {
        ConfigurationContext configCtx =
                ConfigurationContextFactory.createConfigurationContext(axisConfigurator);
        ListenerManager listenerManager = new ListenerManager();
        listenerManager.init(configCtx);
        listenerManager.start();
        ListenerManager.defaultConfigurationContext = configCtx;

        // first check (bundlestarts at the end or partially) {
        //      // loop  and add axis*
        // } then {
        //      // stat the bundle early
        // }
        Registry servicesRegistry = new ServiceRegistry(context, configCtx);
        Registry moduleRegistry = new ModuleRegistry(context, configCtx, servicesRegistry);
        Bundle[] bundles = context.getBundles();
        if (bundles != null) {
            for (Bundle bundle : bundles) {
                if (bundle != context.getBundle()) {
                    if (bundle.getState() == Bundle.ACTIVE) {
                        moduleRegistry.register(bundle);
                    }
                }
            }
            for (Bundle bundle : bundles) {
                if (bundle != context.getBundle()) {
                    if (bundle.getState() == Bundle.ACTIVE) {
                        servicesRegistry.register(bundle);
                    }
                }
            }
        }
        context.addBundleListener(moduleRegistry);
        context.addBundleListener(servicesRegistry);
        context.addServiceListener(new AxisConfigServiceListener(configCtx, context));

        Dictionary prop = new Properties();
        prop.put(PROTOCOL, "http");
        //adding the default listener
        context.registerService(TransportListener.class.getName(), new HttpListener(context), prop);

        return configCtx;

    }

    private static class AxisConfigServiceListener implements ServiceListener {

        private ConfigurationContext configCtx;

        private BundleContext context;

        private Lock lock = new ReentrantLock();

        public AxisConfigServiceListener(ConfigurationContext configCtx, BundleContext context) {
            this.configCtx = configCtx;
            this.context = context;
        }

        public void serviceChanged(ServiceEvent event) {
            ServiceReference reference = event.getServiceReference();
            Object service = context.getService(reference);
            if (service instanceof TransportListener) {
                String protocol = (String) reference.getProperty(PROTOCOL);
                if (protocol == null || protocol.length() == 0) {
                    throw new RuntimeException(
                            "Protocol is not found for the trnasport object");
                }
                if (event.getType() == ServiceEvent.REGISTERED) {
                    TransportListener txListener =
                            (TransportListener) service;

                    TransportInDescription txInDes = new TransportInDescription(protocol);
                    txInDes.setReceiver(txListener);
                    String[] keys = reference.getPropertyKeys();
                    if (keys != null) {
                        for (String key : keys) {
                            if (key.equals(PROTOCOL)) {
                                continue;
                            }
                            //TODO: assume String properties at this moment.
                            try {
                                Object propObj = reference.getProperty(key);
                                if (propObj instanceof String) {
                                    String value = (String) propObj;
                                    Parameter param = new Parameter(key, value);
                                    txInDes.addParameter(param);
                                }
                            } catch (AxisFault e) {
                                String msg = "Error while reading transport properties from :" +
                                             txListener.toString();
                                throw new RuntimeException(msg, e);
                            }
                        }
                    }
                    try {
                        configCtx.getListenerManager().addListener(txInDes, false);
                        //Now update the AxisService endpoint map
                        lock.lock();
                        try {
                            for (Iterator iterator =
                                    configCtx.getAxisConfiguration().getServices().keySet()
                                            .iterator();
                                 iterator.hasNext();) {
                                String serviceName = (String) iterator.next();
                                AxisService axisService =
                                        configCtx.getAxisConfiguration().getService(serviceName);
                                Utils.addEndpointsToService(axisService,
                                                            configCtx.getAxisConfiguration());
                            }
                        } finally {
                            lock.unlock();
                        }
                    } catch (AxisFault e) {
                        String msg = "Error while intiating and starting the listener";
                        throw new RuntimeException(msg, e);
                    }
                }

            }

        }
    }


}
