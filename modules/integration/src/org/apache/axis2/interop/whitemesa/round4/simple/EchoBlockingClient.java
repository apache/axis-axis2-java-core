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

package org.apache.axis2.interop.whitemesa.round4.simple;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.interop.whitemesa.round4.simple.utils.WhitemesaR4ClientUtil;
import org.apache.axis2.om.OMElement;


public class EchoBlockingClient {


    public OMElement sendMsg(WhitemesaR4ClientUtil util, String soapAction) throws AxisFault {
        OMElement firstchild = null;
        EndpointReference targetEPR = new EndpointReference("http://www.whitemesa.net:80/interop/r4/fault-rpc");

        try {


            Call call = new Call("target/test-resources/intregrationRepo");
            call.setTo(targetEPR);
            call.setExceptionToBeThrownOnSOAPFault(false);
            call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);
            call.setSoapAction(soapAction);
            //Blocking invocation

            firstchild = call.invokeBlocking("", util.getEchoOMElement());


        } catch (Exception e) {
            throw new AxisFault(e);
        }
        return firstchild;

    }

}

