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

package org.apache.axis2.osgi.core.web;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.servlet.context.ServletContextHelper;
import org.osgi.service.servlet.runtime.HttpServiceRuntime;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

/**
 *
 * WebApp is a utility class for describing a WebApplication to be deployed into an OSGi
 * HTTP Service implementation. The WebApp implementation extends the OSGi <code>ServletContextHelper</code>.
 */
public class WebApp extends ServletContextHelper {
	protected static WebAppDescriptor webAppDescriptor = null;

	protected HttpServiceRuntime httpServiceRuntime;

	protected ServiceReference sRef;

	protected Set<ServiceRegistration<?>> serviceRegistrations = new HashSet<ServiceRegistration<?>>();

	public WebApp(WebAppDescriptor descriptor) {
		webAppDescriptor = descriptor;
	}

	// Return null and let the HTTP determine the type
	public String getMimeType(String reqEntry) {
		return null;
	}

	// Get the resource from the jar file, use the class loader to do it
	public URL getResource(String name) {
		URL url = getClass().getResource(name);

		return url;
	}

	@Override
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
            return true;
	}

	/**
	 * Starts the WebApp
	 * @param bc the BundleContext of the WebApp host
	 * @throws BundleException
	 */
	public void start(BundleContext bc) throws BundleException {	
		ServiceReference<HttpServiceRuntime> sRef = bc.getServiceReference(HttpServiceRuntime.class);
		if (sRef == null)
			throw new BundleException("Failed to get ServiceReference");
		if ((httpServiceRuntime = bc.getService(sRef)) == null)
			throw new BundleException("Failed to get httpServiceRuntime");
		try {
			WebAppDescriptor wad = webAppDescriptor;

			for (int i = 0; i < wad.servlet.length; i++) {
				ServletDescriptor servlet = wad.servlet[i];

   // ServiceRegistration<?> registerService(String clazz, Object service, Dictionary<String, ?> properties);
   // Dictionary<String,Object> props = new Hashtable<>(2);
			        // serviceRegistrations.add(bc.registerService(wad.context + servlet.subContext, servlet.servlet, servlet.initParameters, this));
			        serviceRegistrations.add(bc.registerService(ServletContextHelper.class, this, servlet.initParameters));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BundleException("Failed to register servlets");
		}
	}

	/**
	 * Stops the WebApp
	 * @param bc the BundleContext of the WebApp host
	 * @throws BundleException
	 */
	public void stop(BundleContext bc) throws BundleException {
            try {
                for (ServiceRegistration<?> serviceRegistration : serviceRegistrations) {
                    serviceRegistration.unregister();
                }
            
                serviceRegistrations.clear();
                bc.ungetService(sRef);
                httpServiceRuntime = null;
                webAppDescriptor = null;
            } catch (Exception e) {
                throw new BundleException("Failed to unregister resources", e);
            }
	}

    public static WebAppDescriptor getWebAppDescriptor() {
        return webAppDescriptor;
    }

    public static void setWebAppDescriptor(WebAppDescriptor webAppDescriptor) {
        WebApp.webAppDescriptor = webAppDescriptor;
    }

    public HttpServiceRuntime getHttpServiceRuntime() {
        return httpServiceRuntime;
    }

    public void setHttpServiceRuntime(HttpServiceRuntime httpServiceRuntime) {
        this.httpServiceRuntime = httpServiceRuntime;
    }

    public ServiceReference getSRef() {
        return sRef;
    }

    public void setSRef(ServiceReference sRef) {
        this.sRef = sRef;
    }
}
