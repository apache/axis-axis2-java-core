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

package org.apache.axis2.rest;

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Call;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;

public class GetTest extends TestCase implements TestConstants {

    public void testRESTGet() throws Exception {

        String epr = "http://localhost:8080/axis2/services/MyService";

        String xml = "<echo>" +
                "<Text>Hello</Text>" +
                "</echo>";

        byte arr[] = xml.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(arr);

        XMLStreamReader reader = null;
        try {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            reader = xif.createXMLStreamReader(bais);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        StAXOMBuilder builder = new StAXOMBuilder(reader);
        OMElement data = builder.getDocumentElement();

        Call call = new Call();
        call.setTo(new EndpointReference(epr));
        call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);
        call.set(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
        call.set(Constants.Configuration.ENABLE_REST_THROUGH_GET, Constants.VALUE_TRUE);

        //if post is through GET of HTTP
        OMElement response = call.invokeBlocking("webSearch", data);
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
        response.serialize(new OMOutputImpl(writer));
        writer.flush();
    }
}

