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
package org.apache.axis2.handlers.addressing;

import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingFaultsHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

public class AddressingSubmissionInHandler extends AddressingInHandler {

    private static final Log log = LogFactory.getLog(AddressingSubmissionInHandler.class);

    public AddressingSubmissionInHandler() {
        addressingNamespace = Submission.WSA_NAMESPACE;
        addressingVersion = "WS-Addressing Submission";
    }


    protected void extractToEprReferenceParameters(EndpointReference toEPR, SOAPHeader header,
                                                   String namespace) {
        // there is no exact way to identify ref parameters for Submission version. So let's have a handler
        // at the end of the flow, which puts all the handlers (which are of course mustUnderstand=false)
        // as reference parameters

        // TODO : Chinthaka
    }

    protected void checkForMandatoryHeaders(ArrayList alreadyFoundAddrHeader,
                                            MessageContext messageContext) throws AxisFault {
        if (!alreadyFoundAddrHeader.contains(WSA_TO)) {
            AddressingFaultsHelper.triggerMessageAddressingRequiredFault(messageContext, WSA_TO);
        }

        if (!alreadyFoundAddrHeader.contains(WSA_ACTION)) {
            AddressingFaultsHelper
                    .triggerMessageAddressingRequiredFault(messageContext, WSA_ACTION);
        }

        if (alreadyFoundAddrHeader.contains(WSA_REPLY_TO) ||
                alreadyFoundAddrHeader.contains(WSA_FAULT_TO)) {

            if (!alreadyFoundAddrHeader.contains(WSA_MESSAGE_ID)) {
                AddressingFaultsHelper
                        .triggerMessageAddressingRequiredFault(messageContext, WSA_MESSAGE_ID);
            }
        }
    }

    protected void setDefaults(ArrayList alreadyFoundAddrHeader, MessageContext messageContext) {
        //The none URI is not defined in the 2004/08 spec, but it is used here anyway
        //as a flag to indicate the correct semantics to apply, i.e. in the 2004/08 spec
        //the absence of a ReplyTo header indicates that a response is NOT required.
        if (!alreadyFoundAddrHeader.contains(WSA_REPLY_TO)) {
            Options messageContextOptions = messageContext.getOptions();
            EndpointReference epr = messageContextOptions.getReplyTo();

            if (epr == null) {
                epr = new EndpointReference("");
                messageContextOptions.setReplyTo(epr);
            }

            if (log.isTraceEnabled()) {
                log.trace(
                        "setDefaults: Setting WS-Addressing default value for the ReplyTo property.");
            }

            epr.setAddress(Final.WSA_NONE_URI);
        }
    }
}
