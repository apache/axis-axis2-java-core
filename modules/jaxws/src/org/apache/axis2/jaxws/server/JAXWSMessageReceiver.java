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

package org.apache.axis2.jaxws.server;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.InvocationContextFactory;
import org.apache.axis2.jaxws.core.InvocationContextImpl;
import org.apache.axis2.jaxws.core.MEPContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.util.MessageUtils;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.axis2.util.ThreadContextMigratorUtil;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004_Constants;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2006Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.Binding;
import javax.xml.ws.WebServiceException;

/**
 * The JAXWSMessageReceiver is the entry point, from the server's perspective, to the JAX-WS code.
 * This will be called by the Axis Engine and is the end of the chain from an Axis2 perspective.
 */
public class JAXWSMessageReceiver implements MessageReceiver {

    private static final Log log = LogFactory.getLog(JAXWSMessageReceiver.class);

    private static String PARAM_SERVICE_CLASS = "ServiceClass";
    public static String PARAM_BINDING = "Binding";

    /**
     * We should have already determined which AxisService we're targetting at this point.  So now,
     * just get the service implementation and invoke the appropriate method.
     */
    public void receive(org.apache.axis2.context.MessageContext axisRequestMsgCtx)
            throws AxisFault {
        AxisFault faultToReturn = null;

        if (log.isDebugEnabled()) {
            log.debug("new request received");
        }

        //Get the name of the service impl that was stored as a parameter
        // inside of the services.xml.
        AxisService service = axisRequestMsgCtx.getAxisService();
        AxisOperation operation = axisRequestMsgCtx.getAxisOperation();
        String mep = operation.getMessageExchangePattern();
        if (log.isDebugEnabled()) {
            log.debug("MEP: " + mep);
        }

        org.apache.axis2.description.Parameter svcClassParam =
                service.getParameter(PARAM_SERVICE_CLASS);

        try {
            if (svcClassParam == null) {
                throw new RuntimeException(
                        Messages.getMessage("JAXWSMessageReceiverNoServiceClass"));
            }

            //This assumes that we are on the ultimate execution thread
            ThreadContextMigratorUtil.performMigrationToThread(
                    Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisRequestMsgCtx);

            //We'll need an instance of the EndpointController to actually
            //drive the invocation.
            //TODO: More work needed to determine the lifecycle of this thing
            EndpointController endpointCtlr = new EndpointController();

            MessageContext requestMsgCtx = new MessageContext(axisRequestMsgCtx);
            requestMsgCtx.setMEPContext(new MEPContext(requestMsgCtx));

            Binding binding = (Binding)axisRequestMsgCtx.getProperty(PARAM_BINDING);
            InvocationContext ic = InvocationContextFactory.createInvocationContext(binding);
            ic.setRequestMessageContext(requestMsgCtx);

            //TODO:Once we the JAX-WS MessageContext one of the next things that
            //needs to be done here is setting up all of the javax.xml.ws.* 
            //properties for the MessageContext.

            ic = endpointCtlr.invoke(ic);
            MessageContext responseMsgCtx = ic.getResponseMessageContext();

            //If there is a fault it could be Robust In-Only
            if (!isMepInOnly(mep) || hasFault(responseMsgCtx)) {
                // If this is a two-way exchange, there should already be a
                // JAX-WS MessageContext for the response.  We need to pull 
                // the Message data out of there and set it on the Axis2 
                // MessageContext.
                org.apache.axis2.context.MessageContext axisResponseMsgCtx =
                        responseMsgCtx.getAxisMessageContext();

                MessageUtils.putMessageOnMessageContext(responseMsgCtx.getMessage(),
                                                        axisResponseMsgCtx);

                OperationContext opCtx = axisResponseMsgCtx.getOperationContext();
                opCtx.addMessageContext(axisResponseMsgCtx);

                // If this is a fault message, we want to throw it as an
                // exception so that the transport can do the appropriate things
                if (responseMsgCtx.getMessage().isFault()) {
                    
                    //Rather than create a new AxisFault, we should use the AxisFault that was
                    //created at the causedBy
                    if (responseMsgCtx.getCausedByException() != null)
                        faultToReturn = responseMsgCtx.getCausedByException();
                    else {
                        faultToReturn = new AxisFault("An error was detected during JAXWS processing",
                                                                                 axisResponseMsgCtx);
                        
                    }
                                    } else {
                    //This assumes that we are on the ultimate execution thread
                    ThreadContextMigratorUtil.performMigrationToContext(
                            Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisResponseMsgCtx);

                    //Create the AxisEngine for the reponse and send it.
                    AxisEngine engine =
                            new AxisEngine(axisResponseMsgCtx.getConfigurationContext());
                    engine.send(axisResponseMsgCtx);
                    //This assumes that we are on the ultimate execution thread
                    ThreadContextMigratorUtil.performContextCleanup(
                            Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisResponseMsgCtx);
                }
            }

        } catch (Exception e) {
            ThreadContextMigratorUtil.performThreadCleanup(
                    Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisRequestMsgCtx);

            // Make a webservice exception (which will strip out a unnecessary stuff)
            WebServiceException wse = ExceptionFactory.makeWebServiceException(e);

            // The AxisEngine expects an AxisFault
            throw AxisFault.makeFault(wse);

        }

        //This assumes that we are on the ultimate execution thread
        ThreadContextMigratorUtil
                .performThreadCleanup(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisRequestMsgCtx);

        if (faultToReturn != null) {
            throw faultToReturn;
        }
    }

    private boolean hasFault(MessageContext responseMsgCtx) {
        if (responseMsgCtx == null || responseMsgCtx.getMessage() == null) {
            return false;
        }
        return responseMsgCtx.getMessage().isFault();
    }

    private boolean isMepInOnly(String mep) {
        boolean inOnly = mep.equals(WSDL20_2004_Constants.MEP_URI_ROBUST_IN_ONLY) ||
                mep.equals(WSDL20_2004_Constants.MEP_URI_IN_ONLY) ||
                mep.equals(WSDL2Constants.MEP_URI_IN_ONLY) ||
                mep.equals(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY) ||
                mep.equals(WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY) ||
                mep.equals(WSDL20_2006Constants.MEP_URI_IN_ONLY);
        return inOnly;
    }

}
