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
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ListenerManager;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.axis2.util.Utils;
import org.apache.axis2.util.threadpool.ThreadPool;

import javax.xml.namespace.QName;
import java.io.File;

public class UtilServer {
    private static int count = 0;
    private static SimpleHTTPServer receiver;
    public static final int TESTING_PORT = 5555;
    public static final String FAILURE_MESSAGE = "Intentional Failure";
    
    private static ThreadPool tp = null;

    public static synchronized void deployService(AxisService service) throws AxisFault {
        receiver.getSystemContext().getAxisConfiguration().addService(service);
        Utils.resolvePhases(receiver.getSystemContext().getAxisConfiguration(),
                service);
//        ServiceGroupContext serviceGroupContext = service.getParent().getServiceGroupContext(receiver.getSystemContext());
    }

    public static synchronized void unDeployService(QName service) throws AxisFault {
        receiver.getSystemContext().getAxisConfiguration().removeService(
                service.getLocalPart());
    }

    public static synchronized void unDeployClientService() throws AxisFault {
        if(ListenerManager.configurationContext !=null){
            ListenerManager.configurationContext.getAxisConfiguration()
                    .removeService("AnonymousService");
        }
    }

    public static synchronized void start() throws Exception {
        start(org.apache.axis2.Constants.TESTING_REPOSITORY);
    }

    public static synchronized void start(String repositry) throws Exception {
        if (count == 0) {
        	tp = new ThreadPool();
            ConfigurationContext er = getNewConfigurationContext(repositry);

            receiver = new SimpleHTTPServer(er, Constants.TESTING_PORT);

            try {
                receiver.start();
                System.out.print(
                        "Server started on port " + Constants.TESTING_PORT +
                                ".....");
            } finally {

            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                throw new AxisFault("Thread interuptted", e1);
            }

        }
        count++;
    }

    public static ConfigurationContext getNewConfigurationContext(String repositry) throws Exception {
        ConfigurationContextFactory erfac = new ConfigurationContextFactory();
        File file = new File(repositry);
        if (!file.exists()) {
            throw new Exception(
                    "repository directory " + file.getAbsolutePath() +
                            " does not exists");
        }
        return erfac.buildConfigurationContext(
                file.getAbsolutePath());
    }

    public static synchronized void stop() {
        if (count == 1) {
            receiver.stop();
            while(receiver.isRunning()){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }
            }
            count = 0;
            //tp.doStop();
            System.out.print("Server stopped .....");
        } else {
            count--;
        }
    }

    public static ConfigurationContext getConfigurationContext() {
        return receiver.getSystemContext();
    }

    public static ServiceContext createAdressedEnabledClientSide(
            AxisService service)
            throws AxisFault {
        DeploymentEngine deploymentEngine = new DeploymentEngine();
        File file = new File(
                org.apache.axis2.Constants.TESTING_REPOSITORY +
                        "/modules/addressing.mar");
        TestCase.assertTrue(file.exists());

        ConfigurationContextFactory efac = new ConfigurationContextFactory();
        ConfigurationContext sysContext =
                efac.buildClientConfigurationContext("target/test-resources/intregrationRepo");
         ModuleDescription moduleDesc = deploymentEngine.buildModule(file,sysContext.getAxisConfiguration());
        sysContext.getAxisConfiguration().addModule(moduleDesc);
        //sysContext.getAxisConfiguration().engageModule(moduleDesc.getName());

        sysContext.getAxisConfiguration().addService(service);

        return service.getParent().getServiceGroupContext(sysContext
        ).getServiceContext(service.getName().getLocalPart());
    }

    public static ServiceContext createAdressedEnabledClientSide(
            AxisService service, String clientHome)
            throws AxisFault {
        DeploymentEngine deploymentEngine = new DeploymentEngine();
        File file = new File(
                org.apache.axis2.Constants.TESTING_REPOSITORY +
                        "/modules/addressing.mar");
        TestCase.assertTrue(file.exists());

        ConfigurationContextFactory efac = new ConfigurationContextFactory();
        ConfigurationContext sysContext =
                efac.buildClientConfigurationContext(clientHome);
        ModuleDescription moduleDesc = deploymentEngine.buildModule(file,sysContext.getAxisConfiguration());

        sysContext.getAxisConfiguration().addModule(moduleDesc);
        //sysContext.getAxisConfiguration().engageModule(moduleDesc.getName());

        sysContext.getAxisConfiguration().addService(service);

        return service.getParent().getServiceGroupContext(sysContext
        ).getServiceContext(service.getName().getLocalPart());
    }

}
