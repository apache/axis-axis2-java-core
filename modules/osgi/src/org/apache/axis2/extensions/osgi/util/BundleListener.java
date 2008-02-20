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

package org.apache.axis2.extensions.osgi.util;

import org.apache.axis2.transport.http.AxisServlet;
import org.apache.axis2.extensions.osgi.OSGiAxis2Servlet;
import org.apache.axis2.engine.AxisConfiguration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;

public class BundleListener implements SynchronousBundleListener {
    private OSGiAxis2Servlet servlet;

    public BundleListener(OSGiAxis2Servlet servlet) {
        this.servlet = servlet;
    }

    public void bundleChanged(BundleEvent event) {
        switch (event.getType()) {
            case BundleEvent.STARTED:
                onBundleStarted(event.getBundle());
                break;
            case BundleEvent.STOPPED:
                onBundleStopped(event.getBundle());
                break;
        }
    }

    private void onBundleStarted(Bundle bundle) {
        AxisConfiguration config = servlet.getConfiguration();
        System.out.println("onBundleStarted : " + bundle);
    }

    private void onBundleStopped(Bundle bundle) {
        System.out.println("onBundleStopped : " + bundle);
    }
}
