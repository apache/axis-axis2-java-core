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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis2.soap.*;
import org.apache.wsdl.WSDLService;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;

public abstract class Stub {
    protected static AxisService _service;
    protected ArrayList modules = new ArrayList();

    /**
     * If _maintainSession is set to true, all the calls can use the same
     * ServiceContext. The user can share information through this
     * ServiceContext across operations.
     */
    protected boolean _maintainSession;
    protected String _currentSessionId;
    protected Options _clientOptions = new Options();
    protected ConfigurationContext _configurationContext;
    protected ServiceContext _serviceContext;

    protected Stub() {
    }

    public void _endSession() {
        _maintainSession = false;
    }

    public Options _getClientOptions() {
        return _clientOptions;
    }

    protected String _getServiceContextID() {
        if (_maintainSession) {
            return _currentSessionId;
        } else {
            return getID();
        }
    }

    public Object _getSessionInfo(String key) throws Exception {
        if (!_maintainSession) {

            // TODO Comeup with a Exception
            throw new Exception(
                    "Client is running the session OFF mode: Start session before saving to a session ");
        }
        return null;
//        return _configurationContext.getServiceContext(_currentSessionId).getProperty(key);
    }

    public void _setClientOptions(Options _clientOptions) {
        this._clientOptions = _clientOptions;
    }

    public void _setSessionInfo(String key, Object value) throws Exception {
        if (!_maintainSession) {

            // TODO Comeup with a Exception
            throw new Exception(
                    "Client is running the session OFF mode: Start session before saving to a session ");
        }
//        _configurationContext.getServiceContext(_currentSessionId).setProperty(key, value);
    }

    public void _startSession() {
        _maintainSession = true;
        _currentSessionId = getID();
    }

    protected SOAPEnvelope createEnvelope() throws SOAPProcessingException {
        return getFactory(this._clientOptions.getSoapVersionURI()).getDefaultEnvelope();
    }

    public void engageModule(String moduleName) {
        this.modules.add(moduleName);
    }

    protected void populateModules(Call call) throws AxisFault {
        for (int i = 0; i < modules.size(); i++) {
            call.engageModule(new QName((String) this.modules.get(i)));
        }
    }

    protected void populateModules(MessageSender sender) throws AxisFault {
        for (int i = 0; i < modules.size(); i++) {
            sender.engageModule(new QName((String) this.modules.get(i)));
        }
    }

    /**
     * A util method that extracts the correct element.
     *
     * @param env
     * @param type
     * @return the relevant element to be databound
     */
    protected OMElement getElement(SOAPEnvelope env, String type) {
        SOAPBody body = env.getBody();
        OMElement element = body.getFirstElement();

        if (WSDLService.STYLE_RPC.equals(type)) {
            return element.getFirstElement();    // todo this needs to be fixed
        } else if (WSDLService.STYLE_DOC.equals(type)) {
            return element;
        } else {
            throw new UnsupportedOperationException("Unsupported type");
        }
    }

    protected OMElement getElementFromReader(XMLStreamReader reader) {
        StAXOMBuilder builder =
                OMXMLBuilderFactory.createStAXOMBuilder(OMAbstractFactory.getOMFactory(), reader);

        return builder.getDocumentElement();
    }

    protected SOAPFactory getFactory(String soapNamespaceURI) {
        String soapVersionURI = _clientOptions.getSoapVersionURI();

        if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            return OMAbstractFactory.getSOAP11Factory();
        } else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            return OMAbstractFactory.getSOAP12Factory();
        } else {
            throw new RuntimeException("Unknown SOAP version");
        }
    }

    private String getID() {

        // TODO Get the UUID generator to generate values
        return Long.toString(System.currentTimeMillis());
    }

    /**
     * Gets the message context.
     */
    protected MessageContext getMessageContext() throws AxisFault {
        MessageContext messageContext = new MessageContext();
        messageContext.setConfigurationContext(_configurationContext);
        return messageContext;
    }
}
