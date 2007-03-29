/*
 * $HeadURL:https://svn.apache.org/repos/asf/jakarta/httpcomponents/trunk/coyote-httpconnector/src/java/org/apache/http/tcconnector/impl/DefaultHttpConnectionManager.java $
 * $Revision:379772 $
 * $Date:2006-02-22 14:52:29 +0100 (Wed, 22 Feb 2006) $
 *
 * ====================================================================
 *
 *  Copyright 1999-2006 The Apache Software Foundation
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

import edu.emory.mathcs.backport.java.util.concurrent.Executor;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DefaultHttpConnectionManager implements HttpConnectionManager {

    private static Log LOG = LogFactory.getLog(DefaultHttpConnectionManager.class);

    private final ConfigurationContext configurationContext;
    private final Executor executor;
    private final WorkerFactory workerfactory;
    private final HttpParams params;
    private final List processors;
    private final SessionManager sessionManager;

    private HttpFactory httpFactory = null;

    public DefaultHttpConnectionManager(
            final ConfigurationContext configurationContext,
            final Executor executor,
            final WorkerFactory workerfactory,
            final HttpParams params) {
        super();
        if (configurationContext == null) {
            throw new IllegalArgumentException("Configuration context may not be null");
        }
        if (executor == null) {
            throw new IllegalArgumentException("Executor may not be null");
        }
        if (workerfactory == null) {
            throw new IllegalArgumentException("Worker factory may not be null");
        }
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        this.configurationContext = configurationContext;
        this.sessionManager = new SessionManager();
        this.executor = executor;
        this.workerfactory = workerfactory;
        this.params = params;
        this.processors = new LinkedList();
    }

    public DefaultHttpConnectionManager(
            final ConfigurationContext configurationContext,
            final Executor executor,
            final WorkerFactory workerfactory,
            final HttpParams params,
            final HttpFactory httpFactory) {
        this(configurationContext, executor, workerfactory, params);
        this.httpFactory = httpFactory;
    }


    private synchronized void cleanup() {
        for (Iterator i = this.processors.iterator(); i.hasNext();) {
            IOProcessor processor = (IOProcessor) i.next();
            if (processor.isDestroyed()) {
                i.remove();
            }
        }
    }

    private synchronized void addProcessor(final IOProcessor processor) {
        if (processor == null) {
            return;
        }
        this.processors.add(processor);
    }

    private synchronized void removeProcessor(final IOProcessor processor) {
        if (processor == null) {
            return;
        }
        this.processors.remove(processor);
    }

    public void process(final HttpServerConnection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("HTTP connection may not be null");
        }
        // Evict destroyed processors
        cleanup();

        // Assemble new Axis HTTP service
        HttpProcessor httpProcessor;
        ConnectionReuseStrategy connStrategy;
        HttpResponseFactory responseFactory;

        if (httpFactory != null) {
            httpProcessor = httpFactory.newHttpProcessor();
            connStrategy = httpFactory.newConnStrategy();
            responseFactory = httpFactory.newResponseFactory();
        } else {
            BasicHttpProcessor p = new BasicHttpProcessor();
            p.addInterceptor(new RequestSessionCookie());
            p.addInterceptor(new ResponseDate());
            p.addInterceptor(new ResponseServer());
            p.addInterceptor(new ResponseContent());
            p.addInterceptor(new ResponseConnControl());
            p.addInterceptor(new ResponseSessionCookie());
            httpProcessor = new LoggingProcessorDecorator(p);
            connStrategy = new DefaultConnectionReuseStrategy();
            responseFactory = new DefaultHttpResponseFactory();
        }

        AxisHttpService httpService = new AxisHttpService(
                httpProcessor,
                connStrategy,
                responseFactory,
                this.configurationContext,
                this.sessionManager,
                this.workerfactory.newWorker());
        httpService.setParams(this.params);

        // Create I/O processor to execute HTTP service
        IOProcessorCallback callback = new IOProcessorCallback() {

            public void completed(final IOProcessor processor) {
                removeProcessor(processor);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(processor + " terminated");
                }
            }

        };
        IOProcessor processor = new HttpServiceProcessor(
                httpService,
                conn,
                callback);

        addProcessor(processor);
        this.executor.execute(processor);
    }

    public synchronized void shutdown() {
        for (int i = 0; i < this.processors.size(); i++) {
            IOProcessor processor = (IOProcessor) this.processors.get(i);
            processor.destroy();
        }
        this.processors.clear();
    }

}
