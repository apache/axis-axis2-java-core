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

package org.apache.axis2.transport.http.mock.server;

import java.io.IOException;
import java.util.Map;

import javax.mail.MessagingException;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;

import junit.framework.TestCase;

/**
 * The Class AbstractHTTPServerTest can be used to write any test case that
 * require HTTP back end server features. This class take care about start the
 * server at each test run and stop after the test execution.
 * AbstractHTTPServerTest also provide number of utility method to query server
 * side details. By default this use HTTPcore based BasicHttpServerImpl as the
 * HTTP server but it is possible to provide other Implementations using
 * setBasicHttpServer() method.
 * 
 * @since 1.7.0
 */
public abstract class AbstractHTTPServerTest extends TestCase {

    private BasicHttpServer basicHttpServer;

    /**
     * Gets the basic http server.
     * 
     * @return the basic http server
     */
    protected BasicHttpServer getBasicHttpServer() {
        return basicHttpServer;
    }

    /**
     * Sets the basic http server.
     * 
     * @param basicHttpServer
     *            the new basic http server
     */
    protected void setBasicHttpServer(BasicHttpServer basicHttpServer) {
        this.basicHttpServer = basicHttpServer;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (basicHttpServer == null) {
            setBasicHttpServer(new BasicHttpServerImpl());
            basicHttpServer.start();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        basicHttpServer.stop();
        basicHttpServer = null;
    }

    /**
     * Return all the HTTP Headers received by the server as a Java Map
     * instance.
     * 
     * @return the headers
     */
    protected Map<String, String> getHeaders() {
        return basicHttpServer.getHeaders();

    }

    /**
     * Gets the string content.
     * 
     * @return the string content
     */
    protected String getStringContent() {
        return new String(basicHttpServer.getContent());
    }

    /**
     * Gets the bytes content.
     * 
     * @return the bytes content
     */
    protected byte[] getBytesContent() {
        return basicHttpServer.getContent();
    }

    /**
     * Gets the hTTP method.
     * 
     * @return the hTTP method
     */
    protected String getHTTPMethod() {
        return basicHttpServer.getMethod();
    }

    /**
     * Gets the request url.
     * 
     * @return the request url
     */
    protected String getRequestURL() {
        return basicHttpServer.getUrl();

    }

    /**
     * Gets the envelope with sample data.
     * 
     * @return the envelope
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws MessagingException
     *             the messaging exception
     */
    public static SOAPEnvelope getEnvelope() throws IOException, MessagingException {
        SOAPFactory soapFac = OMAbstractFactory.getSOAP11Factory();
        OMFactory omFac = OMAbstractFactory.getOMFactory();
        SOAPEnvelope enp = soapFac.createSOAPEnvelope();
        SOAPBody sopaBody = soapFac.createSOAPBody();

        OMElement content = omFac.createOMElement(new QName("message"));
        OMElement data1 = omFac.createOMElement(new QName("part"));
        data1.setText("sample data");

        content.addChild(data1);
        sopaBody.addChild(content);
        enp.addChild(sopaBody);
        return enp;
    }

}
