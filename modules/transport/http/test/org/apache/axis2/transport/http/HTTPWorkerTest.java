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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.server.AxisHttpRequest;
import org.apache.axis2.transport.http.server.AxisHttpResponse;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.ProtocolVersion;
import org.apache.http.params.HttpParams;
import org.apache.ws.commons.schema.XmlSchema;
import org.junit.Test;

public class HTTPWorkerTest extends XMLSchemaTest {
    protected AxisService service;
    private ArrayList<XmlSchema> schemas;

    private HTTPWorker httpWorker = new HTTPWorker();
    private MessageContext messageContext = new MessageContext();
    private ConfigurationContext configurationContext;
    private ByteArrayOutputStream outputStream;

    @Override
    protected void setUp() throws Exception {
        service = new AxisService();
        outputStream = new ByteArrayOutputStream();
        schemas = new ArrayList<XmlSchema>();
        loadSampleSchemaFile(schemas);
        service.addSchema(schemas);

        AxisConfiguration axisConfiguration = new AxisConfiguration();
        service.setName("test_service");
        axisConfiguration.addChild(service);
        axisConfiguration.addService(service);
        configurationContext = new ConfigurationContext(axisConfiguration);
        configurationContext.setServicePath("test_service");
        configurationContext.setContextRoot("test/context");

        messageContext.setConfigurationContext(configurationContext);

    }

    @Override
    protected void tearDown() throws Exception {
        service = null;
        schemas = null;
        outputStream = null;
        super.tearDown();
    }

    @Test
    public void testService() throws Exception {
        // THis method test if (HttpUtils.indexOfIngnoreCase(uri , "?xsd=") > 0)
        // { section of the service method for xmlschema usage
        httpWorker.service(new AxisHttpRequest() {

            public void setParams(HttpParams arg0) {
            }

            public void setHeaders(Header[] arg0) {
            }

            public void setHeader(String arg0, String arg1) {
            }

            public void setHeader(Header arg0) {
            }

            public void removeHeaders(String arg0) {
            }

            public void removeHeader(Header arg0) {
            }

            public HeaderIterator headerIterator(String arg0) {
                return null;
            }

            public HeaderIterator headerIterator() {
                return null;
            }

            public ProtocolVersion getProtocolVersion() {
                return null;
            }

            public HttpParams getParams() {
                return null;
            }

            public Header getLastHeader(String arg0) {
                return null;
            }

            public Header[] getHeaders(String arg0) {
                return null;
            }

            public Header getFirstHeader(String arg0) {
                return null;
            }

            public Header[] getAllHeaders() {
                return null;
            }

            public boolean containsHeader(String arg0) {
                return false;
            }

            public void addHeader(String arg0, String arg1) {
            }

            public void addHeader(Header arg0) {
            }

            public String getRequestURI() {
                return "/test/context/test_service/test_service?xsd=sampleSchema";
            }

            public String getMethod() {
                return HTTPConstants.HEADER_GET;
            }

            public InputStream getInputStream() {
                return null;
            }

            public String getContentType() {
                return null;
            }
        }, new AxisHttpResponse() {

            public void setParams(HttpParams arg0) {
            }

            public void setHeaders(Header[] arg0) {
            }

            public void setHeader(String arg0, String arg1) {
            }

            public void setHeader(Header arg0) {
            }

            public void removeHeaders(String arg0) {
            }

            public void removeHeader(Header arg0) {
            }

            public HeaderIterator headerIterator(String arg0) {
                return null;
            }

            public HeaderIterator headerIterator() {
                return null;
            }

            public ProtocolVersion getProtocolVersion() {
                return null;
            }

            public HttpParams getParams() {
                return null;
            }

            public Header getLastHeader(String arg0) {
                return null;
            }

            public Header[] getHeaders(String arg0) {
                return null;
            }

            public Header getFirstHeader(String arg0) {
                return null;
            }

            public Header[] getAllHeaders() {
                return null;
            }

            public boolean containsHeader(String arg0) {
                return false;
            }

            public void addHeader(String arg0, String arg1) {
            }

            public void addHeader(Header arg0) {
            }

            public void setStatus(int sc) {
            }

            public void setContentType(String contentType) {
            }

            public void sendError(int sc) {
            }

            public void sendError(int sc, String msg) {
            }

            public OutputStream getOutputStream() {
                return outputStream;
            }
        }, messageContext);
        // compare actual schema with schema from the response
        assertSimilarXML(readXMLfromSchemaFile(customDirectoryLocation + "sampleSchema.xsd"),
                outputStream.toString());

    }

}
