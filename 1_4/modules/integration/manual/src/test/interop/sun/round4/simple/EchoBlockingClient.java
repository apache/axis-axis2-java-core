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

package test.interop.sun.round4.simple;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import test.interop.sun.round4.simple.util.SunGroupHClientUtil;

public class EchoBlockingClient {
    public OMElement sendMsg(SunGroupHClientUtil util, String soapAction) {
        OMElement firstchild = null;
        EndpointReference targetEPR =
                new EndpointReference(
                        "http://soapinterop.java.sun.com:80/round4/grouph/simplerpcenc");
        try {
            Options options = new Options();
            options.setTo(targetEPR);
            options.setExceptionToBeThrownOnSOAPFault(false);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setAction(soapAction);
            options.setProperty(HTTPConstants.CHUNKED, Constants.VALUE_FALSE);

            ServiceClient sender = new ServiceClient();
            sender.setOptions(options);
            options.setTo(targetEPR);
            firstchild = sender.sendReceive(util.getEchoOMElement());

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();


        }
        return firstchild;

    }


}

