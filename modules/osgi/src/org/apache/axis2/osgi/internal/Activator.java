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
package org.apache.axis2.osgi.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.osgi.InitServlet;
import org.apache.axis2.osgi.OSGiAxisServlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import javax.servlet.ServletException;

import static org.apache.axis2.osgi.deployment.OSGiAxis2Constants.*;

/**
 * Activator will set the necessary parameters that initiate Axis2 OSGi integration
 * TODO: TBD; yet the structure is being formed
 */
public class Activator implements BundleActivator {

    private HttpServiceTracker tracker;


    public void start(BundleContext context) throws Exception {
        tracker = new HttpServiceTracker(context);
        tracker.open();
    }

    public void stop(BundleContext context) throws Exception {
        tracker.close();
        //ungetService
        ServiceReference axisConfigRef =
                context.getServiceReference(AxisConfigurator.class.getName());
        if (axisConfigRef != null) {
            context.ungetService(axisConfigRef);
        }
        ServiceReference configCtxRef =
                context.getServiceReference(ConfigurationContext.class.getName());
        if (configCtxRef != null) {
            context.ungetService(configCtxRef);
        }
    }

    //service trackers

    private static class HttpServiceTracker extends ServiceTracker {

        public HttpServiceTracker(BundleContext context) {
            super(context, HttpService.class.getName(), null);
        }

        public Object addingService(ServiceReference serviceReference) {

            HttpService httpService = (HttpService) context.getService(serviceReference);
            try {
                InitServlet initServlet = new InitServlet(context);
                httpService.registerServlet("/init_servlet_not_public", initServlet, null, null);
                OSGiAxisServlet axisServlet = new OSGiAxisServlet(context);
                ServiceReference configCtxRef =
                        context.getServiceReference(ConfigurationContext.class.getName());
                ConfigurationContext configCtx =
                        (ConfigurationContext) context.getService(configCtxRef);
                String propServiceContextRoot = context.getProperty(AXIS2_OSGi_ROOT_CONTEXT);
                String serviceContextRoot = "services";
                if (propServiceContextRoot != null && propServiceContextRoot.length() != 0) {
                    if (propServiceContextRoot.startsWith("/")) {
                        serviceContextRoot = propServiceContextRoot.substring(1);
                    } else {
                        serviceContextRoot = propServiceContextRoot;
                    }
                }
                configCtx.setServicePath(serviceContextRoot);
                httpService.registerServlet("/" + serviceContextRoot, axisServlet, null, null);
            } catch (ServletException e) {
                String msg = "Error while registering servlets";
                throw new RuntimeException(msg, e);
            } catch (NamespaceException e) {
                String msg = "Namespace missmatch when registering servlets";
                throw new RuntimeException(msg, e);
            }
            return httpService;
        }

    }
}
