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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.*;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import static org.apache.axis2.osgi.deployment.OSGiAxis2Constants.MODULE_NOT_FOUND_ERROR;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Creates proper AxisServiceGroup/AxisService looking into bundles
 */
public class ServiceRegistry extends AbstractRegistry<AxisServiceGroup> {

    public ServiceRegistry(BundleContext context, ConfigurationContext configCtx) {
        super(context, configCtx);
    }

    public void register(Bundle bundle) throws AxisFault {
        lock.lock();
        try {
            addServices(bundle);
        } finally {
            lock.unlock();
        }
    }

    private void addServices(Bundle bundle) throws AxisFault {
        try {
            Enumeration enumeration = bundle.findEntries("META-INF", "services.xml", false);
            while (enumeration != null && enumeration.hasMoreElements()) {
                URL url = (URL) enumeration.nextElement();
                AxisServiceGroup serviceGroup =
                        new AxisServiceGroup(configCtx.getAxisConfiguration());
                serviceGroup.addParameter("last.updated", bundle.getLastModified());
                ClassLoader loader =
                        new BundleClassLoader(bundle, Registry.class.getClassLoader());
                serviceGroup.setServiceGroupClassLoader(loader);
                InputStream inputStream = url.openStream();
                DescriptionBuilder builder = new DescriptionBuilder(inputStream, configCtx);
                OMElement rootElement = builder.buildOM();
                String elementName = rootElement.getLocalName();
                Dictionary headers = bundle.getHeaders();
                String bundleSymbolicName = (String) headers.get("Bundle-SymbolicName");
                HashMap wsdlServicesMap = new HashMap();
                if (DeploymentConstants.TAG_SERVICE.equals(elementName)) {
                    AxisService axisService = new AxisService(bundleSymbolicName);
                    axisService.setParent(serviceGroup);
                    axisService.setClassLoader(loader);
                    ServiceBuilder serviceBuilder = new OSGiServiceBuilder(configCtx, axisService);
                    serviceBuilder.setWsdlServiceMap(wsdlServicesMap);
                    AxisService service = serviceBuilder.populateService(rootElement);
                    configCtx.getAxisConfiguration().addService(service);
                    //TODO: use OSGi log service from compendum.
                    System.out.println("[Axis2/OSGi] Deployed axis2 service:" + service.getName() +
                                       " in Bundle: " +
                                       bundle.getSymbolicName());
                } else if (DeploymentConstants.TAG_SERVICE_GROUP.equals(elementName)) {
                    ServiceGroupBuilder groupBuilder =
                            new OSGiServiceGroupBuilder(rootElement, wsdlServicesMap,
                                                        configCtx);
                    ArrayList serviceList = groupBuilder.populateServiceGroup(serviceGroup);
                    DeploymentEngine.addServiceGroup(serviceGroup,
                                                     serviceList,
                                                     null,
                                                     null,
                                                     configCtx.getAxisConfiguration());
                    System.out.println("[Axis2/OSGi] Deployed axis2 service group:" +
                                       serviceGroup.getServiceGroupName() + " in Bundle: " +
                                       bundle.getSymbolicName());
                }
                resolvedBundles.put(bundle, serviceGroup);
                //marked as resolved.
                if (unreslovedBundles.contains(bundle)) {
                    unreslovedBundles.remove(bundle);
                }
            }
        } catch (Throwable e) {
            //TODO: TBD log
            String msg = "Error while reading from the bundle";
            if (e instanceof DeploymentException) {
                String message = e.getMessage();
                if (message != null && message.length() != 0) {
                    if (message.indexOf(MODULE_NOT_FOUND_ERROR) > -1) {
                        if (!unreslovedBundles.contains(bundle)) {
                            unreslovedBundles.add(bundle);
                        }
                    } else {
                        throw new AxisFault(msg, e);
                    }
                } else {
                    throw new AxisFault(msg, e);
                }
            } else {
                throw new AxisFault(msg, e);
            }
        }
    }

    public void unRegister(Bundle bundle) throws AxisFault {
        lock.lock();
        try {
            AxisServiceGroup axisServiceGroup = resolvedBundles.get(bundle);
            if (axisServiceGroup != null) {
                resolvedBundles.remove(bundle);
                try {
                    configCtx.getAxisConfiguration()
                            .removeServiceGroup(axisServiceGroup.getServiceGroupName());
                    System.out.println("[Axis2/OSGi] Stopping" +
                                       axisServiceGroup.getServiceGroupName() +
                                       " service group in Bundle - " +
                                       bundle.getSymbolicName());
                    for (Iterator iterator = axisServiceGroup.getServices(); iterator.hasNext();) {
                        AxisService service = (AxisService) iterator.next();
                        System.out.println("[Axis2/OSGi]      Service - " + service.getName());
                    }
                } catch (AxisFault e) {
                    String msg = "Error while removing the service group";
                    throw new AxisFault(msg, e);
                }
            }
        } finally {
            lock.unlock();
        }
    }


}
