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


package org.apache.axis2.transport.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.http.server.SimpleHttpServer;
import org.apache.axis2.transport.http.server.SimpleHttpServerConnection;
import org.apache.axis2.util.OptionsParser;
import org.apache.axis2.util.threadpool.ThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;

/**
 * This is a simple implementation of an HTTP server for processing
 * SOAP requests via Apache's xml-axis2.  This is not intended for production
 * use.  Its intended uses are for demos, debugging, and performance
 * profiling.
 * Note this classes uses static objects to provide a thread pool, so you should
 * not use multiple instances of this class in the same JVM/classloader unless
 * you want bad things to happen at shutdown.
 */
public class SimpleHTTPServer implements TransportListener {

    /**
     * Field log
     */
	private static final Log log = LogFactory.getLog(SimpleHTTPServer.class);

    /**
     * Embedded commons http client based server
     */
    SimpleHttpServer embedded = null;
    int port = -1;
    private ThreadFactory threadPool = null;

    public static int DEFAULT_PORT = 8080;
    private String hostAddress = null;

    /**
     * Field systemContext
     */
    protected ConfigurationContext configurationContext;

    public SimpleHTTPServer() {
    }

    public SimpleHTTPServer(ConfigurationContext systemContext, int port) throws AxisFault {
        this(systemContext, port, null);
    }

    /**
     * Constructor SimpleHTTPServer
     *
     * @param systemContext
     * @param pool
     */
    public SimpleHTTPServer(ConfigurationContext systemContext, int port, ThreadFactory pool) throws AxisFault {
        // If a threadPool is not passed-in the threadpool
        // from the ConfigurationContext
        // is used. This is a bit tricky, and might cause a
        // thread lock. So use with
        // caution
        this.configurationContext = systemContext;
        if (pool == null) {
            pool = this.configurationContext.getThreadPool();
        } else {
            this.configurationContext.setThreadPool(pool);
        }
        this.port = port;
        this.threadPool = pool;
        ListenerManager listenerManager = configurationContext.getListenerManager();
        TransportInDescription trsIn = new TransportInDescription(
                new QName(Constants.TRANSPORT_HTTP));
        trsIn.setReceiver(this);
        if (listenerManager == null) {
            listenerManager = new ListenerManager();
            listenerManager.init(configurationContext);
        }
        listenerManager.addListener(trsIn, true);
    }

    /**
     * init method in TransportListener
     *
     * @param axisConf
     * @param transprtIn
     * @throws AxisFault
     */
    public void init(ConfigurationContext axisConf, TransportInDescription transprtIn)
            throws AxisFault {
        try {
            this.configurationContext = axisConf;

            Parameter param = transprtIn.getParameter(PARAM_PORT);

            if (param != null) {
                this.port = Integer.parseInt((String) param.getValue());
            }
            param = transprtIn.getParameter(HOST_ADDRESS);
            if (param != null) {
                hostAddress = ((String) param.getValue()).trim();
            }
        } catch (Exception e1) {
            throw new AxisFault(e1);
        }
    }

    /**
     * Method main
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        OptionsParser optionsParser = new OptionsParser(args);

        args = optionsParser.getRemainingArgs();
        // first check if we should print usage
        if ((optionsParser.isFlagSet('?') > 0) || (optionsParser.isFlagSet('h') > 0) ||
                args == null || args.length == 0 || args.length > 2) {
            printUsage();
        }
        String paramPort = optionsParser.isValueSet('p');
        if (paramPort != null) {
            port = Integer.parseInt(paramPort);
        }
        args = optionsParser.getRemainingArgs();

        System.out.println("[SimpleHTTPServer] Starting");
        System.out.println("[SimpleHTTPServer] Using the Axis2 Repository "
                + new File(args[0]).getAbsolutePath());
        System.out.println("[SimpleHTTPServer] Listening on port " + port);
        try {
            SimpleHTTPServer receiver = new SimpleHTTPServer(
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                            args[0], null), port, null);
            Runtime.getRuntime().addShutdownHook(new ShutdownThread(receiver));
            receiver.start();
            System.out.println("[SimpleHTTPServer] Started");
        } catch (Throwable t) {
            log.fatal("Error starting SimpleHTTPServer", t);
            System.out.println("[SimpleHTTPServer] Shutting down");
        }
    }

    public static void printUsage() {
        System.out.println("Usage: SimpleHTTPServer [options] <repository>");
        System.out.println(" Opts: -? this message");
        System.out.println();
        System.out.println("       -p port to listen on (default is 8080)");
        System.exit(1);
    }


    /**
     * Start this server as a NON-daemon.
     */
    public void start() throws AxisFault {
        try {
            embedded = new SimpleHttpServer(port, this.threadPool);
            embedded.setRequestHandler(new HTTPWorker(configurationContext));
        } catch (IOException e) {
            log.error(e);
            throw new AxisFault(e);
        }
    }

    /**
     * Stop this server. Can be called safely if the system is already stopped,
     * or if it was never started.
     * This will interrupt any pending accept().
     */
    public void stop() {
        System.out.println("[SimpleHTTPServer] Stop called");
        if (embedded != null) {
            embedded.destroy();
        }
    }

    /**
     * Method getConfigurationContext
     *
     * @return the system context
     */
    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    /**
     * replyToEPR
     * If the user has given host address paramter then it gets the high priority and
     * ERP will be creatd using that
     * N:B - hostAddress should be a complte url (http://www.myApp.com/ws)
     *
     * @param serviceName
     * @param ip
     * @return an EndpointReference
     * @see org.apache.axis2.transport.TransportListener#getEPRForService(String,String)
     */
    public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
        //if host address is present
        if (hostAddress != null) {
            if (embedded != null) {
                return new EndpointReference(hostAddress + "/axis2/services/" + serviceName);
            } else {
                throw new AxisFault("Unable to generate EPR for the transport : http");
            }
        }
        //if the host address is not present
        String localAddress;
        if (ip != null) {
            localAddress = ip;
        } else {
            try {
                localAddress = SimpleHttpServerConnection.getIpAddress();
            } catch (SocketException e) {
                throw AxisFault.makeFault(e);
            }
        }
        if (embedded != null) {
            return new EndpointReference("http://" + localAddress + ":" +
                    (embedded.getLocalPort())
                    + "/axis2/services/" + serviceName);
        } else {
            throw new AxisFault("Unable to generate EPR for the transport : http");
        }
    }

    /**
     * Checks if this HTTP server instance is running.
     *
     * @return true/false
     */
    public boolean isRunning() {
        if (embedded == null) {
            return false;
        }

        return embedded.isRunning();
    }

    static class ShutdownThread extends Thread {
        private SimpleHTTPServer server = null;

        public ShutdownThread(SimpleHTTPServer server) {
            super();
            this.server = server;
        }

        public void run() {
            System.out.println("[SimpleHTTPServer] Shutting down");
            server.stop();
            System.out.println("[SimpleHTTPServer] Shutdown complete");
        }
    }
}
