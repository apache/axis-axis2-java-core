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

package org.apache.axis2.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.MessageInformationHeaders;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPHeader;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the super class for all the MEPClients.
 */
public abstract class MEPClient {
    ServiceContext serviceContext;
    protected final String mep;
    protected String soapVersionURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
    protected String soapAction = "";
    protected String wsaAction;

    protected MessageInformationHeaders messageInformationHeaders = new MessageInformationHeaders();

    protected List soapHeaderList;

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
     * Prepares the message context for invocation. The properties in the
     * MEPClient are copied to the MessageContext.
     */
    protected void prepareInvocation(AxisOperation axisop, MessageContext msgCtx)
            throws AxisFault {
        if (axisop == null) {
            throw new AxisFault(Messages.getMessage("cannotBeNullAxisOperation"));
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
        if (serviceContext.getAxisService().getOperation(axisop.getName()) == null) {
            serviceContext.getAxisService().addOperation(axisop);
        }
        if (msgCtx.getMessageInformationHeaders() != null && msgCtx.getMessageInformationHeaders().getAction() != null) {
            messageInformationHeaders.setAction(msgCtx.getMessageInformationHeaders().getAction());
        }

        msgCtx.setMessageInformationHeaders(cloneMIHeaders());

        //msgCtx.setMessageInformationHeaders(messageInformationHeaders);
        msgCtx.setSoapAction(soapAction + "");

        // check user has put any SOAPHeader using the call MEPClient methods and add them, if any, to the
        // the SOAP message
        addUserAddedSOAPHeaders(msgCtx);
    }

    private void addUserAddedSOAPHeaders(MessageContext msgCtx) {
        if (soapHeaderList != null && soapHeaderList.size() > 0 && msgCtx.getEnvelope() != null) {
            SOAPFactory soapFactory;
            SOAPHeader header = msgCtx.getEnvelope().getHeader();
            if (header == null) {
                soapFactory = getCorrectSOAPFactory(msgCtx);
                header = soapFactory.createSOAPHeader(msgCtx.getEnvelope());
            }
            if (!header.isComplete()) {
                header.build();
            }

            for (int i = 0; i < soapHeaderList.size(); i++) {
                OMElement headerBlock = (OMElement) soapHeaderList.get(i);
                header.addChild(headerBlock);
            }

        }
    }

    /**
     * Prepares the SOAPEnvelope using the payload.
     *
     * @param toSend
     * @return
     * @throws AxisFault
     */
    protected MessageContext prepareTheSOAPEnvelope(OMElement toSend) throws AxisFault {
        MessageContext msgctx = new MessageContext(serviceContext.getConfigurationContext());

        SOAPEnvelope envelope = createDefaultSOAPEnvelope();
        if (toSend != null) {
            envelope.getBody().addChild(toSend);
        }
        msgctx.setEnvelope(envelope);
        return msgctx;
    }

    /**
     * Infers the transport by looking at the URL. The URL can be http://
     * tcp:// mail:// local://. 
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
            return serviceContext.getConfigurationContext().getAxisConfiguration().getTransportOut(
                    new QName(transport));

        } else {
            throw new AxisFault(Messages.getMessage("cannotInferTransport"));
        }

    }

    /**
     * Creates SOAPEvelope(in terms of version) from the values set.
     *
     * @return
     * @throws AxisFault
     */
    public SOAPEnvelope createDefaultSOAPEnvelope() throws AxisFault {
        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            return OMAbstractFactory.getSOAP12Factory().getDefaultEnvelope();
        } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            return OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        } else {
            throw new AxisFault(Messages.getMessage("invaidSOAPversion"));
        }
    }

    /**
     * Engages a given module to the current invocation. But in order to call this method, the
     * module *MUST* be enabled ( i.e. picked up by the deployment and known to Axis Engine). If not,
     * an exception will be thrown. To be enabled, the modules are added to the AXIS2_REPOSITORY/modules directory.
     *
     * @param name
     * @throws AxisFault
     */
    public void engageModule(QName name) throws AxisFault {
        AxisConfiguration axisConf = serviceContext.getConfigurationContext().getAxisConfiguration();
        //if it is already engeged do not engege it agaien
        if (!axisConf.isEngaged(name)) {
            axisConf.engageModule(name);
        }
    }

    public MessageInformationHeaders cloneMIHeaders() {
        MessageInformationHeaders messageInformationHeaders = new MessageInformationHeaders();
        messageInformationHeaders.setAction(this.messageInformationHeaders.getAction());
        messageInformationHeaders.setFaultTo(this.messageInformationHeaders.getFaultTo());
        messageInformationHeaders.setFrom(this.messageInformationHeaders.getFrom());
        messageInformationHeaders.setMessageId(this.messageInformationHeaders.getMessageId());
        messageInformationHeaders.setRelatesTo(this.messageInformationHeaders.getRelatesTo());
        messageInformationHeaders.setReplyTo(this.messageInformationHeaders.getReplyTo());
        messageInformationHeaders.setTo(this.messageInformationHeaders.getTo());
        return messageInformationHeaders;
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
     * Setting to configure if an exception is to be thrown when the SOAP message has a fault.
     * If set to true, system throws an exeption with the details extracted from the fault message.
     * Else the response message is returned to the application, irrespective of whether it has a Fault or not.
     * 
     * @param exceptionToBeThrownOnSOAPFault 
     */
    public void setExceptionToBeThrownOnSOAPFault(boolean exceptionToBeThrownOnSOAPFault) {
        isExceptionToBeThrownOnSOAPFault = exceptionToBeThrownOnSOAPFault;
    }

    /**
     * Allows users to add their own headers to the out going message from the client. It is 
     * restrictive, in the sense, that user can set a 
     * header with only one text. <code><pre>&lt;HeaderBlockName&gt;your text&lt;/HeaderBlockName&gt;</pre></code>. A more flexible 
     * way is to use addSOAPHeader(OMElement).
     *
     * @param soapHeaderQName
     * @param soapHeaderText
     */
    public void addSOAPHeader(QName soapHeaderQName, String soapHeaderText) {
        OMElement omElement = OMAbstractFactory.getOMFactory().createOMElement(soapHeaderQName, null);
        omElement.setText(soapHeaderText);
        if (soapHeaderList == null) {
            soapHeaderList = new ArrayList();
        }
        soapHeaderList.add(omElement);
    }

    /**
     * Allows users to add a SOAP header block.
     * @param soapHeaderBlock
     */
    public void addSOAPHeader(OMElement soapHeaderBlock) {
        if (soapHeaderBlock == null) {
            // what are you trying to do here. You wanna set null to a header ??
            return;
        }
        if (soapHeaderList == null) {
            soapHeaderList = new ArrayList();
        }
        soapHeaderList.add(soapHeaderBlock);
    }

    //==============================================================================
    // Use these methods to set Addressing specific information to the SOAP envelope.
    //===============================================================================
    /**
     * @param action
     */
    public void setWsaAction(String action) {
        messageInformationHeaders.setAction(action);
    }

    /**
     * @param faultTo
     */
    public void setFaultTo(EndpointReference faultTo) {
        messageInformationHeaders.setFaultTo(faultTo);
    }

    /**
     * @param from
     */
    public void setFrom(EndpointReference from) {
        messageInformationHeaders.setFrom(from);
    }

    /**
     * @param messageId
     */
    public void setMessageId(String messageId) {
        messageInformationHeaders.setMessageId(messageId);
    }

    /**
     * @param relatesTo
     */
    public void setRelatesTo(org.apache.axis2.addressing.RelatesTo relatesTo) {
        messageInformationHeaders.setRelatesTo(relatesTo);
    }

    /**
     * @param replyTo
     */
    public void setReplyTo(EndpointReference replyTo) {
        messageInformationHeaders.setReplyTo(replyTo);
    }

    /**
     * @param to
     */
    public void setTo(EndpointReference to) {
        messageInformationHeaders.setTo(to);
    }

    // ==============================================================================


    private SOAPFactory getCorrectSOAPFactory(MessageContext msgCtx) {
        String soapNSURI = msgCtx.getEnvelope().getNamespace().getName();
        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapNSURI)) {
            return OMAbstractFactory.getSOAP11Factory();
        } else {
            return OMAbstractFactory.getSOAP12Factory();
        }
    }

    public void setServiceContext(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    public ServiceContext getServiceContext() {
        return serviceContext;
    }
}
