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

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.extensions.osgi.util.BundleClassLoader;
import org.apache.axis2.jaxws.framework.JAXWSDeployer;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

public class ServiceRegistry extends JAXWSDeployer {
    private OSGiAxis2Servlet servlet;
    private LogService log;
    private HashMap services = new HashMap();

    public ServiceRegistry(OSGiAxis2Servlet servlet, LogService log) {
        this.servlet = servlet;
        this.log = log;
    }

    public void register(Bundle bundle) {
        axisConfig = servlet.getConfiguration();
        ClassLoader loader = new BundleClassLoader(bundle, this.getClass().getClassLoader());
        ArrayList classes = new ArrayList();

        Enumeration enumeration = bundle.findEntries("/", "*.class", true);
        while (enumeration.hasMoreElements()) {
            URL url = (URL) enumeration.nextElement();
            String path = url.getPath();
            // skip the leading '/' the trailing ".class" and replace path separator with '.'
            path = path.substring(1, path.length() - 6).replace('/', '.');
            classes.add(path);
        }
        try {
            AxisServiceGroup serviceGroup = deployClasses(bundle.getSymbolicName(), null, loader, classes);
            if (serviceGroup != null) {
                System.out.println("Deployed ServiceGroup - " + serviceGroup.getServiceGroupName());

                for (Iterator iterator = serviceGroup.getServices(); iterator.hasNext();) {
                    AxisService service = (AxisService) iterator.next();
                    System.out.println("     Service - " + service.getName());
                }
                services.put(bundle, serviceGroup);
            }
        } catch (Exception e) {
            log.log(LogService.LOG_INFO, "Exception deploying classes", e);
        }
    }

    public void close() {
        for (Iterator iterator = services.values().iterator(); iterator.hasNext();) {
            AxisServiceGroup serviceGroup = (AxisServiceGroup) iterator.next();
            try {
                axisConfig.removeServiceGroup(serviceGroup.getServiceGroupName());
                System.out.println("Undeployed ServiceGroup - " + serviceGroup.getServiceGroupName());

                for (Iterator iterator2 = serviceGroup.getServices(); iterator2.hasNext();) {
                    AxisService service = (AxisService) iterator2.next();
                    System.out.println("     Service - " + service.getName());
                }
            } catch (AxisFault axisFault) {
                log.log(LogService.LOG_INFO, axisFault.getMessage(), axisFault);
            }
        }
    }

    public void unregister(Bundle bundle) {
        AxisServiceGroup serviceGroup = (AxisServiceGroup) services.get(bundle);
        if (serviceGroup != null) {
            try {
                axisConfig.removeServiceGroup(serviceGroup.getServiceGroupName());
                System.out.println("Undeployed ServiceGroup - " + serviceGroup.getServiceGroupName());

                for (Iterator iterator2 = serviceGroup.getServices(); iterator2.hasNext();) {
                    AxisService service = (AxisService) iterator2.next();
                    System.out.println("     Service - " + service.getName());
                }
            } catch (AxisFault axisFault) {
                log.log(LogService.LOG_INFO, axisFault.getMessage(), axisFault);
            }
        }
    }
}
