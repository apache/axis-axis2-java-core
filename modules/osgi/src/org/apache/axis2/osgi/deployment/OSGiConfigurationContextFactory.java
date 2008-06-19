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

import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.AxisFault;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;

/**
 *
 */
public class OSGiConfigurationContextFactory {

    public static ConfigurationContext createConfigurationContext(
            AxisConfigurator axisConfigurator, BundleContext context) throws AxisFault {
        ConfigurationContext configCtx =
                ConfigurationContextFactory.createConfigurationContext(axisConfigurator);

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

        return configCtx;

    }


}
