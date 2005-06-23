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
package org.apache.axis.transport.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.xml.namespace.QName;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.ListenerManager;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.ConfigurationContextFactory;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.Parameter;
import org.apache.axis.description.TransportInDescription;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.transport.TransportListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a simple implementation of an HTTP server for processing
 * SOAP requests via Apache's xml-axis.  This is not intended for production
 * use.  Its intended uses are for demos, debugging, and performance
 * profiling.
 * Note this classes uses static objects to provide a thread pool, so you should
 * not use multiple instances of this class in the same JVM/classloader unless
 * you want bad things to happen at shutdown.
 */
public class SimpleHTTPServer extends TransportListener implements Runnable {
    /**
     * Field log
     */
    protected Log log = LogFactory.getLog(SimpleHTTPServer.class.getName());

    /**
     * Field systemContext
     */
    protected ConfigurationContext configurationContext;

    /**
     * Field serverSocket
     */
    protected ServerSocket serverSocket;

    /**
     * Field socket
     */
    protected Socket socket;

    /**
     * are we stopped?
     * latch to true if stop() is called
     */
    private boolean stopped = false;

    public SimpleHTTPServer() {
    }

    /**
     * Constructor SimpleHTTPServer
     *
     * @param systemContext
     */
    public SimpleHTTPServer(ConfigurationContext systemContext, ServerSocket serverSoc) {
        this.configurationContext = systemContext;
        this.serverSocket = serverSoc;
    }

    /**
     * Constructor SimpleHTTPServer
     *
     * @param dir
     * @throws AxisFault
     */
    public SimpleHTTPServer(String dir, ServerSocket serverSoc) throws AxisFault {
        try {
            this.serverSocket = serverSoc;
            // Class erClass = Class.forName("org.apache.axis.deployment.EngineContextFactoryImpl");
            ConfigurationContextFactory erfac = new ConfigurationContextFactory();
            this.configurationContext = erfac.buildConfigurationContext(dir);
            Thread.sleep(2000);
        } catch (Exception e1) {
            throw new AxisFault("Thread interuptted", e1);
        }
    }

    /**
     * stop the server if not already told to.
     *
     * @throws Throwable
     */
    protected void finalize() throws Throwable {
        stop();
        super.finalize();
    }

    /**
     * Accept requests from a given TCP port and send them through the
     * Axis engine for processing.
     */
    public void run() {
        try {
            while (!stopped) {
                try {

                    // Accept and process requests from the socket
                    try {
                        socket = serverSocket.accept();
                    } catch (java.io.InterruptedIOException iie) {
                    } catch (Exception e) {
                        log.debug(e);
                        break;
                    }
                    if (socket != null) {
                        if (configurationContext == null) {
                            throw new AxisFault("Engine Must be null");
                        }

                        InputStream inStream = socket.getInputStream();

                        

                        TransportOutDescription transportOut =
                            configurationContext.getAxisConfiguration().getTransportOut(
                                new QName(Constants.TRANSPORT_HTTP));
                        MessageContext msgContext =
                            new MessageContext(
                                configurationContext,
                                configurationContext.getAxisConfiguration().getTransportIn(
                                    new QName(Constants.TRANSPORT_HTTP)),
                                transportOut);
                        msgContext.setServerSide(true);

                        // We do not have any Addressing Headers to put
                        // let us put the information about incoming transport
                        OutputStream out = socket.getOutputStream();
                        msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);
                        msgContext.setProperty(MessageContext.TRANSPORT_IN, inStream);
                        HTTPTransportReceiver reciver = new HTTPTransportReceiver();

                        /*
                         * If the request is a GET request then  
                         * process the request and send out HTML
                         * if not get the soap message and process it
                         */

                        //Handle POST Request
                        SOAPEnvelope envelope =
                            reciver.checkForMessage(msgContext, configurationContext);

                        if (envelope != null) {
                            msgContext.setEnvelope(envelope);

                            AxisEngine engine = new AxisEngine(configurationContext);
                            engine.receive(msgContext);

                            Object contextWritten =
                                msgContext.getProperty(Constants.RESPONSE_WRITTEN);
                            if (contextWritten == null
                                || !Constants.VALUE_TRUE.equals(contextWritten)) {
                                out.write(new String(HTTPConstants.NOCONTENT).getBytes());
                                out.close();
                            }

                        }

                    }
                } catch (Throwable e) {
                    log.error(e);
                    e.printStackTrace();
                } finally {
                    if (socket != null) {
                        this.socket.close();
                        this.socket = null;
                    }
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
        stop();
        log.info("Simple Axis Server Quit");
    }

    /**
     * Obtain the serverSocket that that SimpleAxisServer is listening on.
     *
     * @return
     */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * Set the serverSocket this server should listen on.
     * (note : changing this will not affect a running server, but if you
     * stop() and then start() the server, the new socket will be used).
     *
     * @param serverSocket
     */
    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * Start this server as a NON-daemon.
     *
     * @throws Exception
     */
    public void start() throws AxisFault {
        Thread newThread = new Thread(this);
        newThread.start();
    }

    /**
     * Stop this server. Can be called safely if the system is already stopped,
     * or if it was never started.
     * This will interrupt any pending accept().
     */
    public void stop() {
        log.info("stop called");

        // recognise use before we are live
        if (stopped) {
            return;
        }

        /*
         * Close the server socket cleanly, but avoid fresh accepts while
         * the socket is closing.
         */
        stopped = true;
        try {
            if (serverSocket != null) {
                serverSocket.close();

                // while(socket != null){
                // try {
                // //make sure all sockets closed by the time
                // //else we got in to lot of trouble testing
                // Thread.sleep(1000);
                // } catch (InterruptedException e1) {
                // log.error(e1);
                // }
                // }
            }
        } catch (IOException e) {
            log.info(e);
        } finally {
            serverSocket = null;
        }
        log.info("Simple Axis Server Quits");
    }

    /**
     * Method getSystemContext
     *
     * @return
     */
    public ConfigurationContext getSystemContext() {
        return configurationContext;
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
        }
        ServerSocket serverSoc = null;
        serverSoc = new ServerSocket(Integer.parseInt(args[1]));
        SimpleHTTPServer reciver = new SimpleHTTPServer(args[0], serverSoc);
        System.out.println(
            "starting SimpleHTTPServer in port "
                + args[1]
                + " using the repository "
                + new File(args[0]).getAbsolutePath());
        reciver.setServerSocket(serverSoc);
        Thread thread = new Thread(reciver);
        thread.setDaemon(true);
        try {
            System.out.println("[Axis2] Using the Repository " + new File(args[1]).getAbsolutePath());
            System.out.println("[Axis2] Starting the SimpleHTTPServer on port "+ args[0]);
            thread.start();
            System.in.read();
        } finally {
            reciver.stop();
        }
    }
    /* (non-Javadoc)
     * @see org.apache.axis.transport.TransportListener#replyToEPR(java.lang.String)
     */
    public EndpointReference replyToEPR(String serviceName) {
        return new EndpointReference(
            AddressingConstants.WSA_REPLY_TO,
            "http://127.0.0.1:" + (serverSocket.getLocalPort()) + "/axis/services/" + serviceName);
    }

    public void init(ConfigurationContext axisConf, TransportInDescription transprtIn)
        throws AxisFault {
        this.configurationContext = axisConf;
        Parameter param = transprtIn.getParameter(PARAM_PORT);
        if (param != null) {
            int port = Integer.parseInt((String) param.getValue());
            serverSocket = ListenerManager.openSocket(port);
        }
    }

}
