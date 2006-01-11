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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.jms.JNDIVendorAdapter;
import org.apache.axis2.transport.jms.SimpleJMSListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.HashMap;

public class UtilsJMSServer {
    private static int count = 0;

    private static SimpleJMSListener receiver;

    public static final int TESTING_PORT = 5555;

    public static final String FAILURE_MESSAGE = "Intentional Failure";

    private static Log log = LogFactory.getLog(UtilsJMSServer.class);

    public static synchronized void deployService(AxisService service)
            throws AxisFault {

        receiver.getConfigurationContext().getAxisConfiguration().addService(service);

        ServiceGroupContext serviceGroupContext = new ServiceGroupContext(
                receiver.getConfigurationContext(), service.getParent());
    }

    public static synchronized void unDeployService(QName service)
            throws AxisFault {
        receiver.getConfigurationContext().getAxisConfiguration().removeService(
                service.getLocalPart());
    }

    public static synchronized void start() throws Exception {
        if (count == 0) {

            HashMap connectorMap = new HashMap();
            HashMap cfMap = new HashMap();
            String destination = "dynamicQueues/BAR";
            String username = null;
            String password = null;
            boolean doThreads = true;

            cfMap.put(JNDIVendorAdapter.CONTEXT_FACTORY,
                    "org.activemq.jndi.ActiveMQInitialContextFactory");
            cfMap.put(JNDIVendorAdapter.PROVIDER_URL, "tcp://localhost:61616");

            // start JMS server
            ConfigurationContextFactory erfac = new ConfigurationContextFactory();
            File file = new File(org.apache.axis2.Constants.TESTING_REPOSITORY);
            System.out.println(file.getAbsoluteFile());
            if (!file.exists()) {
                throw new Exception("Repository directory does not exist");
            }

            ConfigurationContext er = erfac.createConfigurationContextFromFileSystem(file
                    .getAbsolutePath());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                throw new AxisFault("Thread interuptted", e1);
            }
            er.getAxisConfiguration().engageModule(new QName("addressing"));
            receiver = new SimpleJMSListener(
                    org.apache.axis2.Constants.TESTING_REPOSITORY,
                    connectorMap, cfMap, destination, username, password,
                    doThreads);
            receiver.start();

        }
        count++;
    }

    public static synchronized void stop() {
        try {
            if (count == 1) {
                receiver.stop();
                count = 0;
                System.out.print("Server stopped .....");
            } else {
                count--;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
