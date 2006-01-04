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
package org.apache.axis2.saaj.integration;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.transport.http.SimpleHTTPServer;

import javax.xml.namespace.QName;
import java.io.File;

/**
 * 
 */
public class UtilServer {
    private static int count = 0;

    private static SimpleHTTPServer receiver;

    public static final int TESTING_PORT = 5555;

    private static final String FAILURE_MESSAGE = "Intentional Failure";

    public static synchronized void deployService(AxisService service) throws AxisFault {
        receiver.getConfigurationContext().getAxisConfiguration().addService(service);
    }

    public static synchronized void unDeployService(QName service) throws AxisFault {
        receiver.getConfigurationContext().getAxisConfiguration().
                removeService(service.getLocalPart());
    }

    public static synchronized void unDeployClientService() throws AxisFault {
        if (receiver.getConfigurationContext().getAxisConfiguration() != null) {
            receiver.getConfigurationContext().getAxisConfiguration()
                    .removeService("AnonymousService");
        }
    }

    public static synchronized void start() throws Exception {
        start(org.apache.axis2.Constants.TESTING_REPOSITORY);
    }

    public static synchronized void start(String repository) throws Exception {
        if (count == 0) {
            ConfigurationContext er = getNewConfigurationContext(repository);

            receiver = new SimpleHTTPServer(er, TESTING_PORT);

            receiver.start();
            System.out.print("Server started on port " + TESTING_PORT + ".....");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                throw new AxisFault("Thread interupted", e1);
            }

        }
        count++;
    }

    public static ConfigurationContext getNewConfigurationContext(String repository)
            throws Exception {
        ConfigurationContextFactory erfac = new ConfigurationContextFactory();
        File file = new File(repository);
        if (!file.exists()) {
            throw new Exception("repository directory "
                                + file.getAbsolutePath() + " does not exists");
        }
        return erfac.createConfigurationContextFromFileSystem(file.getAbsolutePath());
    }

    public static synchronized void stop() {
        if (count == 1) {
            receiver.stop();
            while (receiver.isRunning()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }
            }
            count = 0;
            // tp.doStop();
            System.out.print("Server stopped .....");
        } else {
            count--;
        }
    }

    public static ConfigurationContext getConfigurationContext() {
        return receiver.getConfigurationContext();
    }

    public static ServiceContext createAdressedEnabledClientSide(
            AxisService service) throws AxisFault {
        DeploymentEngine deploymentEngine = new DeploymentEngine();
        File file = new File(org.apache.axis2.Constants.TESTING_REPOSITORY
                             + "/modules/addressing.mar");
        TestCase.assertTrue(file.exists());

        ConfigurationContextFactory efac = new ConfigurationContextFactory();
        ConfigurationContext configContext = efac
                .createConfigurationContextFromFileSystem("target/test-resources/integrationRepo");
        ModuleDescription moduleDesc = deploymentEngine.buildModule(file,
                                                                    configContext.getAxisConfiguration());
        configContext.getAxisConfiguration().addModule(moduleDesc);

        configContext.getAxisConfiguration().addService(service);

        return new ServiceGroupContext(configContext, service.getParent())
                .getServiceContext(service);
    }

    public static ConfigurationContext createClientConfigurationContext() throws AxisFault {
        File file = new File(org.apache.axis2.Constants.TESTING_REPOSITORY
                             + "/modules/addressing.mar");
        TestCase.assertTrue(file.exists());
        DeploymentEngine deploymentEngine = new DeploymentEngine();

        ConfigurationContextFactory efac = new ConfigurationContextFactory();
        ConfigurationContext configContext = efac .createConfigurationContextFromFileSystem("target/test-resources/integrationRepo");
        ModuleDescription moduleDesc = deploymentEngine.buildModule(file,
                                                                    configContext.getAxisConfiguration());
        configContext.getAxisConfiguration().addModule(moduleDesc);
        configContext.getAxisConfiguration().engageModule(new QName("addressing"));
        return configContext;
    }

    public static ServiceContext createAdressedEnabledClientSide(
            AxisService service, String clientHome) throws AxisFault {
        DeploymentEngine deploymentEngine = new DeploymentEngine();
        File file = new File(org.apache.axis2.Constants.TESTING_REPOSITORY
                             + "/modules/addressing.mar");
        TestCase.assertTrue(file.exists());

        ConfigurationContextFactory efac = new ConfigurationContextFactory();
        ConfigurationContext configContext = efac
                .createConfigurationContextFromFileSystem(clientHome);
        ModuleDescription moduleDesc = deploymentEngine.buildModule(file,
                                                                    configContext.getAxisConfiguration());

        configContext.getAxisConfiguration().addModule(moduleDesc);
        // sysContext.getAxisConfiguration().engageModule(moduleDesc.getName());

        configContext.getAxisConfiguration().addService(service);

        return new ServiceGroupContext(configContext, service.getParent())
                .getServiceContext(service);
    }
}
