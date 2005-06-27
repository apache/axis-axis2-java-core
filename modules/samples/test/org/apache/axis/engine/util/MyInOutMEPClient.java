package org.apache.axis.engine.util;

import org.apache.axis.clientapi.InOutMEPClient;
import org.apache.axis.clientapi.Call;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMElement;
import org.apache.axis.soap.SOAPEnvelope;

import javax.xml.namespace.QName;
import java.util.HashMap;

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
 *
 * author : Eran Chinthaka (chinthaka@apache.org)
 */

public class MyInOutMEPClient extends Call{

    public MyInOutMEPClient() throws AxisFault {
        super(assumeServiceContext(null));
    }

    public SOAPEnvelope invokeBlockingWithEnvelopeOut(String axisop, OMElement toSend) throws AxisFault {
        OperationDescription axisConfig =
            serviceContext.getServiceConfig().getOperation(new QName(axisop));
         if (axisConfig == null) {
            axisConfig = new OperationDescription(new QName(axisop));
            axisConfig.setRemainingPhasesInFlow(operationTemplate.getRemainingPhasesInFlow());
            axisConfig.setPhasesOutFlow(operationTemplate.getPhasesOutFlow());
            axisConfig.setPhasesInFaultFlow(operationTemplate.getPhasesInFaultFlow());
            axisConfig.setPhasesOutFaultFlow(operationTemplate.getPhasesOutFaultFlow());
            serviceContext.getServiceConfig().addOperation(axisConfig);
        }

//        if (axisConfig == null) {
//            axisConfig = new OperationDescription(new QName(axisop));
//            serviceContext.getServiceConfig().addOperation(axisConfig);
//        }
        MessageContext msgctx = prepareTheSystem(toSend);

        MessageContext responseContext = super.invokeBlocking(axisConfig, msgctx);
        SOAPEnvelope envelope = responseContext.getEnvelope();
        return envelope;
    }
}
