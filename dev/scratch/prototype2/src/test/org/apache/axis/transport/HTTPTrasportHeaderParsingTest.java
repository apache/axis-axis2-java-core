/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.transport;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.transport.http.HTTPTransportReciver;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Map;

public class HTTPTrasportHeaderParsingTest extends AbstractTestCase {

    public HTTPTrasportHeaderParsingTest(String testName) {
        super(testName);
    }

    public void testServerHaeders() throws Exception {
        String message =
                "POST /axis2/services/echo HTTP/1.0\n"
                + "Content-Type: text/xml; charset=utf-8\n"
                + "Accept: application/soap+xml, application/dime, multipart/related, text/*\n"
                + "User-Agent: Axis/1.2RC1\n"
                + "Host: 127.0.0.1:8081\n"
                + "Cache-Control: no-cache\n"
                + "Pragma: no-cache\n"
                + "SOAPAction: \"\"\n"
                + "Content-Length: 73507\n\nee rwewebtewbeww";
        StringReader reader = new StringReader(message);
        HTTPTransportReciver reciver = new HTTPTransportReciver();

        Map map = reciver.parseTheHeaders(reader, true);
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
        StringReader reader = new StringReader(message);
        HTTPTransportReciver reciver = new HTTPTransportReciver();

        Map map = reciver.parseTheHeaders(reader, false);
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
            StringReader reader = new StringReader(message);
            HTTPTransportReciver reciver = new HTTPTransportReciver();
            BufferedReader br = new BufferedReader(reader);
            Map map = reciver.parseTheHeaders(br, false);
            fail("test must failed as \n\n is missing");
        } catch (AxisFault e) {
        }
    }

}
