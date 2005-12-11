package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.i18n.Messages;

import java.util.ArrayList;
/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, softwar
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* @author : Deepal Jayasinghe (deepal@apache.org)
*
*/

public class DispatchPhase extends Phase {

    public DispatchPhase(String phaseName) {
        super(phaseName);
    }

    public DispatchPhase() {

    }

    public void checkPostConditions(MessageContext msgContext) throws AxisFault {

        EndpointReference toEPR = msgContext.getTo();
        if (msgContext.getAxisService() == null) {
            throw new AxisFault(
                    "Service Not found EPR is " +
                            ((toEPR != null) ? toEPR.getAddress() : ""));
        } else if (msgContext.getAxisOperation() == null) {
            throw new AxisFault(
                    "Operation Not found EPR is " +
                            ((toEPR != null) ? toEPR.getAddress() : "") +
                            " and WSA Action = " +
                            msgContext.getWSAAction());
        }
        
        if (msgContext.getOperationContext() == null) {
            throw new AxisFault(
                    Messages.getMessage("cannotBeNullOperationContext"));
        }
        if (msgContext.getServiceContext() == null) {
            throw new AxisFault(
                    Messages.getMessage("cannotBeNullServiceContext"));
        }
        if (msgContext.getAxisOperation() == null && msgContext.getOperationContext() != null) {
            msgContext.setAxisOperation(msgContext.getOperationContext().getAxisOperation());
        }

        if (msgContext.getAxisService() == null && msgContext.getServiceContext() != null) {
            msgContext.setAxisService(msgContext.getServiceContext().getAxisService());
        }

        // TODO : do post-dispatch execution chain setup...
        ArrayList operationChain = msgContext.getAxisOperation().getRemainingPhasesInFlow();
        msgContext.setExecutionChain(operationChain);
    }
}
