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

package org.apache.axis2.clientapi;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;

import javax.xml.namespace.QName;

/**
 * This is the Super Class for all the MEPClients, All the MEPClient will extend this.
 */
public abstract class MEPClient {
    protected ServiceContext serviceContext;
    protected final String mep;
    protected String soapVersionURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
    protected String soapAction = "";
    protected boolean doREST = false;
    protected boolean doRestThroughPOST = true;
    protected String wsaAction;

    /*
      If there is a SOAP Fault in the body of the incoming SOAP Message, system can be configured to
      throw an exception with the details extracted from the information from the fault message.
      This boolean variable will enable that facility. If this is false, the response message will just
      be returned to the application, irrespective of whether it has a Fault or not.
    */
    protected boolean isExceptionToBeThrownOnSOAPFault = true;


    public String getSoapAction() {
        return soapAction;
    }

    public MEPClient(ServiceContext service, String mep) {
        this.serviceContext = service;
        this.mep = mep;
    }

    /**
     * prepare the message context for invocation, here the properties kept in the
     * MEPClient copied to the MessageContext
     */
    protected void prepareInvocation(OperationDescription axisop, MessageContext msgCtx)
            throws AxisFault {
        if (axisop == null) {
            throw new AxisFault(Messages.getMessage("cannotBeNullOperationDescription"));
        }
        //make sure operation is type right MEP
        if (mep.equals(axisop.getMessageExchangePattern())) {
            throw new AxisFault(
                    Messages.getMessage(
                            "mepClientSupportOnly",
                            mep,
                            axisop.getMessageExchangePattern()));
        }
        //if operation not alrady added, add it
        if (serviceContext.getServiceConfig().getOperation(axisop.getName()) == null) {
            serviceContext.getServiceConfig().addOperation(axisop);
        }
        msgCtx.setDoingREST(doREST);
        msgCtx.setRestThroughPOST(doRestThroughPOST);
        if (wsaAction != null) {
            msgCtx.setWSAAction(wsaAction);
        }
        msgCtx.setSoapAction(soapAction);
    }

    /**
     * This class prepare the SOAP Envelope using the payload
     *
     * @param toSend
     * @return
     * @throws AxisFault
     */
    protected MessageContext prepareTheSOAPEnvelope(OMElement toSend) throws AxisFault {
        MessageContext msgctx = new MessageContext(serviceContext.getEngineContext());

        SOAPEnvelope envelope = createDefaultSOAPEnvelope();
        if (toSend != null) {
            envelope.getBody().addChild(toSend);
        }
        msgctx.setEnvelope(envelope);
        return msgctx;
    }

    /**
     * try to infer the transport looking at the URL, the URL can be http://
     * tcp:// mail:// local://. The method will look for the trnasport name as the
     * protocol part of the transport.
     *
     * @param epr
     * @return
     * @throws AxisFault
     */
    public TransportOutDescription inferTransport(EndpointReference epr) throws AxisFault {
        String transport = null;
        if (epr != null) {
            String toURL = epr.getAddress();
            int index = toURL.indexOf(':');
            if (index > 0) {
                transport = toURL.substring(0, index);
            }
        }

        if (transport != null) {
            return serviceContext.getEngineContext().getAxisConfiguration().getTransportOut(
                    new QName(transport));

        } else {
            throw new AxisFault(Messages.getMessage("cannotInferTransport"));
        }

    }

    /**
     * create write SOAPEvelope(in terms of version) based on the values set.
     *
     * @return
     * @throws AxisFault
     */
    public SOAPEnvelope createDefaultSOAPEnvelope() throws AxisFault {
        SOAPFactory fac = null;
        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            fac = OMAbstractFactory.getSOAP12Factory();
        } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            fac = OMAbstractFactory.getSOAP11Factory();
        } else {
            throw new AxisFault(Messages.getMessage("invaidSOAPversion"));
        }
        return fac.getDefaultEnvelope();
    }

    /**
     * Engage a given Module to the current invocation. But to call this method the
     * Module *MUST* be enable (picked up by the deployment and known to Axis2) else
     * Exception will be thrown. To be detected put the moduels to the AXIS2_REPOSITORY/modules directory
     *
     * @param name
     * @throws AxisFault
     */
    public void engageModule(QName name) throws AxisFault {
        AxisConfiguration axisConf = serviceContext.getEngineContext().getAxisConfiguration();
        //if it is already engeged do not engege it agaien
        if (!axisConf.isEngaged(name)) {
            axisConf.engageModule(name);
        }
    }

    /**
     * @param string
     */
    public void setSoapVersionURI(String string) {
        soapVersionURI = string;
    }

    /**
     * @param string
     */
    public void setSoapAction(String string) {
        soapAction = string;
    }

    /**
     * @param string
     */
    public void setWsaAction(String string) {
        wsaAction = string;
    }

    /**
     * @param exceptionToBeThrownOnSOAPFault - If there is a SOAP Fault in the body of the incoming
     *                                       SOAP Message, system can be configured to throw an exception with the details extracted from
     *                                       the information from the fault message.
     *                                       This boolean variable will enable that facility. If this is false, the response message will just
     *                                       be returned to the application, irrespective of whether it has a Fault or not.
     */
    public void setExceptionToBeThrownOnSOAPFault(boolean exceptionToBeThrownOnSOAPFault) {
        isExceptionToBeThrownOnSOAPFault = exceptionToBeThrownOnSOAPFault;
    }

}
