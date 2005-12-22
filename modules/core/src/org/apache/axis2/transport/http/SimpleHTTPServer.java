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
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.http.server.SimpleHttpServer;
import org.apache.axis2.util.threadpool.ThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * This is a simple implementation of an HTTP server for processing
 * SOAP requests via Apache's xml-axis.  This is not intended for production
 * use.  Its intended uses are for demos, debugging, and performance
 * profiling.
 * Note this classes uses static objects to provide a thread pool, so you should
 * not use multiple instances of this class in the same JVM/classloader unless
 * you want bad things to happen at shutdown.
 */
public class SimpleHTTPServer extends TransportListener {

    /**
     * Field log
     */
    protected Log log = LogFactory.getLog(SimpleHTTPServer.class.getName());

    /**
     * Embedded commons http client based server
     */
    SimpleHttpServer embedded = null;
    int port = -1;
    private ThreadFactory threadPool = null;

    /**
     * Field systemContext
     */
    protected ConfigurationContext configurationContext;

    /**
     * Constructor SimpleHTTPServer
     */
    public SimpleHTTPServer() {
    }

    /**
     * Constructor SimpleHTTPServer
     *
     * @param systemContext
     */
    public SimpleHTTPServer(ConfigurationContext systemContext, int port) {
        this(systemContext, port, null);
    }

    /**
     * Constructor SimpleHTTPServer
     *
     * @param dir
     * @throws AxisFault
     */
    public SimpleHTTPServer(String dir, int port) throws AxisFault {
        this(dir, port, null);
    }

    /**
     * Constructor SimpleHTTPServer
     *
     * @param systemContext
     * @param pool
     */
    public SimpleHTTPServer(ConfigurationContext systemContext, int port, ThreadFactory pool) {
        // If a threadPool is not passed-in the threadpool
        // from the ConfigurationContext
        // is used. This is a bit tricky, and might cause a
        // thread lock. So use with
        // caution
        if (pool == null) {
            pool = systemContext.getThreadPool();
        }

        this.configurationContext = systemContext;
        this.port = port;
        this.threadPool = pool;
    }

    /**
     * Constructor SimpleHTTPServer
     *
     * @param dir
     * @param pool
     * @throws AxisFault
     */
    public SimpleHTTPServer(String dir, int port, ThreadFactory pool) throws AxisFault {
        try {
            this.port = port;

            ConfigurationContextFactory erfac = new ConfigurationContextFactory();

            this.configurationContext = erfac.buildConfigurationContext(dir);

            // If a thread pool is not passed the thread pool from the config context
            // is used. If one is passed it is set on the config context.
            if (pool == null) {
                pool = this.configurationContext.getThreadPool();
            } else {
                this.configurationContext.setThreadPool(pool);
            }

            this.threadPool = pool;
            Thread.sleep(2000);
        } catch (Exception e1) {
            throw new AxisFault(e1);
        }
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
        if (args.length != 2) {
            System.out.println("SimpleHTTPServer repositoryLocation port");
            System.exit(1);
        }

        SimpleHTTPServer receiver = new SimpleHTTPServer(args[0], Integer.parseInt(args[1]));

        System.out.println("starting SimpleHTTPServer in port " + args[1]
                + " using the repository " + new File(args[0]).getAbsolutePath());

        try {
            System.out.println("[Axis2] Using the Repository "
                    + new File(args[0]).getAbsolutePath());
            System.out.println("[Axis2] Starting the SimpleHTTPServer on port " + args[1]);
            receiver.start();
            System.out.println("[Axis2] SimpleHTTPServer started");
            System.in.read();
        } finally {
            receiver.stop();
        }
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
        log.info("stop called");

        if (embedded != null) {
            embedded.destroy();
        }

        log.info("Simple Axis Server Quits");
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
     * Returns the ip address to be used for the replyto epr
     * CAUTION:
     * This will simply go though the list of available network
     * interfaces and will return the final address of the final interface
     * available in the list. This workes fine for the simple cases where
     * 1.) there's only the loopback interface, where the ip is 127.0.0.1
     * 2.) there's an additional interface availbale which is used to
     * access an external network and has only one ip assigned to it.
     * <p/>
     * TODO:
     * - Improve this logic to genaralize it a bit more
     * - Obtain the ip to be used here from the Call API
     *
     * @return
     * @throws AxisFault
     */
    private String getIpAddress() throws AxisFault {
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            String address = null;

            while (e.hasMoreElements()) {
                NetworkInterface netface = (NetworkInterface) e.nextElement();
                Enumeration addresses = netface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress ip = (InetAddress) addresses.nextElement();

                    // the last available ip address will be returned
                    address = ip.getHostAddress();
                }
            }

            return address;
        } catch (SocketException e) {
            throw new AxisFault(e);
        }
    }

    /**
     * replyToEPR
     *
     * @param serviceName
     * @return an EndpointReference
     * @see org.apache.axis2.transport.TransportListener#getReplyToEPR(String)
     */
    public EndpointReference getReplyToEPR(String serviceName) throws AxisFault {
        String hostAddress = getIpAddress();

        return new EndpointReference("http://" + hostAddress + ":" + (embedded.getLocalPort())
                + "/axis2/services/" + serviceName);
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
}
