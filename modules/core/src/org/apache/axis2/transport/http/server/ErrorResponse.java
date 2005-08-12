/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/server/ErrorResponse.java,v 1.6 2004/11/13 12:21:28 olegk Exp $
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

import java.util.HashMap;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;

/**
 * Default error responses.
 * 
 * @author Christian Kohlschuetter
 */
public class ErrorResponse {
    
    private static final HashMap responses = new HashMap();
    
    private ErrorResponse() {
        super();
    }
    
    public static SimpleResponse getResponse(int statusCode) {
        Integer code = new Integer(statusCode);
        SimpleResponse response = (SimpleResponse)responses.get(code);
        if (response == null) {
            response = new SimpleResponse();
            response.setStatusLine(HttpVersion.HTTP_1_0, statusCode);
            response.setHeader(new Header("Content-Type", "text/plain; charset=US-ASCII"));

            String s = HttpStatus.getStatusText(statusCode);
            if (s == null) {
                s = "Error " + statusCode;
            }
            response.setBodyString(s);
            response.addHeader(new Header("Connection", "close"));
            response.addHeader(new Header("Content-Length", Integer.toString(s.length())));
            responses.put(code, response);
        }
        return response;
    }
}
