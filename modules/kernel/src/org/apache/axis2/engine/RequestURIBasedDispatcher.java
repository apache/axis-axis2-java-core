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


package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Dispatches the service based on the information from the target endpoint URL.
 */
public class RequestURIBasedDispatcher extends AbstractDispatcher {

    public static final String NAME = "RequestURIBasedDispatcher";
    private static final Log log = LogFactory.getLog(RequestURIBasedDispatcher.class);
    private static final boolean isDebugEnabled = log.isDebugEnabled();

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findOperation(org.apache.axis2.description.AxisService, org.apache.axis2.context.MessageContext)
     */
    public AxisOperation findOperation(AxisService service, MessageContext messageContext)
            throws AxisFault {
        // This Dispatcher does not need to resolve the operation, as that is handled
        // by the RequestURIOperationDispatcher.
                return null;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        EndpointReference toEPR = messageContext.getTo();

        if (toEPR != null) {
            if (isDebugEnabled) {
                log.debug(messageContext.getLogIDString() +
                        " Checking for Service using target endpoint address : " +
                        toEPR.getAddress());
            }
            String filePart = toEPR.getAddress();
            //REVIEW: (nagy) Parsing the RequestURI will also give us the operationName if present, so we could conceivably store it in the MessageContext, but doing so and retrieving it is probably no faster than simply reparsing the URI
            ConfigurationContext configurationContext = messageContext.getConfigurationContext();
            String[] values = Utils.parseRequestURLForServiceAndOperation(filePart,
                                                                          configurationContext.getServiceContextPath());

            if ((values.length >= 1) && (values[0] != null)) {
                AxisConfiguration registry =
                        configurationContext.getAxisConfiguration();

                AxisService axisService = registry.getService(values[0]);

                // If the axisService is not null we get the binding that the request came to add
                // add it as a property to the messageContext
                if (axisService != null) {
                    Map endpoints = axisService.getEndpoints();
                    if (endpoints != null) {
                        if (endpoints.size() == 1) {
                            messageContext.setProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME,
                                                       endpoints.get(
                                                               axisService.getEndpointName()));
                        } else {
                            String endpointName = values[0].substring(values[0].indexOf(".") + 1);
                            messageContext.setProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME,
                                                       endpoints.get(endpointName));
                        }
                    }
                }

                return axisService;
            } else {
                if (isDebugEnabled) {
                    log.debug(messageContext.getLogIDString() +
                            " Attempted to check for Service using target endpoint URI, but the service fragment was missing");
                }
                return null;
            }
        } else {
            if (isDebugEnabled) {
                log.debug(messageContext.getLogIDString() +
                        " Attempted to check for Service using null target endpoint URI");
            }
            return null;
        }
    }

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }
}
