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

import javax.xml.namespace.QName;

import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.ConfigurationContextFactory;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.tcp.TCPServer;

public class UtilsTCPServer {
    private static int count = 0;
    private static TCPServer reciver;

    private static ConfigurationContext configurationContext;
    public static final int TESTING_PORT = 5555;
    public static final String FAILURE_MESSAGE = "Intentional Faliure";
    public static final String TESTING_REPOSITORY = "target/test-resources/samples";
    //public static final String TESTING_REPOSITORY = "modules/samples/target/test-resources/samples";

    public static synchronized void deployService(ServiceDescription service) throws AxisFault {
        configurationContext.getEngineConfig().addService(service);

    }

    public static synchronized void unDeployService(QName service) throws AxisFault {
        configurationContext.getEngineConfig().removeService(service);
    }

    public static synchronized void start() throws Exception {
        if (count == 0) {

            //start tcp server

            ConfigurationContextFactory erfac = new ConfigurationContextFactory();
            File file = new File(TESTING_REPOSITORY);
            if (!file.exists()) {
                throw new Exception("repository directory does not exists");
            }
            configurationContext = erfac.buildEngineContext(file.getAbsolutePath());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                throw new AxisFault("Thread interuptted", e1);
            }

            reciver = new TCPServer(UtilServer.TESTING_PORT, configurationContext);
            reciver.start();

        }
        count++;
    }

    public static synchronized void stop() {
        try {
            if (count == 1) {
                reciver.stop();
                count = 0;
                System.out.print("Server stopped .....");
            } else {
                count--;
            }
        } catch (AxisFault e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }
}
