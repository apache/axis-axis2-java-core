/*
 * Copyright 2007 The Apache Software Foundation.
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
package org.apache.axis2.integration;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AddressingBasedDispatcher;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DispatchPhase;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.transport.local.LocalTransportReceiver;
import org.apache.axis2.transport.local.LocalTransportSender;

import java.util.ArrayList;

/**
 * LocalTestCase is an extendable base class which provides common functionality
 * for building JUnit tests which exercise Axis2 using the (fast, in-process)
 * "local" transport.
 */
public class LocalTestCase extends TestCase {
    /** Our server AxisConfiguration */
    protected AxisConfiguration serverConfig;

    /** Our client ConfigurationContext */
    protected ConfigurationContext clientCtx;

    LocalTransportSender sender = new LocalTransportSender();

    protected void setUp() throws Exception {
        // Configuration - server side
        serverConfig = new AxisConfiguration();
        LocalTransportReceiver.CONFIG_CONTEXT = new ConfigurationContext(serverConfig);
        TransportOutDescription tOut = new TransportOutDescription(Constants.TRANSPORT_LOCAL);
        tOut.setSender(new LocalTransportSender());
        serverConfig.addTransportOut(tOut);

        addInPhases(serverConfig.getGlobalInFlow());
        DispatchPhase dp = (DispatchPhase)serverConfig.getGlobalInFlow().get(1);
        dp.addHandler(new AddressingBasedDispatcher());

        addInPhases(serverConfig.getInFaultFlow());

        addOutPhases(serverConfig.getGlobalOutPhases());
        addOutPhases(serverConfig.getOutFaultFlow());

        // NOTE : If you want addressing (which you probably do), we can do something
        // like this, or we can pull it off the classpath (better solution?)
        //
        // serverConfig.deployModule("repo/modules/addressing.mar");
        // serverConfig.engageModule("addressing");

        ///////////////////////////////////////////////////////////////////////
        // Set up raw message receivers for OMElement based tests

        serverConfig.addMessageReceiver(WSDL2Constants.MEP_URI_IN_OUT,
                                        new RawXMLINOutMessageReceiver());
        serverConfig.addMessageReceiver(WSDL2Constants.MEP_URI_IN_OUT,
                                        new RawXMLINOutMessageReceiver());

        ///////////////////////////////////////////////////////////////////////
        // And client side
        clientCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null);
    }

    /**
     * Add well-known Phases on the in side
     *
     * @param flow the Flow in which to add these Phases
     */
    private void addInPhases(ArrayList flow) {
        flow.add(new Phase("PreDispatch"));
        Phase dispatchPhase = new DispatchPhase("Dispatch");
        flow.add(dispatchPhase);
    }

    /**
     * Add well-known Phases on the out side
     *
     * @param flow the Flow in which to add these Phases
     */
    private void addOutPhases(ArrayList flow) {
        flow.add(new Phase("MessageOut"));
    }

    /**
     * Deploy a class as a service.
     *
     * @param name the service name
     * @param myClass the Java class to deploy (all methods exposed by default)
     * @return a fully configured AxisService, already deployed into the server
     * @throws Exception in case of problems
     */
    protected AxisService deployClassAsService(String name, Class myClass) throws Exception {
        AxisService service = new AxisService(name);
        service.addParameter(Constants.SERVICE_CLASS,
                              myClass.getName());

        Utils.fillAxisService(service, serverConfig, null, null);

        serverConfig.addService(service);
        return service;
    }

    /**
     * Get a pre-initialized ServiceClient set up to talk to our local
     * server.  If you want to set options, call this and then use getOptions()
     * on the return.
     *
     * @return a ServiceClient, pre-initialized to talk using our local sender
     * @throws AxisFault if there's a problem
     */
    protected ServiceClient getClient() throws AxisFault {
        TransportOutDescription td = new TransportOutDescription("local");
        td.setSender(sender);

        Options opts = new Options();
        opts.setTransportOut(td);

        ServiceClient client = new ServiceClient(clientCtx, null);
        client.setOptions(opts);
        return client;
    }
}
