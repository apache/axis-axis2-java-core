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

package org.apache.axis2.transport.http;

import org.apache.axis2.Constants;
import org.apache.axis2.transport.http.impl.httpclient4.HTTPSenderImpl;

import javax.ws.rs.core.HttpHeaders;

public class HTTPClient4SenderTest extends HTTPSenderTest {

    @Override
    protected HTTPSender getHTTPSender() {
        return new HTTPSenderImpl();
    }

    @Override
    public void testSendViaGet() throws Exception {
        int port = getBasicHttpServer().getPort();
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_GET, "urn:getService", "http://localhost:"
                                                                               + port + "/getService", true);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_GET,
                     getHTTPMethod());
        assertEquals("Not the expected content", "/getService?part=sample%20data",
                     getStringContent());
        assertNull("Not the expected HTTP Header value", getHeaders().get("SOAPAction"));
        assertEquals("Not the expected HTTP Header value",
                     "application/x-www-form-urlencoded;action=\"urn:getService\";",
                     getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost",
                     getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                     getHeaders().get(HttpHeaders.USER_AGENT));
    }

    @Override
    public void testSendViaPost() throws Exception {
        // test with REST payload
        int port = getBasicHttpServer().getPort();
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_POST, "urn:postService",
                    "http://localhost:" + port + "/postService", true);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_POST,
                     getHTTPMethod());
        assertEquals("Not the expected content", getEnvelope().getFirstElement().getFirstElement()
                .toString(), getStringContent());
        assertNull("Not the expected HTTP Header value", getHeaders().get("SOAPAction"));
        assertEquals("Not the expected HTTP Header value", "application/xml",
                     getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost",
                     getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                     getHeaders().get(HttpHeaders.USER_AGENT));

        // test with SOAP payload.
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_POST, "urn:postService",
                    "http://localhost:" + port + "/postService", false);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_POST,
                     getHTTPMethod());
        assertEquals("Not the expected content", getEnvelope().toString(), getStringContent());
        assertEquals("Not the expected HTTP Header value", "urn:postService",
                     getHeaders().get("SOAPAction").replace("\"", ""));
        assertEquals("Not the expected HTTP Header value", "text/xml",
                     getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost",
                     getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                     getHeaders().get(HttpHeaders.USER_AGENT));
    }

    @Override
    public void testSendViaPut() throws Exception {
        // test with REST payload
        int port = getBasicHttpServer().getPort();
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_PUT, "urn:putService", "http://localhost:"
                                                                               + port + "/putService", true);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_PUT,
                     getHTTPMethod());
        assertEquals("Not the expected content", getEnvelope().getFirstElement().getFirstElement()
                .toString(), getStringContent());
        assertNull("Not the expected HTTP Header value", getHeaders().get("SOAPAction"));
        assertEquals("Not the expected HTTP Header value", "application/xml",
                     getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost",
                     getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                     getHeaders().get(HttpHeaders.USER_AGENT));

        // test with SOAP payload.
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_PUT, "urn:putService", "http://localhost:"
                                                                               + port + "/putService", false);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_PUT,
                     getHTTPMethod());
        assertEquals("Not the expected content", getEnvelope().toString(), getStringContent());
        assertEquals("Not the expected HTTP Header value", "urn:putService",
                     getHeaders().get("SOAPAction").replace("\"", ""));
        assertEquals("Not the expected HTTP Header value", "text/xml",
                     getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost",
                     getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                     getHeaders().get(HttpHeaders.USER_AGENT));
    }

    @Override
    public void testSendViaDelete() throws Exception {
        // test with REST payload
        int port = getBasicHttpServer().getPort();
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_DELETE, "urn:deleteService",
                    "http://localhost:" + port + "/deleteService", true);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_DELETE,
                     getHTTPMethod());
        assertEquals("Not the expected content", "/deleteService?part=sample%20data",
                     getStringContent());
        assertEquals("Not the expected HTTP Header value",
                     "application/x-www-form-urlencoded;action=\"urn:deleteService\";", getHeaders()
                .get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost",
                     getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                     getHeaders().get(HttpHeaders.USER_AGENT));
    }

    @Override
    public void testSendViaHead() throws Exception {
        int port = getBasicHttpServer().getPort();
        sendViaHTTP(Constants.Configuration.HTTP_METHOD_HEAD, "urn:deleteService",
                    "http://localhost:" + port + "/deleteService", true);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_POST,
                     getHTTPMethod());
        assertEquals("Not the expected content", getEnvelope().getFirstElement().getFirstElement()
                .toString(), getStringContent());
        assertEquals("Not the expected HTTP Header value", "application/xml",
                     getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost",
                     getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                     getHeaders().get(HttpHeaders.USER_AGENT));

    }

    @Override
    public void testSendNOHTTPMethod() throws Exception {
        int port = getBasicHttpServer().getPort();
        sendViaHTTP(null, "urn:noService", "http://localhost:" + port + "/noService", true);
        assertEquals("Not the expected HTTP Method", Constants.Configuration.HTTP_METHOD_POST,
                     getHTTPMethod());
        assertEquals("Not the expected content", getEnvelope().getFirstElement().getFirstElement()
                .toString(), getStringContent());
        assertNull("Not the expected HTTP Header value", getHeaders().get("SOAPAction"));
        assertEquals("Not the expected HTTP Header value", "application/xml",
                     getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals("Not the expected HTTP Header value", "localhost",
                     getHeaders().get(HttpHeaders.HOST));
        assertEquals("Not the expected HTTP Header value", "Axis2",
                     getHeaders().get(HttpHeaders.USER_AGENT));
    }
}
