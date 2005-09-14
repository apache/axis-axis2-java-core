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

package org.apache.axis2.mtom;

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ParameterImpl;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.swa.EchoRawSwATest;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.WSDLService;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;

/**
 * Author: Saminda Abeyruwan <saminda@wso2.com>
 */
public class EchoRawMTOMFaultReportTest extends TestCase {

    private Log log = LogFactory.getLog(getClass());

    private QName serviceName = new QName("EchoService");

    private QName operationName = new QName("mtomSample");

    private ServiceDescription service;

    public EchoRawMTOMFaultReportTest() {
        super(EchoRawSwATest.class.getName());
    }

    public EchoRawMTOMFaultReportTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start(Constants.TESTING_PATH + "MTOM-enabledRepository");
        service = new ServiceDescription(serviceName);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new ParameterImpl(AbstractMessageReceiver.SERVICE_CLASS,
                EchoService.class.getName()));

        OperationDescription axisOp = new OperationDescription(operationName);
        axisOp.setMessageReceiver(new RawXMLINOutMessageReceiver());
        axisOp.setStyle(WSDLService.STYLE_DOC);
        service.addOperation(axisOp);
        UtilServer.deployService(service);

    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }

    public void testEchoFaultSync() throws Exception {
        HttpClient client = new HttpClient();

        PostMethod httppost = new PostMethod("http://127.0.0.1:"
                + (UtilServer.TESTING_PORT)
                + "/axis/services/EchoService/mtomSample");

        HttpMethodRetryHandler myretryhandler = new HttpMethodRetryHandler() {
            public boolean retryMethod(final HttpMethod method,
                                       final IOException exception,
                                       int executionCount) {
                if (executionCount >= 10) {
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {
                    return true;
                }
                if (!method.isRequestSent()) {
                    return true;
                }
                // otherwise do not retry
                return false;
            }
        };
        httppost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                myretryhandler);
        httppost.setRequestEntity(new InputStreamRequestEntity(
                this.getResourceAsStream("/org/apache/axis2/mtom/wmtom.bin")));

        httppost.setRequestHeader("Content-Type",
                "multipart/related; boundary=--MIMEBoundary258DE2D105298B756D; type=\"application/xop+xml\"; start=\"<0.15B50EF49317518B01@apache.org>\"; start-info=\"application/soap+xml\"");
        try {
            client.executeMethod(httppost);

            if (httppost.getStatusCode() ==
                    HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                assertEquals("HTTP/1.1 500 Internal server error",
                        httppost.getStatusLine().toString());
            }

        } finally {
            httppost.releaseConnection();
        }
    }

    private InputStream getResourceAsStream(String path) {
        return this.getClass().getResourceAsStream(path);
    }
}
