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

import org.apache.axis2.extensions.osgi.util.BundleListener;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

public class Activator implements BundleActivator {

    BundleContext context;

    public void start(BundleContext context) throws Exception {
        this.context = context;

        ServiceReference sr = context.getServiceReference(HttpService.class.getName());
        if (sr != null) {
            HttpService httpServ = (HttpService) context.getService(sr);

            try {
                OSGiAxis2Servlet servlet = new OSGiAxis2Servlet();
                httpServ.registerServlet("/axis2",
                        servlet, null, null);
                context.addBundleListener(new BundleListener(servlet));
            } catch (Exception e) {
                System.err.println("Exception registering Axis Servlet:"
                        + e);
            }
        }
    }

    public void stop(BundleContext context) throws Exception {
    }

}
