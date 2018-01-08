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
package org.apache.axis2.testutils.jaxws;

import javax.xml.ws.Endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.junit.rules.ExternalResource;

public final class JAXWSEndpoint extends ExternalResource {
    private static final Log log = LogFactory.getLog(JAXWSEndpoint.class);

    private final Object implementor;
    private Server server;

    public JAXWSEndpoint(Object implementor) {
        this.implementor = implementor;
    }

    @Override
    protected void before() throws Throwable {
        server = new Server();
        SelectChannelConnector connector = new SelectChannelConnector();
        server.addConnector(connector);
        HttpContextImpl httpContext = new HttpContextImpl();
        Endpoint.create(implementor).publish(httpContext);
        server.setHandler(new JAXWSHandler(httpContext));
        server.start();
    }

    @Override
    protected void after() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception ex) {
                log.error("Failed to stop Jetty server", ex);
            }
            server = null;
        }
    }

    public String getAddress() {
        return String.format("http://localhost:%s/", server.getConnectors()[0].getLocalPort());
    }
}
