package org.apache.axis2.engine.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Options;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

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
 *
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */

public class InvokerThread extends Thread {

    private int threadNumber;
    protected EndpointReference targetEPR =
            new EndpointReference("http://127.0.0.1:"
                    + (UtilServer.TESTING_PORT)
                    + "/axis/services/EchoXMLService/echoOMElement");
    protected QName operationName = new QName("echoOMElement");
    protected Log log = LogFactory.getLog(getClass());
    private Exception thrownException = null;

    public InvokerThread(int threadNumber) {
        this.threadNumber = threadNumber;
    }

    public void run() {
        try {
            log.info("Starting Thread number "+ threadNumber + " .............");
            Call call =
                    new Call("target/test-resources/intregrationRepo");
            OMElement payload = TestingUtils.createDummyOMElement();

            Options options = new Options();
            options.setTo(targetEPR);
            options.setTransportInfo(Constants.TRANSPORT_HTTP,
                    Constants.TRANSPORT_HTTP,
                    false);
            call.setClientOptions(options);

            OMElement result =
                    call.invokeBlocking(operationName.getLocalPart(),
                            payload);
            TestingUtils.campareWithCreatedOMElement(result);
            call.close();
            log.info("Finishing Thread number "+ threadNumber + " .....");
        } catch (AxisFault axisFault) {
            thrownException = axisFault;
            log.error("Error has occured invoking the service ", axisFault);
        }
    }

    public Exception getThrownException() {
        return thrownException;
    }
}
