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

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.AxisServlet;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.WarBasedAxisConfigurator;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.Constants;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

public class OSGiAxis2Servlet extends AxisServlet {
    
    /**
     * Set the context root if it is not set already. We are overriding AxisServlet's impl to
     * get past an issue in Felix.
     *
     * @param req
     */
    public void initContextRoot(HttpServletRequest req) {
        if (contextRoot != null && contextRoot.trim().length() != 0) {
            return;
        }
        String contextPath = req.getServletPath();

        //handling ROOT scenario, for servlets in the default (root) context, this method returns ""
        if (contextPath != null && contextPath.length() == 0) {
            contextPath = "/";
        }
        this.contextRoot = contextPath;

        configContext.setContextRoot(contextRoot);
    }

    public ConfigurationContext ConfigurationContext() {
        return configContext;
    }
}
