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
import org.apache.axis2.modules.Module;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.ModuleBuilder;
import org.apache.axis2.description.AxisModule;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * @see org.osgi.framework.BundleListener
 * TODO: TBD removed sout
 */
public class ModuleRegistry extends AbstractRegistry<AxisModule> {


    public ModuleRegistry(BundleContext context, ConfigurationContext configCtx) {
        super(context, configCtx);
    }

    public void register(Bundle bundle) throws AxisFault {
        lock.lock();
        try {
            addModules(bundle);
        } finally {
            lock.unlock();
        }

    }

    public void unRegister(Bundle bundle) throws AxisFault {
        lock.lock();
        try {
            AxisModule module = bundleMap.get(bundle);
            if (module != null) {
                bundleMap.remove(bundle);
                configCtx.getAxisConfiguration()
                        .removeModule(module.getName(), module.getVersion());
                System.out.println("[Axis2/OSGi] Stopping" + module.getName() + ":" +
                                   module.getVersion() + " moduel in Bundle - " +
                                   bundle.getSymbolicName());
            }
        } finally {
            lock.unlock();
        }
    }

    private void addModules(Bundle bundle) throws AxisFault {
        if (!bundleMap.containsKey(bundle)) {
            try {
                Enumeration enumeration = bundle.findEntries("META-INF", "module.xml", false);
                while (enumeration != null && enumeration.hasMoreElements()) {
                    URL url = (URL) enumeration.nextElement();
                    String urlString = url.toString();
                    //                String shortFileName = urlString.substring(urlString.lastIndexOf('/'));
                    AxisModule axismodule = new AxisModule();
                    ClassLoader loader =
                            new BundleClassLoader(bundle, Registry.class.getClassLoader());
                    axismodule.setModuleClassLoader(loader);
                    ModuleBuilder builder =
                            new ModuleBuilder(url.openStream(), axismodule,
                                              configCtx.getAxisConfiguration());
                    //                axismodule.setName(org.apache.axis2.util.Utils.getModuleName(shortFileName));
                    //                axismodule.setVersion(org.apache.axis2.util.Utils.getModuleVersion(shortFileName));
                    builder.populateModule();
                    axismodule.setParent(configCtx.getAxisConfiguration());
                    //                axismodule.setFileName(new URL(bundle.getLocation()));
                    //TODO this logic needed to be revised. remove sout
                    AxisModule module =
                            configCtx.getAxisConfiguration().getModule(axismodule.getName());
                    if (module == null) {
                        DeploymentEngine.addNewModule(axismodule, configCtx.getAxisConfiguration());
                        //initialze the module if the module contains Module interface.
                        Module moduleObj = axismodule.getModule();
                        if (moduleObj != null) {
                            moduleObj.init(configCtx, axismodule);
                        }
                        bundleMap.put(bundle, axismodule);
                        System.out.println("[Axis2/OSGi] Starting any modules in Bundle - " +
                                           bundle.getSymbolicName());
                    } else {
                        System.out.println("[ModuleRegistry] Module : " + axismodule.getName() +
                                           " is already available.");
                    }
                }
            } catch (IOException e) {
                String msg = "Error while reading module.xml";
                throw new AxisFault(msg, e);
            }
        }

    }
}
