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

package test.interop.sun.round4.simple;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Options;
import org.apache.axis2.om.OMElement;
import test.interop.sun.round4.simple.util.SunGroupHClientUtil;

public class EchoBlockingClient {
    public OMElement sendMsg(SunGroupHClientUtil util, String soapAction) {
        OMElement firstchild = null;
        EndpointReference targetEPR =
                new EndpointReference("http://soapinterop.java.sun.com:80/round4/grouph/simplerpcenc");
        try {


            Call call = new Call();
            Options options = new Options();
            call.setClientOptions(options);
            options.setTo(targetEPR);
            options.setExceptionToBeThrownOnSOAPFault(false);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setSoapAction(soapAction);
            //Blocking invocation

            firstchild = call.invokeBlocking("", util.getEchoOMElement());

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();


        }
        return firstchild;

    }


}

