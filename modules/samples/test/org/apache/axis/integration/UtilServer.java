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

package org.apache.axis.integration;

import java.io.File;
import java.net.ServerSocket;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.ConfigurationContextFactory;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.deployment.DeploymentEngine;
import org.apache.axis.description.ModuleDescription;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.engine.AxisConfigurationImpl;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.http.SimpleHTTPServer;
import org.apache.axis.util.Utils;

public class UtilServer {
    private static int count = 0;
    private static SimpleHTTPServer reciver;
    public static final int TESTING_PORT = 5555;
    public static final String FAILURE_MESSAGE = "Intentional Faliure";
    public static final String TESTING_REPOSITORY = "target/test-resources/samples";
    //public static final String TESTING_REPOSITORY = "modules/samples/target/test-resources/samples";

    public static synchronized void deployService(ServiceDescription service) throws AxisFault {
        reciver.getSystemContext().getEngineConfig().addService(service);
        Utils.resolvePhases(reciver.getSystemContext().getEngineConfig(), service);
    }

    public static synchronized void unDeployService(QName service) throws AxisFault {
        reciver.getSystemContext().getEngineConfig().removeService(service);
    }

    public static synchronized void start() throws Exception {
        if (count == 0) {
            ConfigurationContextFactory erfac = new ConfigurationContextFactory();
            File file = new File(TESTING_REPOSITORY);
            if (!file.exists()) {
                throw new Exception("repository directory does not exists");
            }
            ConfigurationContext er = erfac.buildEngineContext(file.getAbsolutePath());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                throw new AxisFault("Thread interuptted", e1);
            }

            ServerSocket serverSoc = null;
            serverSoc = new ServerSocket(Constants.TESTING_PORT);
            reciver = new SimpleHTTPServer(er, serverSoc);
            Thread thread = new Thread(reciver);
            thread.setDaemon(true);

            try {
                thread.start();
                System.out.print("Server started on port " + Constants.TESTING_PORT + ".....");
            } finally {

            }
        }
        count++;
    }

    public static synchronized void stop() {
        if (count == 1) {
            reciver.stop();
            count = 0;
            System.out.print("Server stopped .....");
        } else {
            count--;
        }
    }

    public static ConfigurationContext getConfigurationContext() {
        return reciver.getSystemContext();
    }

    public static ServiceContext createAdressedEnabledClientSide(ServiceDescription service)
        throws AxisFault {
        DeploymentEngine deploymentEngine = new DeploymentEngine();
        File file = new File(UtilServer.TESTING_REPOSITORY + "/modules/addressing.mar");
        TestCase.assertTrue(file.exists());
        ModuleDescription moduleDesc = deploymentEngine.buildModule(file);

        ConfigurationContextFactory efac = new ConfigurationContextFactory();
        ConfigurationContext sysContext = efac.buildClientEngineContext(null);
        new ConfigurationContext(new AxisConfigurationImpl());
        sysContext.getEngineConfig().addMdoule(moduleDesc);
        sysContext.getEngineConfig().engageModule(moduleDesc.getName());

        sysContext.getEngineConfig().addService(service);
        //Utils.resolvePhases(sysContext.getEngineConfig(), service);
        ServiceContext serviceContext = sysContext.createServiceContext(service.getName());
        return serviceContext;

    }

}
