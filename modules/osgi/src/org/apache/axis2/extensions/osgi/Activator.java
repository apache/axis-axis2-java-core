/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.extensions.osgi;

import org.apache.axis2.extensions.osgi.util.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

public class Activator implements BundleActivator, BundleListener {

    BundleContext context;
    OSGiAxis2Servlet servlet = new OSGiAxis2Servlet();
    ServiceRegistry registry = null;
    HttpService httpServ = null;
    Logger logger;

    public void start(BundleContext context) throws Exception {
        this.context = context;
        logger = new Logger(context);
        logger.log(LogService.LOG_INFO,"[Axis2/OSGi] Registering Axis2 Servlet");

        ServiceReference sr = context.getServiceReference(HttpService.class.getName());
        if (sr != null) {
            HttpService httpServ = (HttpService) context.getService(sr);
            try {
                httpServ.registerServlet("/axis2",
                        servlet, null, null);
            } catch (Exception e) {
                logger.log(LogService.LOG_ERROR,"[Axis2/OSGi] Exception registering Axis Servlet",
                        e);
            }
        }

        registry = new ServiceRegistry(servlet, logger);

        logger.log(LogService.LOG_INFO, "[Axis2/OSGi] Starting Bundle Listener");
        context.addBundleListener(this);
// TODO: We should poke at all the bundles already in the system
//        Bundle bundles[] = context.getBundles();
//        for (int i = 0; i < bundles.length; i++) {
//            if ((bundles[i].getState() &
//                    (Bundle.STARTING | Bundle.ACTIVE)) != 0) {
//                if(context.getBundle() != event.getBundle()){
//                    registry.register(bundles[i]);
//                }
//            }
//        }
    }

    public void bundleChanged(BundleEvent event) {
        Bundle bundle = event.getBundle();
        switch (event.getType()) {
            case BundleEvent.STARTED:
                if(context.getBundle() != bundle){
                    logger.log(LogService.LOG_INFO,"[Axis2/OSGi] Starting any services in Bundle - " + bundle.getSymbolicName());
                    registry.register(event.getBundle());
                }
                break;

            case BundleEvent.STOPPED:
                if(context.getBundle() != bundle){
                    logger.log(LogService.LOG_INFO,"[Axis2/OSGi] Stopping any services in Bundle - " + bundle.getSymbolicName());
                    registry.unregister(event.getBundle());
                }
                break;
        }
    }

    public void stop(BundleContext context) throws Exception {
        logger.log(LogService.LOG_INFO,"[Axis2/OSGi] Stopping all services and the Bundle Listener");
        this.context.removeBundleListener(this);
        logger.close();
        registry.close();
    }
}
