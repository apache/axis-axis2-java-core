/*
* $HeadURL$
* $Revision$
* $Date$
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
*/
package org.apache.axis2.transport.http.server;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.UnsupportedHttpVersionException;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

public class DefaultHttpServiceProcessor extends HttpServiceProcessor {

    private static final Log LOG = LogFactory.getLog(DefaultHttpServiceProcessor.class);
    private static final Log HEADERLOG = LogFactory.getLog("org.apache.axis2.transport.http.server.wire");
    
    // to store session object
    private static Hashtable sessionContextTable = new Hashtable();
    
    private final ConfigurationContext configurationContext;
    private final Worker worker;
    private final IOProcessorCallback callback;
    
    public DefaultHttpServiceProcessor(
            final HttpServerConnection conn,
            final ConfigurationContext configurationContext,
            final Worker worker,
            final IOProcessorCallback callback) {
        super(conn);
        if (worker == null) {
            throw new IllegalArgumentException("Worker may not be null");
        }
        if (configurationContext == null) {
            throw new IllegalArgumentException("Configuration context may not be null");
        }
        this.configurationContext = configurationContext;
        this.worker = worker;
        this.callback = callback;
    }
    
    protected void postprocessResponse(final HttpResponse response, final HttpContext context) 
            throws IOException, HttpException {
        super.postprocessResponse(response, context);
        if (HEADERLOG.isDebugEnabled()) {
            HEADERLOG.debug("<< " + response.getStatusLine().toString());
            Header[] headers = response.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                HEADERLOG.debug("<< " + headers[i].toString());
            }
        }
    }

    protected void preprocessRequest(final HttpRequest request, final HttpContext context) 
            throws IOException, HttpException {
        super.preprocessRequest(request, context);
        if (HEADERLOG.isDebugEnabled()) {
            HEADERLOG.debug(">> " + request.getRequestLine().toString());
            Header[] headers = request.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                HEADERLOG.debug(">> " + headers[i].toString());
            }
        }
    }

    protected void doService(final HttpRequest request, final HttpResponse response) 
            throws HttpException, IOException {
        RequestLine reqline = request.getRequestLine();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Request method: " + reqline.getMethod());
            LOG.debug("Target URI: " + reqline.getUri());
        }
        
        HttpVersion ver = reqline.getHttpVersion();
        if (!ver.lessEquals(HttpVersion.HTTP_1_1)) {
            throw new UnsupportedHttpVersionException("Unsupported HTTP version: " + ver); 
        }
        
        MessageContext msgContext = new MessageContext();
        msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);
        try {
            TransportOutDescription transportOut = this.configurationContext.getAxisConfiguration()
                .getTransportOut(new QName(Constants.TRANSPORT_HTTP));
            TransportInDescription transportIn = this.configurationContext.getAxisConfiguration()
                .getTransportIn(new QName(Constants.TRANSPORT_HTTP));            
        
            String cookieID = HttpUtils.getCookieID(request);
            msgContext.setConfigurationContext(this.configurationContext);
            if (this.configurationContext.getAxisConfiguration().isManageTransportSession()) {
                SessionContext sessionContext = getSessionContext(cookieID);
                msgContext.setSessionContext(sessionContext);
            }
            msgContext.setTransportIn(transportIn);
            msgContext.setTransportOut(transportOut);
            msgContext.setServiceGroupContextId(UUIDGenerator.getUUID());
            msgContext.setServerSide(true);
            msgContext.setProperty(Constants.Configuration.TRANSPORT_IN_URL, 
                    request.getRequestLine().getUri());

            // set the transport Headers
            HashMap headerMap = new HashMap();
            Header[] headers = request.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].getName(), headers[i].getValue());
            }
            msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);

            this.worker.service(request, response, msgContext);
            setCookie(response, msgContext);
        } catch (SocketException ex) {
            // Socket is unreliable. 
            throw ex;
        } catch (HttpException ex) {
            // HTTP protocol violation. Transport is unrelaible
            throw ex;
        } catch (Throwable e) {
            try {
                AxisEngine engine = new AxisEngine(this.configurationContext);
                
                OutputBuffer outbuffer = new OutputBuffer(); 
                msgContext.setProperty(MessageContext.TRANSPORT_OUT, outbuffer.getOutputStream());
                msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, outbuffer);

                MessageContext faultContext = engine.createFaultMessageContext(msgContext, e);

                response.setStatusLine(new StatusLine(ver, 500, "Internal server error"));
                engine.sendFault(faultContext);
                response.setEntity(outbuffer);
                setCookie(response, msgContext);
            } catch (Exception ex) {
                response.setStatusLine(new StatusLine(ver, 500, "Internal server error"));
                StringEntity entity = new StringEntity(ex.getMessage());
                entity.setContentType("text/plain");
                response.setEntity(entity);
            }
        }
        
    }

    private static void setCookie(final HttpResponse response, MessageContext msgContext) {
        //TODO : provide a way to enable and diable cookies
        //setting the coolie in the out path
        Object cookieString = msgContext.getProperty(Constants.COOKIE_STRING);
        if (cookieString != null) {
            response.addHeader(new Header(HTTPConstants.HEADER_SET_COOKIE, (String) cookieString));
            response.addHeader(new Header(HTTPConstants.HEADER_SET_COOKIE2, (String) cookieString));
        }
    }

    /**
     * To get the sessioncontext , if its not there in the hashtable , new one will be created and
     * added to the list.
     *
     * @param cookieID
     * @return <code>SessionContext</code>
     */
    private synchronized SessionContext getSessionContext(String cookieID) {
        SessionContext sessionContext = null;
        if (!(cookieID == null || cookieID.trim().equals(""))) {
            sessionContext = (SessionContext) sessionContextTable.get(cookieID);
        }
        if (sessionContext == null) {
            String cookieString = UUIDGenerator.getUUID();
            sessionContext = new SessionContext(null);
            sessionContext.setCookieID(cookieString);
            sessionContextTable.put(cookieString, sessionContext);
        }
        sessionContext.touch();
        cleanupServiceGroupContexts();
        return sessionContext;
    }

    private void cleanupServiceGroupContexts() {
        synchronized (sessionContextTable) {
            long currentTime = System.currentTimeMillis();
            Iterator sgCtxtMapKeyIter = sessionContextTable.keySet().iterator();
            while (sgCtxtMapKeyIter.hasNext()) {
                String cookieID = (String) sgCtxtMapKeyIter.next();
                SessionContext sessionContext = (SessionContext) sessionContextTable.get(cookieID);
                if ((currentTime - sessionContext.getLastTouchedTime()) >
                        sessionContext.sessionContextTimeoutInterval) {
                    sgCtxtMapKeyIter.remove();
                    Iterator serviceGroupContext = sessionContext.getServiceGroupContext();
                    if (serviceGroupContext != null) {
                        while (serviceGroupContext.hasNext()) {
                            ServiceGroupContext groupContext = (ServiceGroupContext) serviceGroupContext.next();
                            cleanupServiceContextes(groupContext);
                        }
                    }
                }
            }
        }
    }

    private void cleanupServiceContextes(ServiceGroupContext serviceGroupContext) {
        Iterator serviceContecxtes = serviceGroupContext.getServiceContexts();
        while (serviceContecxtes.hasNext()) {
            ServiceContext serviceContext = (ServiceContext) serviceContecxtes.next();
            try {
                DependencyManager.destroyServiceClass(serviceContext);
            } catch (AxisFault axisFault) {
                LOG.info(axisFault.getMessage());
            }
        }
    }
    
    protected void logIOException(final IOException ex) {
        if (ex instanceof SocketTimeoutException) {
            LOG.debug(ex.getMessage());
        } else if (ex instanceof SocketException) {
            LOG.debug(ex.getMessage());
        }
        else {
            LOG.warn(ex.getMessage(), ex);
        }
    }

    protected void logMessage(final String s) {
        LOG.debug(s);
    }

    protected void logProtocolException(final HttpException ex) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("HTTP protocol error: " + ex.getMessage());
        }
    }

    public void close() throws IOException {
        closeConnection();
    }

    public void run() {
        LOG.debug("New connection thread");
        try {
            while (!Thread.interrupted() && !isDestroyed() && isActive()) {
                handleRequest();
            }
        } finally {
            destroy();
            if (this.callback != null) {
                this.callback.completed(this);
            }
        }
    }
                   
}
