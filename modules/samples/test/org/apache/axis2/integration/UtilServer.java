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

package org.apache.axis2.integration;

import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.AxisFault;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.ServerSocket;

public class UtilServer {
    private static int count = 0;
    private static SimpleHTTPServer receiver;
    public static final int TESTING_PORT = 5555;
    public static final String FAILURE_MESSAGE = "Intentional Faliure";

    public static synchronized void deployService(ServiceDescription service) throws AxisFault {
        receiver.getSystemContext().getAxisConfiguration().addService(service);
        Utils.resolvePhases(receiver.getSystemContext().getAxisConfiguration(), service);
    }

    public static synchronized void unDeployService(QName service) throws AxisFault {
        receiver.getSystemContext().getAxisConfiguration().removeService(service);
    }

    public static synchronized void start() throws Exception {
        start(org.apache.axis2.Constants.TESTING_REPOSITORY);
    }

    public static synchronized void start(String repositry) throws Exception {
        if (count == 0) {
            ConfigurationContextFactory erfac = new ConfigurationContextFactory();
            File file = new File(repositry);
            if (!file.exists()) {
                throw new Exception("repository directory " + file.getAbsolutePath() + " does not exists");
            }
            ConfigurationContext er = erfac.buildConfigurationContext(file.getAbsolutePath());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                throw new AxisFault("Thread interuptted", e1);
            }

            ServerSocket serverSoc = null;
            serverSoc = new ServerSocket(Constants.TESTING_PORT);
            receiver = new SimpleHTTPServer(er, serverSoc);
            Thread thread = new Thread(receiver);
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
            receiver.stop();
            count = 0;
            System.out.print("Server stopped .....");
        } else {
            count--;
        }
    }

    public static ConfigurationContext getConfigurationContext() {
        return receiver.getSystemContext();
    }

    public static ServiceContext createAdressedEnabledClientSide(ServiceDescription service)
            throws AxisFault {
        DeploymentEngine deploymentEngine = new DeploymentEngine();
        File file = new File(org.apache.axis2.Constants.TESTING_REPOSITORY + "/modules/addressing.mar");
        TestCase.assertTrue(file.exists());
        ModuleDescription moduleDesc = deploymentEngine.buildModule(file);

        ConfigurationContextFactory efac = new ConfigurationContextFactory();
        ConfigurationContext sysContext = efac.buildClientConfigurationContext(null);

        sysContext.getAxisConfiguration().addMdoule(moduleDesc);
        //sysContext.getAxisConfiguration().engageModule(moduleDesc.getName());

        sysContext.getAxisConfiguration().addService(service);
        //Utils.resolvePhases(sysContext.getEngineConfig(), service);
        ServiceContext serviceContext = sysContext.createServiceContext(service.getName());
        return serviceContext;

    }

}
