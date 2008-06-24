/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2.transport.tcp;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.util.Utils;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.http.server.HttpUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Class TCPServer
 */
public class TCPServer implements Runnable, TransportListener {
    private int port = 8000;
    private boolean started = false;
    private static final Log log = LogFactory.getLog(TCPServer.class);
    private ConfigurationContext configContext;
    private ServerSocket serversocket;
    private String hostAddress = null;
    private String contextPath;

    public TCPServer() {
    }

    public TCPServer(int port, ConfigurationContext configContext) throws AxisFault {
        try {
            this.configContext = configContext;
            serversocket = new ServerSocket(port);

            ListenerManager listenerManager = configContext.getListenerManager();
            TransportInDescription trsIn = new TransportInDescription(Constants.TRANSPORT_TCP);
            trsIn.setReceiver(this);
            if (listenerManager == null) {
                listenerManager = new ListenerManager();
                listenerManager.init(configContext);
            }
            listenerManager.addListener(trsIn, true);
            contextPath = configContext.getServiceContextPath();

        } catch (IOException e1) {
            throw AxisFault.makeFault(e1);
        }
    }

    public TCPServer(int port, String dir) throws AxisFault {
        this(port, ConfigurationContextFactory.createConfigurationContextFromFileSystem(dir, null));
    }

    public void init(ConfigurationContext axisConf, TransportInDescription transprtIn)
            throws AxisFault {
        this.configContext = axisConf;

        Parameter param = transprtIn.getParameter(PARAM_PORT);

        if (param != null) {
            this.port = Integer.parseInt((String) param.getValue());
        }
        param = transprtIn.getParameter(HOST_ADDRESS);
        if (param != null) {
            hostAddress = ((String) param.getValue()).trim();
        }
        contextPath = configContext.getServiceContextPath();
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
            Runtime.getRuntime().addShutdownHook(new Thread(tcpServer));
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
            serversocket = openSocket(port);
        }
        started = true;
        this.configContext.getThreadPool().execute(this);
    }


    /**
     * Controls the number of server sockets kept open.
     */
    public ServerSocket openSocket(int port) throws AxisFault {
        for (int i = 0; i < 5; i++) {
            try {
                return new ServerSocket(port + i);
            } catch (IOException e) {
                // What I'm gonna do here. Try again.
            }
        }

        throw new AxisFault(Messages.getMessage("failedToOpenSocket"));
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
            throw AxisFault.makeFault(e);
        }
    }

    public ConfigurationContext getConfigurationContext() {
        return this.configContext;
    }

    /**
     * I fthe hostAddress parameter is present in axis2.xml then the EPR will be
     * created by taking the hostAddres into account
     * (non-Javadoc)
     *
     * @see org.apache.axis2.transport.TransportListener#getEPRForService(String, String)
     */
    public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
        EndpointReference[] epRsForService = getEPRsForService(serviceName, ip);
        return epRsForService != null ? epRsForService[0] : null;
    }

    public EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault {
        //if host address is present
        if (hostAddress != null) {
            if (serversocket != null) {
                // todo this has to fix
                return new EndpointReference[]{
                        new EndpointReference(hostAddress + "/" + contextPath + serviceName)};
            } else {
                log.debug("Unable to generate EPR for the transport tcp");
                return null;
            }
        }
        if (ip == null) {
            try {
                ip = Utils.getIpAddress(configContext.getAxisConfiguration());
            } catch (SocketException e) {
                throw AxisFault.makeFault(e);
            }
        }
        if (serversocket != null) {
            // todo this has to fix
            return new EndpointReference[]{
                    new EndpointReference("tcp://" + ip + ":" + (serversocket.getLocalPort())
                                          + "/" + contextPath + "/" + serviceName)};
        } else {
            log.debug("Unable to generate EPR for the transport tcp");
            return null;
        }
    }

    public SessionContext getSessionContext(MessageContext messageContext) {
        return null;
    }

    public void destroy() {
        this.configContext = null;
    }
}
