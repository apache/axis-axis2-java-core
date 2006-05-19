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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.AxisFault;

import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;

public abstract class
        Stub {

    protected static AxisService _service;
    protected ArrayList modules = new ArrayList();


    protected ServiceClient _serviceClient;

    public ServiceClient _getServiceClient() {
        return _serviceClient;
    }

    public void _setServiceClient(ServiceClient _serviceClient) {
        this._serviceClient = _serviceClient;
    }

    protected SOAPEnvelope createEnvelope(Options options) throws SOAPProcessingException {
        return getFactory(options.getSoapVersionURI()).getDefaultEnvelope();
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

        if (WSDLConstants.STYLE_RPC.equals(type)) {
            return element.getFirstElement();    // todo this needs to be fixed
        } else if (WSDLConstants.STYLE_DOC.equals(type)) {
            return element;
        } else {
            throw new UnsupportedOperationException(Messages
                    .getMessage("unsupportedType"));
        }
    }

    protected OMElement getElementFromReader(XMLStreamReader reader) {
        StAXOMBuilder builder =
                OMXMLBuilderFactory.createStAXOMBuilder(OMAbstractFactory.getOMFactory(), reader);

        return builder.getDocumentElement();
    }

    protected SOAPFactory getFactory(String soapVersionURI) {

        if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            return OMAbstractFactory.getSOAP11Factory();
        } else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            return OMAbstractFactory.getSOAP12Factory();
        } else {
            throw new RuntimeException(Messages
                    .getMessage("unknownsoapversion"));
        }
    }

    /**
     * Finalize overridden to cleanup
     * @throws Throwable
     */
    protected void finalize() throws Throwable {
         super.finalize();
         cleanup();
    }

    /**
     * Cleanup by removing the axis service
     * @throws AxisFault
     */
    public void cleanup() throws AxisFault {
        _service.getAxisConfiguration().removeService(_service.getName());
    }

}
