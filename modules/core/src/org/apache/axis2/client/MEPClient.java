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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.ws.commons.soap.SOAP11Constants;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.SOAPHeader;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the super class for all MEPClients.
 *
 * @see OperationClient
 * @deprecated
 */
public abstract class MEPClient {
    private static final String ANONYMOUS_SERVICE = "AnonymousService";

    /**
     * Client will pass all the parameters to this invocation using this.
     */
    protected Options clientOptions;
    protected final String mep;
    ServiceContext serviceContext;
    private List soapHeaderList;

    public MEPClient(ServiceContext service, String mep) {
        this.serviceContext = service;
        this.mep = mep;
    }

    /**
     * Allows users to add a SOAP header block.
     *
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

    /**
     * Allows users to add their own headers to the out going message from the
     * client. It is restrictive, in the sense, that user can set a header with
     * only one text. <code><pre>
     *    &lt;HeaderBlockName&gt;your text&lt;/HeaderBlockName&gt;
     * </pre></code>. A more flexible way is to use addSOAPHeader(OMElement).
     *
     * @param soapHeaderQName
     * @param soapHeaderText
     */
    public void addSOAPHeader(QName soapHeaderQName, String soapHeaderText) {
        OMElement omElement = OMAbstractFactory.getOMFactory().createOMElement(soapHeaderQName,
                null);

        omElement.setText(soapHeaderText);

        if (soapHeaderList == null) {
            soapHeaderList = new ArrayList();
        }

        soapHeaderList.add(omElement);
    }

    protected void addUserAddedSOAPHeaders(MessageContext msgCtx, Options options) {
        if ((soapHeaderList != null) && (soapHeaderList.size() > 0)
                && (msgCtx.getEnvelope() != null)) {
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
     * Assumes the values for the ConfigurationContext and ServiceContext to
     * make the NON WSDL cases simple.
     *
     * @throws AxisFault
     */
    protected void assumeServiceContext(String clientHome) throws AxisFault {
        ConfigurationContext configurationContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(clientHome, null);
        AxisService axisService =
                configurationContext.getAxisConfiguration().getService(ANONYMOUS_SERVICE);

        if (axisService == null) {

            // we will assume a Service and operations
            axisService = new AxisService(ANONYMOUS_SERVICE);
        }

        configurationContext.getAxisConfiguration().addService(axisService);
        serviceContext = new ServiceGroupContext(configurationContext,
                (AxisServiceGroup) axisService.getParent()).getServiceContext(axisService);
    }

    /**
     * This gives chance to the derived class to configure its transport
     * from the information injected by the user via options. This is
     * called within the prepare invocation method, so user does not need to
     * call this explicitly.
     */
    protected abstract void configureTransportInformation(MessageContext msgCtxt) throws AxisFault;

    /**
     * Creates SOAPEvelope(in terms of version) from the values set.
     *
     * @return Returns SOAPEnvelope.
     * @throws AxisFault
     */
    protected SOAPEnvelope createDefaultSOAPEnvelope() throws AxisFault {

        // I added code to check the nullity in the prepareInvocation(). But it
        // seems that this method
        // can be called before prepareInvocation().
        if (clientOptions == null) {
            throw new AxisFault(
                    "Can not proceed without options being set for invocation. Set the"
                            + "properties for this invocation via MEPClient.setOptions(Options) first.");
        }

        String soapVersionURI = clientOptions.getSoapVersionURI();
        String soapFactory =
                (String) clientOptions.getProperty(OMAbstractFactory.SOAP_FACTORY_NAME_PROPERTY);

        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            String factory = (String) clientOptions.getProperty(OMAbstractFactory.SOAP12_FACTORY_NAME_PROPERTY);
            if (factory != null) {
                return OMAbstractFactory.getSOAPFactory(soapFactory).getDefaultEnvelope();
            } else {
                return OMAbstractFactory.getSOAP12Factory().getDefaultEnvelope();
            }
        } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            String factory = (String) clientOptions.getProperty(OMAbstractFactory.SOAP11_FACTORY_NAME_PROPERTY);
            if (factory != null) {
                return OMAbstractFactory.getSOAPFactory(soapFactory).getDefaultEnvelope();
            } else {
                return OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
            }
        } else if ("".equals(soapVersionURI) || (soapVersionURI == null)) {
            return OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        } else {
            throw new AxisFault(Messages.getMessage("invaidSOAPversion"));
        }
    }

    /**
     * Engages a given module to the current invocation. But in order to call
     * this method, the module *MUST* be enabled ( i.e. picked up by the
     * deployment and known to Axis Engine). If not, an exception will be
     * thrown. To be enabled, the modules are added to the
     * AXIS2_REPOSITORY/modules directory.
     *
     * @param name
     * @throws AxisFault
     */
    public void engageModule(QName name) throws AxisFault {
        AxisConfiguration axisConf =
                serviceContext.getConfigurationContext().getAxisConfiguration();

        // if it is already engeged do not engage it again
        if (!axisConf.isEngaged(name)) {
            axisConf.engageModule(name);
        }
    }

