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

package org.apache.axis2.swa;

/**
 * @author <a href="mailto:thilina@opensource.lk">Thilina Gunarathne </a>
 */

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.attachments.utils.ImageDataSource;
import org.apache.axis2.attachments.utils.JDK13IO;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.impl.llom.OMTextImpl;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.transport.http.HTTPTransportSender;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

public class EchoRawSwATest extends TestCase {
    private EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:"
            + (UtilServer.TESTING_PORT)
            + "/axis/services/EchoSwAService/echoAttachment");

    private Log log = LogFactory.getLog(getClass());

    private QName serviceName = new QName("EchoSwAService");

    private QName operationName = new QName("echoAttachment");

    private QName transportName = new QName("http://localhost/my",
            "NullTransport");

    private String imageInFileName = "img/test.jpg";

    private String imageOutFileName = "mtom/img/testOut.jpg";

    private AxisConfiguration engineRegistry;

    private MessageContext mc;

    private ServiceContext serviceContext;

    private ServiceDescription service;

    private boolean finish = false;
    
    private OMTextImpl expectedTextData;

    public EchoRawSwATest() {
        super(EchoRawSwATest.class.getName());
    }

    public EchoRawSwATest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start(Constants.TESTING_PATH + "MTOM-enabledRepository");
        service = Utils.createSimpleService(serviceName, EchoSwA.class.getName(),
                operationName);
        UtilServer.deployService(service);
        serviceContext = UtilServer.getConfigurationContext()
                .createServiceContext(service.getName());
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }


    public void testEchoXMLSync() throws Exception {
        Socket socket =  new Socket("127.0.0.1",5555);
        
    }

    private InputStream getResourceAsStream(String path) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResourceAsStream(path);
    }
    private void compareWithCreatedOMText(OMText actualTextData) {
        String originalTextValue = expectedTextData.getText();
        String returnedTextValue = actualTextData.getText();
        TestCase.assertEquals(returnedTextValue, originalTextValue);
    }
}