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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

//This Sample test Client is written for Yahoo Web Search

public class RESTGetTest extends TestCase {

    public void testRESTGet() throws Exception {
        String epr = "http://api.search.yahoo.com/WebSearchService/V1/webSearch";

        String xml =
                "<websearch>" +
                        "<appid>ApacheRestDemo</appid>" +
                        "<query>finances</query>" +
                        "<format>pdf</format>" +
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
        OMElement data = builder.getDocumentElement();
        Options options = new Options();
        options.setTo(new EndpointReference(epr));
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
        options.setProperty(Constants.Configuration.ENABLE_REST_THROUGH_GET, Constants.VALUE_TRUE);

        //if post is through GET of HTTP

        ServiceClient sender = new ServiceClient();
        sender.setOptions(options);
        options.setTo(new EndpointReference (epr));
        OMElement response = sender.sendReceive(data);

        response.serialize(System.out);
    }
}
