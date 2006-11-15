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

package org.apache.axis2.transport;


import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportReceiver;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

public class HTTPTransportHeaderParsingTest extends AbstractTestCase {

    public HTTPTransportHeaderParsingTest(String testName) {
        super(testName);
    }

    public void testServerHaeders() throws Exception {
        String message =
                "POST /axis2/services/echo HTTP/1.0\n"
                + "Content-Type: text/xml; charset=utf-8\n"
                +
                "Accept: application/soap+xml, application/dime, multipart/related, text/*\n"
                + "User-Agent: Axis/1.2RC1\n"
                + "Host: 127.0.0.1:8081\n"
                + "Cache-Control: no-cache\n"
                + "Pragma: no-cache\n"
                + "SOAPAction: \"\"\n"
                + "Content-Length: 73507\n\nee rwewebtewbeww";

        InputStream reader = new ByteArrayInputStream(message.getBytes());
        HTTPTransportReceiver receiver = new HTTPTransportReceiver();

        Map map = receiver.parseTheHeaders(reader, true);
        assertEquals(map.get(HTTPConstants.PROTOCOL_VERSION), "HTTP/1.0");
        assertEquals(map.get(HTTPConstants.REQUEST_URI),
                "/axis2/services/echo");
        assertEquals(map.get("Accept"),
                "application/soap+xml, application/dime, multipart/related, text/*");
        assertEquals(map.get("User-Agent"), "Axis/1.2RC1");
        assertEquals(map.get("Host"), "127.0.0.1:8081");
        assertEquals(map.get("Cache-Control"), "no-cache");
        assertEquals(map.get("Pragma"), "no-cache");
        assertEquals(map.get("Content-Length"), "73507");
        assertEquals(reader.read(), 'e');
    }

    public void testClientHeaders() throws Exception {
        String message =
                "HTTP/1.1 200 OK\n"
                + "Content-Type: text/xml;charset=utf-8\n"
                + "Date: Sat, 12 Feb 2005 10:39:39 GMT\n"
                + "Server: Apache-Coyote/1.1\n"
                + "Connection: close\n\nA";
        InputStream reader = new ByteArrayInputStream(message.getBytes());
        HTTPTransportReceiver receiver = new HTTPTransportReceiver();

        Map map = receiver.parseTheHeaders(reader, false);
        assertEquals(map.get(HTTPConstants.PROTOCOL_VERSION), "HTTP/1.1");
        assertEquals(map.get(HTTPConstants.RESPONSE_CODE), "200");
        assertEquals(map.get(HTTPConstants.RESPONSE_WORD), "OK");
        assertEquals(map.get("Content-Type"), "text/xml;charset=utf-8");
        assertEquals(map.get("Date"), "Sat, 12 Feb 2005 10:39:39 GMT");
        assertEquals(map.get("Server"), "Apache-Coyote/1.1");
        assertEquals(map.get("Connection"), "close");
        assertEquals(reader.read(), 'A');
    }

    public void testWrongClientHeaders() throws AxisFault {
        try {
            String message =
                    "HTTP/1.1 200 OK\n"
                    + "Content-Type: text/xml;charset=utf-8\n"
                    + "Date: Sat, 12 Feb 2005 10:39:39 GMT\n"
                    + "Server: Apache-Coyote/1.1\n"
                    + "Connection: close";
            InputStream reader = new ByteArrayInputStream(message.getBytes());
            HTTPTransportReceiver receiver = new HTTPTransportReceiver();
            BufferedInputStream br = new BufferedInputStream(reader);
            receiver.parseTheHeaders(br, false);
            fail("test must failed as \n\n is missing");
        } catch (AxisFault e) {
        }
    }

}
