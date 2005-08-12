/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/server/HttpService.java,v 1.1 2004/02/27 19:04:32 olegk Exp $
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.axis2.transport.http.server;

import java.io.IOException;

/**
 * Defines an HTTP request/response service for the SimpleHttpServer
 * 
 * @author Oleg Kalnichevski
 */
public interface HttpService {
    /**
     * This interface represents a serice to process HTTP requests.
     * 
     * @param request       The HTTP request object.
     * @param response      The HTTP response object.
     * @return true if this service was able to handle the request, false otherwise.
     * 
     * @throws IOException
     */
    public boolean process(
        final SimpleRequest request, final SimpleResponse response) 
            throws IOException;
}
