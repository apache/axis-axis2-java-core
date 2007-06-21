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
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.transport.jms.JMSListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.File;

public class UtilsJMSServer {
    private static JMSListener receiver;

    public static final int TESTING_PORT = 5555;

    public static final String FAILURE_MESSAGE = "Intentional Failure";

    public static final String REPOSITORY_JMS =
            "target/test-resources/jms-enabled-server-repository";

    private static final Log log = LogFactory.getLog(UtilsJMSServer.class);

    public static synchronized void deployService(AxisService service)
            throws AxisFault {

        receiver.getConfigurationContext().getAxisConfiguration().addService(service);

    }

    public static synchronized void unDeployService(QName service)
            throws AxisFault {
        receiver.getConfigurationContext().getAxisConfiguration().removeService(
                service.getLocalPart());
    }

    public static synchronized void start() throws Exception {
        // start JMS Listener
        File file = new File(TestingUtils.prefixBaseDirectory(REPOSITORY_JMS));
        System.out.println(file.getAbsoluteFile());
        if (!file.exists()) {
            throw new Exception("Repository directory does not exist");
        }

        ConfigurationContext configurationContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(file
                        .getAbsolutePath(), REPOSITORY_JMS + "/conf/axis2.xml");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            throw new AxisFault("Thread interuptted", e1);
        }
        receiver = new JMSListener();
        ListenerManager listenerManager = configurationContext.getListenerManager();
        TransportInDescription trsIn =
                configurationContext.getAxisConfiguration().getTransportIn(Constants.TRANSPORT_JMS);
        trsIn.setReceiver(receiver);
        if (listenerManager == null) {
            listenerManager = new ListenerManager();
            listenerManager.init(configurationContext);
        }
        listenerManager.addListener(trsIn, true);
        receiver.init(configurationContext, trsIn);
        receiver.start();
    }

    public static synchronized void stop() {
        try {
            receiver.stop();
            System.out.print("Server stopped .....");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
