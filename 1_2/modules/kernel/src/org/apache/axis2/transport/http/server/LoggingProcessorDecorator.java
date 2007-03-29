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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;

import java.io.IOException;

/**
 * This class wraps an arbitrary {@link HttpProcessor} and extends it with
 * an additional request / response debugging service
 */
public class LoggingProcessorDecorator implements HttpProcessor {

    private static final Log HEADERLOG =
            LogFactory.getLog("org.apache.axis2.transport.http.server.wire");

    final private HttpProcessor httpProcessor;

    public LoggingProcessorDecorator(final HttpProcessor httpProcessor) {
        super();
        if (httpProcessor == null) {
            throw new IllegalArgumentException("HTTP processor may not be null");
        }
        this.httpProcessor = httpProcessor;
    }

    public void process(final HttpRequest request, final HttpContext context)
            throws HttpException, IOException {
        this.httpProcessor.process(request, context);
        if (HEADERLOG.isDebugEnabled()) {
            HEADERLOG.debug(">> " + request.getRequestLine().toString());
            Header[] headers = request.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                HEADERLOG.debug(">> " + headers[i].toString());
            }
        }
    }

    public void process(final HttpResponse response, final HttpContext context)
            throws HttpException, IOException {
        this.httpProcessor.process(response, context);
        if (HEADERLOG.isDebugEnabled()) {
            HEADERLOG.debug("<< " + response.getStatusLine().toString());
            Header[] headers = response.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                HEADERLOG.debug("<< " + headers[i].toString());
            }
        }
    }

}
