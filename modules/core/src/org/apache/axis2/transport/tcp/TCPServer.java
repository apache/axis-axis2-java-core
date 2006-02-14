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


package org.apache.axis2.transport.tcp;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ListenerManager;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class TCPServer
 */
public class TCPServer extends TransportListener implements Runnable {
    private int port = 8000;
    private boolean started = false;
    protected Log log = LogFactory.getLog(SimpleHTTPServer.class.getName());
    private ConfigurationContext configContext;
    private ServerSocket serversocket;

    public TCPServer() {
    }

    public TCPServer(int port, ConfigurationContext configContext) throws AxisFault {
        try {
            this.configContext = configContext;
            serversocket = new ServerSocket(port);
        } catch (IOException e1) {
            throw new AxisFault(e1);
        }
    }

    public TCPServer(int port, String dir) throws AxisFault {
        try {
            this.configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(dir, null);
            Thread.sleep(3000);
            serversocket = new ServerSocket(port);
        } catch (DeploymentException e1) {
            throw new AxisFault(e1);
        } catch (InterruptedException e1) {
            throw new AxisFault(e1);
        } catch (IOException e1) {
            throw new AxisFault(e1);
        }
    }

    public void init(ConfigurationContext axisConf, TransportInDescription transprtIn)
            throws AxisFault {
        this.configContext = axisConf;

        Parameter param = transprtIn.getParameter(PARAM_PORT);

        if (param != null) {
            this.port = Integer.parseInt((String) param.getValue());
        }
    }

    public static void main(String[] args) throws AxisFault, NumberFormatException {
        if (args.length != 2) {
            System.out.println("TCPServer repositoryLocation port");
        } else {
            File repository = new File(args[0]);

            if (!repository.exists()) {
                System.out.print("Repository file does not exists .. initializing repository");
            }

            TCPServer tcpServer = new TCPServer(Integer.parseInt(args[1]),
                    repository.getAbsolutePath());

            System.out.println("[Axis2] Using the Repository " + repository.getAbsolutePath());
            System.out.println("[Axis2] Starting the TCP Server on port " + args[1]);
            tcpServer.start();
        }
    }

    public void run() {
        while (started) {
            Socket socket = null;

            try {
                socket = serversocket.accept();
            } catch (java.io.InterruptedIOException iie) {
            }
            catch (Exception e) {
                log.debug(e);

                break;
            }

            if (socket != null) {
                configContext.getThreadPool().execute(new TCPWorker(configContext, socket));
            }
        }
    }

    public synchronized void start() throws AxisFault {
        if (serversocket == null) {
            serversocket = ListenerManager.openSocket(port);
        }

        started = true;
        this.configContext.getThreadPool().execute(this);
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.transport.TransportListener#stop()
     */
    public void stop() throws AxisFault {
        try {
            this.serversocket.close();
            started = false;
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    public ConfigurationContext getConfigurationContext() {
        return this.configContext;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.transport.TransportListener#replyToEPR(java.lang.String)
     */
    public EndpointReference getEPRForService(String serviceName) throws AxisFault {

        // todo this has to fix
        return new EndpointReference("tcp://127.0.0.1:" + (serversocket.getLocalPort())
                + "/axis2/services/" + serviceName);
    }
}
