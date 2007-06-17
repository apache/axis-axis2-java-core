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

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

/**
 * Sample for synchronous single channel blocking service invocation.
 * Message Exchage Pattern IN-OUT
 */
public class PostTest extends TestCase {
    private static EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:8070/onca/xml");

    public void testRESTPost() throws Exception {
        try {
            String xml =
                    "<websearch>" +
                            "<Service>AWSECommerceService</Service>" +
                            "<SubscriptionId>03WM83XFMP0X52C7A9R2</SubscriptionId>" +
                            "<Operation>ItemSearch</Operation>" +
                            "<SearchIndex>Books</SearchIndex>" +
                            "<Keywords>Sanjiva,Web,Services</Keywords>" +
                            "<ResponseGroup>Request,Small</ResponseGroup>" +
                            "</websearch>";

            byte arr[] = xml.getBytes();
            ByteArrayInputStream bais = new ByteArrayInputStream(arr);

            XMLStreamReader reader = null;
            try {
                reader = StAXUtils.createXMLStreamReader(bais);
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement websearch = builder.getDocumentElement();

            Options options = new Options();
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            options.setProperty(HTTPConstants.HTTP_CONTENT_TYPE, "application/x-www-form-urlencoded");

            ServiceClient sender = new ServiceClient();
            sender.setOptions(options);
            options.setTo(targetEPR);
            OMElement result = sender.sendReceive(websearch);

            StringWriter writer = new StringWriter();
            result.serialize(StAXUtils
                    .createXMLStreamWriter(writer));
            writer.flush();

            System.out.println(writer.toString());

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }
}

