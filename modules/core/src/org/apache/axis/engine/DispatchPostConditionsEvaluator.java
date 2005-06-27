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
package org.apache.axis.engine;

import javax.xml.namespace.QName;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.HandlerDescription;
import org.apache.axis.handlers.AbstractHandler;

/**
 * Class Dispatcher
 */
public class DispatchPostConditionsEvaluator extends AbstractHandler implements Handler {
    /**
     * Field NAME
     */
    public static final QName NAME =
        new QName("http://axis.ws.apache.org", "DispatchPostConditionsEvaluator");

    /**
     * Constructor Dispatcher
     */
    private ConfigurationContext engineContext;

    public DispatchPostConditionsEvaluator() {
        init(new HandlerDescription(NAME));
    }

    /**
     * Method invoke
     *
     * @param msgctx
     * @throws AxisFault
     */
    public final void invoke(MessageContext msgctx) throws AxisFault {
        EndpointReference toEPR = msgctx.getTo();
        if (msgctx.getServiceContext() == null) {
            throw new AxisFault("Service Not found EPR is " + ((toEPR!= null)?toEPR.getAddress():""));
        }

        if (msgctx.getServiceContext() != null && msgctx.getOperationContext() == null) {
            throw new AxisFault("Operation Not found EPR is " + ((toEPR!= null)?toEPR.getAddress():"") + " and WSA Action = "+ msgctx.getWSAAction());
        }

    }

}
