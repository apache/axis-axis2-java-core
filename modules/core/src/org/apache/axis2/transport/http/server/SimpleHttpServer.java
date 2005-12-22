/*
* $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/server/SimpleHttpServer.java,v 1.15 2004/12/11 22:35:26 olegk Exp $
* $Revision: 155418 $
* $Date: 2005-02-26 08:01:52 -0500 (Sat, 26 Feb 2005) $
*
* ====================================================================
*
*  Copyright 1999-2004 The Apache Software Foundation
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation.  For more
* information on the Apache Software Foundation, please see
* <http://www.apache.org/>.
*
*/


package org.apache.axis2.transport.http.server;

import org.apache.axis2.util.threadpool.ThreadFactory;
import org.apache.axis2.util.threadpool.ThreadPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple, but extensible HTTP server, mostly for testing purposes.
 */
public class SimpleHttpServer implements Runnable {
    private static final Log LOG = LogFactory.getLog(SimpleHttpServer.class);
    private String testname = "Simple test";
    private ServerSocket listener = null;
    private long count = 0;
    private ThreadFactory threadPool = null;
    private boolean stopped = false;
    private HttpRequestHandler requestHandler = null;
    private SimpleConnSet connectionsPool = new SimpleConnSet();
    private Thread t;
    private ThreadGroup tg;

    /**
     * Creates a new HTTP server instance, using an arbitrary free TCP port
     *
     * @throws IOException if anything goes wrong during initialization
     */
    public SimpleHttpServer() throws IOException {
        this(null, 0, null);
    }

    /**
     * Creates a new HTTP server instance, using the specified TCP port
     *
     * @param port Desired TCP port
     * @throws IOException if anything goes wrong during initialization
     */
    public SimpleHttpServer(int port) throws IOException {
        this(null, port, null);
    }

    /**
     * Creates a new HTTP server instance, using the specified TCP port
     *
     * @param port       Desired TCP port
     * @param threadPool ThreadPool to be used.
     * @throws IOException if anything goes wrong during initialization
     */
    public SimpleHttpServer(int port, ThreadFactory threadPool) throws IOException {
        this(null, port, threadPool);
    }

    /**
     * Creates a new HTTP server instance, using the specified socket
     * factory and the TCP port
     *
     * @param port Desired TCP port
     * @throws IOException if anything goes wrong during initialization
     */
    public SimpleHttpServer(SimpleSocketFactory socketfactory, int port) throws IOException {
        this(socketfactory, port, null);
    }

    /**
     * Creates a new HTTP server instance, using the specified socket
     * factory and the TCP port that uses the given ThreadPool. If a
     * ThreadPool is not given then a new default axis2 ThreadPool will be
     * used.
     *
     * @param port       Desired TCP port
     * @param threadPool ThreadPool to be used inside the SimpleHttpServer. The
     *                   threadPool object that is provided needs to implement
     *                   tp.execute(Runnable r)
     * @throws IOException if anything goes wrong during initialization
     */
    public SimpleHttpServer(SimpleSocketFactory socketfactory, int port, ThreadFactory threadPool)
            throws IOException {
        if (socketfactory == null) {
            socketfactory = new SimplePlainSocketFactory();
        }

        if (threadPool == null) {
            threadPool = new ThreadPool();
        }

        this.threadPool = threadPool;
        listener = socketfactory.createServerSocket(port);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting test HTTP server on port " + getLocalPort());
        }

        this.threadPool.execute(this);
    }

    /**
     * Stops this HTTP server instance.
     */
    public synchronized void destroy() {
        if (stopped) {
            return;
        }

        this.stopped = true;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Stopping test HTTP server on port " + getLocalPort());
        }

        // tg.interrupt();
        // threadPool.doStop();

        if (listener != null) {
            try {
                listener.close();
            } catch (IOException e) {
            }
        }

        this.connectionsPool.shutdown();
    }

    public void run() {
        try {
            while (!this.stopped && !Thread.interrupted()) {
                Socket socket = listener.accept();

                try {
                    if (this.requestHandler == null) {
                        socket.close();

                        break;
                    }

                    SimpleHttpServerConnection conn = new SimpleHttpServerConnection(socket);

                    this.connectionsPool.addConnection(conn);
                    this.threadPool.execute(new SimpleConnectionThread(this.testname + " thread "
                            + this.count, conn, this.connectionsPool, this.requestHandler));
                } catch (IOException e) {
                    LOG.debug("I/O error: " + e.getMessage());
                }

                this.count++;
                Thread.sleep(100);
            }
        } catch (InterruptedException accept) {
        }
        catch (IOException e) {
            if (!stopped) {
                LOG.debug("I/O error: " + e.getMessage());
            }
        } finally {
            destroy();
        }
    }

    /**
     * Returns the IP address that this HTTP server instance is bound to.
     *
     * @return String representation of the IP address or <code>null</code> if not running
     */
    public String getLocalAddress() {
        InetAddress address = listener.getInetAddress();

        // Ugly work-around for older JDKs
        byte[] octets = address.getAddress();

        if ((octets[0] == 0) && (octets[1] == 0) && (octets[2] == 0) && (octets[3] == 0)) {
            return "localhost";
        } else {
            return address.getHostAddress();
        }
    }

    /**
     * Returns the TCP port that this HTTP server instance is bound to.
     *
     * @return TCP port, or -1 if not running
     */
    public int getLocalPort() {
        return listener.getLocalPort();
    }

    /**
     * Returns the currently used HttpRequestHandler by this SimpleHttpServer
     *
     * @return The used HttpRequestHandler, or null.
     */
    public HttpRequestHandler getRequestHandler() {
        return requestHandler;
    }

    public String getTestname() {
        return this.testname;
    }

    /**
     * Checks if this HTTP server instance is running.
     *
     * @return true/false
     */
    public boolean isRunning() {
        if (t == null) {
            return false;
        }

        return t.isAlive();
    }

    public void setHttpService(HttpService service) {
        setRequestHandler(new HttpServiceHandler(service));
    }

    /**
     * Sets the HttpRequestHandler to be used for this SimpleHttpServer.
     *
     * @param rh Request handler to be used, or null to disable.
     */
    public void setRequestHandler(HttpRequestHandler rh) {
        this.requestHandler = rh;
    }

    public void setTestname(final String testname) {
        this.testname = testname;
    }
}