    /**
     * Infers the transport by looking at the URL. The URL can be http:// tcp:/
     * mail:// local://.
     *
     * @param epr
     * @return Returns TransportOutDescription.
     * @throws AxisFault
     */
    protected TransportOutDescription inferTransport(EndpointReference epr) throws AxisFault {
        if (epr != null) {
            return inferTransport(epr.getAddress());
        } else {
            throw new AxisFault(Messages.getMessage("cannotInferTransport"));
        }
    }

    protected TransportOutDescription inferTransport(String uri) throws AxisFault {
        String transport = null;

        if (uri != null) {
            int index = uri.indexOf(':');

            if (index > 0) {
                transport = uri.substring(0, index);
            }
        }

        if (transport != null) {
            return serviceContext.getConfigurationContext().getAxisConfiguration().getTransportOut(
                    new QName(transport));
        } else {
            throw new AxisFault(Messages.getMessage("cannotInferTransport"));
        }
    }

    protected void inferTransportOutDescription(MessageContext msgCtx) throws AxisFault {

        // user can set the transport by giving a TransportOutDescription or we
        // will deduce that from the "to" epr information, if user has not set the
        // TransportOutDescription, lets infer that
        if (clientOptions.getTranportOut() == null) {
            AxisConfiguration axisConfig =
                    this.serviceContext.getConfigurationContext().getAxisConfiguration();

            // we have a deprecated method for user to set the transport
            // protocol directly. Lets support that also
            String senderTrasportProtocol = clientOptions.getSenderTransportProtocol();

            if (axisConfig != null) {
                if ((senderTrasportProtocol == null) || "".equals(senderTrasportProtocol)) {

                    // by this time we have passed all the information we
                    // collected via Options to the message context
                    clientOptions.setTranportOut(inferTransport(msgCtx.getTo()));
                } else {

                    // if he has not set the transport information, we gonna
                    // infer that from the to EPR
                    clientOptions.setTranportOut(
                            axisConfig.getTransportOut(new QName(senderTrasportProtocol)));
                }
            }

            if (this.clientOptions.getTranportOut() == null) {
                throw new AxisFault(Messages.getMessage("unknownTransport",
                        senderTrasportProtocol));
            }
        }
    }

    /**
     * Prepares the message context for invocation. The properties in the
     * MEPClient are copied to the MessageContext.
     */
    protected void prepareInvocation(AxisOperation axisop, MessageContext msgCtx) throws AxisFault {

        // user must provide the minimum information for the engine to proceed
        // with the invocation.
        // For the time being, I think he should at least provide the toEPR. So
        // I should check that is
        // available either from the message context or from the options.
        if (((msgCtx == null) || (msgCtx.getTo() == null))
                && ((clientOptions == null) || (clientOptions.getTo() == null))) {
            throw new AxisFault(
                    "Can not proceed without options being set for invocation. Set the"
                            + "properties for this invocation via MEPClient.setOptions(Options) first.");
        }

        if (axisop == null) {
            throw new AxisFault(Messages.getMessage("cannotBeNullAxisOperation"));
        }

        // make sure operation is type right MEP
        if (mep.equals(axisop.getMessageExchangePattern())) {
            throw new AxisFault(Messages.getMessage("mepClientSupportOnly", mep,
                    axisop.getMessageExchangePattern()));
        }

        // if operation not alrady added, add it
        if (serviceContext.getAxisService().getOperation(axisop.getName()) == null) {
            serviceContext.getAxisService().addOperation(axisop);
        }

        // now its the time to put the parameters set by the user in to the
        // correct places and to the
        // if there is no message id still, set a new one.
        String messageId = clientOptions.getMessageId();
        if (messageId == null || "".equals(messageId)) {
            clientOptions.setMessageId(UUIDGenerator.getUUID());
        }
        msgCtx.setOptions(clientOptions);

        // check user has put any SOAPHeader using the call MEPClient methods
        // and add them, if any, to the
        // the SOAP message
        addUserAddedSOAPHeaders(msgCtx, clientOptions);

        // find and set the transport details
        configureTransportInformation(msgCtx);
    }

    /**
     * Prepares the SOAPEnvelope using the payload.
     *
     * @param toSend
     * @return Returns MessageContext.
     * @throws AxisFault
     */
    protected MessageContext prepareTheSOAPEnvelope(OMElement toSend) throws AxisFault {
        MessageContext msgctx = new MessageContext();
        msgctx.setConfigurationContext(serviceContext.getConfigurationContext());
        SOAPEnvelope envelope = createDefaultSOAPEnvelope();

        if (toSend != null) {
            envelope.getBody().addChild(toSend);
        }

        msgctx.setEnvelope(envelope);

        return msgctx;
    }

    public Options getClientOptions() {
        return clientOptions;
    }

    private SOAPFactory getCorrectSOAPFactory(MessageContext msgCtx) {
        String soapNSURI = msgCtx.getEnvelope().getNamespace().getName();

        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapNSURI)) {
            return OMAbstractFactory.getSOAP11Factory();
        } else {
            return OMAbstractFactory.getSOAP12Factory();
        }
    }

    public ServiceContext getServiceContext() {
        return serviceContext;
    }

    /**
     * Sets all client options and parameters for this invocation.
     *
     * @param clientOptions
     * @see Options for more details.
     */
    public void setClientOptions(Options clientOptions) {
        this.clientOptions = clientOptions;
    }

    public void setServiceContext(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }
}
