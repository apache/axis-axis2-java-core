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

package org.apache.axis2.mtom;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.NoHttpResponseException;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

public class EchoRawMTOMFaultReportTest extends UtilServerBasedTestCase {

    private QName serviceName = new QName("EchoService");

    private QName operationName = new QName("mtomSample");

    public EchoRawMTOMFaultReportTest() {
        super(EchoRawMTOMFaultReportTest.class.getName());
    }

    public EchoRawMTOMFaultReportTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup2(new TestSuite(EchoRawMTOMFaultReportTest.class),
                             TestingUtils.prefixBaseDirectory(Constants.TESTING_PATH + "MTOM-enabledRepository"));
    }


    protected void setUp() throws Exception {
        AxisService service = new AxisService(serviceName.getLocalPart());
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new Parameter(Constants.SERVICE_CLASS,
                                           EchoService.class.getName()));

        AxisOperation axisOp = new OutInAxisOperation(operationName);
        axisOp.setMessageReceiver(new RawXMLINOutMessageReceiver());
        axisOp.setStyle(WSDLConstants.STYLE_DOC);
        service.addOperation(axisOp);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
    }

    public void testEchoFaultSync() throws Exception {
	HttpPost httpPost = new HttpPost("http://127.0.0.1:" + (UtilServer.TESTING_PORT) + "/axis2/services/EchoService/mtomSample");

        httpPost.setEntity(new InputStreamEntity(
                new FileInputStream(TestingUtils.prefixBaseDirectory("test-resources/mtom/wmtom.bin"))));

	Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, "multipart/related; boundary=--MIMEBoundary258DE2D105298B756D; type=\"application/xop+xml\"; start=\"<0.15B50EF49317518B01@apache.org>\"; start-info=\"application/soap+xml\"");

	List<Header> headers = new ArrayList();
        headers.add(header);
	
	CloseableHttpClient httpclient = HttpClients.custom().setDefaultHeaders(headers).setRetryHandler(new HttpRequestRetryHandler() {
                @Override
                public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                    if (executionCount >= 10) {
                        return false;
                    }
                    if (exception instanceof NoHttpResponseException) {
                        return true;
                    }
                    if (exception instanceof InterruptedIOException) {
                        return true;
                    }
                    HttpClientContext clientContext = HttpClientContext.adapt(context);
                    if (!clientContext.isRequestSent()) {
                        return true;
                    }
                    // otherwise do not retry
                    return false;
                }
            }).build();


        try {
            CloseableHttpResponse hcResponse = httpclient.execute(httpPost);
	    int status = hcResponse.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                
                // TODO: There is a missing wsa:Action header in the SOAP message.  Fix or look for correct fault text!

//                assertEquals("HTTP/1.1 500 Internal server error",
//                             httppost.getStatusLine().toString());
            }
	    System.out.println("\ntestEchoFaultSync() result status: " + status + " , statusLine: " + hcResponse.getStatusLine());

        }finally {
            httpclient.close();
        }
    }	
}
