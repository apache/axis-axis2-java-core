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
package org.apache.axis2.osgi;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.osgi.deployment.OSGiConfigurationContextFactory;
import org.apache.axis2.osgi.deployment.OSGiServerConfigurator;
import org.osgi.framework.BundleContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * InitServlet is used only to initialize Axis2 environment
 * and it's meant not to listen to any request.
 */
public class InitServlet extends HttpServlet{

    private BundleContext context;

    public InitServlet(BundleContext context) {
        this.context = context;
    }
    
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        AxisConfigurator configurator = new OSGiServerConfigurator(context);
        try {
            ConfigurationContext configCtx = OSGiConfigurationContextFactory
                    .createConfigurationContext(configurator, context);
            //regiser the ConfigurationContext as an service.
            context.registerService(ConfigurationContext.class.getName(), configCtx, null);
        } catch (AxisFault e) {
            String msg = "Error while creating the ConfigurationContext";
            throw new ServletException(msg, e);
        }
        
    }
}
