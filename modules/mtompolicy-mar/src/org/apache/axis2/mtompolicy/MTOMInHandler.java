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


package org.apache.axis2.mtompolicy;

import java.util.List;

import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.policy.model.MTOMAssertion;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

public class MTOMInHandler extends AbstractHandler {

    public InvocationResponse invoke(MessageContext msgCtx) throws AxisFault {
        Policy policy = msgCtx.getEffectivePolicy();
        if (policy == null) {
            return InvocationResponse.CONTINUE;
        }
        List<Assertion> list = (List<Assertion>) policy.getAlternatives()
                .next();
        for (Assertion assertion : list) {
            if (assertion instanceof MTOMAssertion) {
                String contentType = (String) msgCtx.getProperty(Constants.Configuration.CONTENT_TYPE);
                if (!assertion.isOptional()) {
                    if (contentType == null || contentType.indexOf(MTOMConstants.MTOM_TYPE) == -1) {//!MTOMConstants.MTOM_TYPE.equals(contentType)
                        if (msgCtx.isServerSide()) {
                        throw new AxisFault(
                                "The SOAP REQUEST sent by the client IS NOT MTOMized!");
                        } else {
                            throw new AxisFault(
                            "The SOAP RESPONSE sent by the service IS NOT MTOMized!");
                        }
                    }
                }
            }
        }
        return InvocationResponse.CONTINUE;
    }

}
