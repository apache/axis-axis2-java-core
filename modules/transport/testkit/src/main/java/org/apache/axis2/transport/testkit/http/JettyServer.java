/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.http;

import org.apache.axis2.transport.testkit.channel.Channel;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.TearDown;
import org.apache.axis2.transport.testkit.tests.Transient;
import org.eclipse.jetty.ee9.nested.AbstractHandler;
import org.eclipse.jetty.ee9.nested.ContextHandler;
import org.eclipse.jetty.ee9.nested.Handler;
import org.eclipse.jetty.ee9.nested.HandlerCollection;
import org.eclipse.jetty.server.Server;

public class JettyServer {
    public static final JettyServer INSTANCE = new JettyServer();
    
    private @Transient Server server;
    // ee9 specific nested HandlerCollection
    private @Transient HandlerCollection ee9HandlerCollection;
    
    private JettyServer() {}
    
    @Setup @SuppressWarnings("unused")
    private void setUp(HttpTestEnvironment env) throws Exception {
        server = new Server(env.getServerPort());
        ContextHandler context = new ContextHandler(server, Channel.CONTEXT_PATH);
        ee9HandlerCollection = new HandlerCollection();
        context.setHandler(ee9HandlerCollection);
        server.start();
    }
    
    public void addHandler(Handler handler) {
        ee9HandlerCollection.addHandler(handler);
    }

    public void removeHandler(Handler handler) {
        ee9HandlerCollection.removeHandler(handler);
    }

    @TearDown @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        server.stop();
    }
}
