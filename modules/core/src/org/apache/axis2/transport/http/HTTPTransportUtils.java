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


package org.apache.axis2.transport.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.Utils;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMException;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.om.OMNode;
import org.apache.ws.commons.om.OMText;
import org.apache.ws.commons.om.impl.llom.OMNamespaceImpl;
import org.apache.ws.commons.om.impl.llom.builder.StAXBuilder;
import org.apache.ws.commons.om.impl.llom.builder.StAXOMBuilder;
import org.apache.ws.commons.soap.SOAP11Constants;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.SOAPProcessingException;
import org.apache.ws.commons.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.ws.commons.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.ws.commons.soap.impl.llom.soap12.SOAP12Factory;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

public class HTTPTransportUtils {

    public static boolean checkEnvelopeForOptimise(SOAPEnvelope envelope) {
        return isOptimised(envelope);
    }

    public static SOAPEnvelope createEnvelopeFromGetRequest(String requestUrl, Map map) {
        String[] values = Utils.parseRequestURLForServiceAndOperation(requestUrl);

        if (values == null) {
            return new SOAP11Factory().getDefaultEnvelope();
        }

        if ((values[1] != null) && (values[0] != null)) {
            String operation = values[1];
            SOAPFactory soapFactory = new SOAP11Factory();
            SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
            OMNamespace omNs = soapFactory.createOMNamespace(values[0], "services");
            //OMNamespace defualtNs = new OMNamespaceImpl("", null, soapFactory);
            OMNamespace defualtNs = soapFactory.createOMNamespace("", null);
            OMElement opElement = soapFactory.createOMElement(operation, omNs);
            Iterator it = map.keySet().iterator();

            while (it.hasNext()) {
                String name = (String) it.next();
                String value = (String) map.get(name);
                OMElement omEle = soapFactory.createOMElement(name, defualtNs);

                omEle.setText(value);
                opElement.addChild(omEle);
            }

            envelope.getBody().addChild(opElement);

            return envelope;
        } else {
            return null;
        }
    }

    public static boolean doWriteMTOM(MessageContext msgContext) {
        boolean enableMTOM = false;

        if (msgContext.getParameter(Constants.Configuration.ENABLE_MTOM) != null) {
            enableMTOM = Constants.VALUE_TRUE.equals(
                    msgContext.getParameter(Constants.Configuration.ENABLE_MTOM).getValue());
        }

        if (msgContext.getProperty(Constants.Configuration.ENABLE_MTOM) != null) {
            enableMTOM = Constants.VALUE_TRUE.equals(
                    msgContext.getProperty(Constants.Configuration.ENABLE_MTOM));
        }

        boolean forceMIME =
                Constants.VALUE_TRUE.equals(msgContext.getProperty(Constants.Configuration.FORCE_MIME));

        if (forceMIME) {
            return true;
        }

        // If MTOM is explicitly disabled, no need to check the envelope
        if (!enableMTOM) {
            return false;
        }

        boolean envelopeContainsOptimise =
                HTTPTransportUtils.checkEnvelopeForOptimise(msgContext.getEnvelope());

        return enableMTOM && envelopeContainsOptimise;
    }

    public static boolean processHTTPGetRequest(MessageContext msgContext, InputStream in,
                                                OutputStream out, String contentType, String soapAction, String requestURI,
                                                ConfigurationContext configurationContext, Map requestParameters)
            throws AxisFault {
        if ((soapAction != null) && soapAction.startsWith("\"") && soapAction.endsWith("\"")) {
            soapAction = soapAction.substring(1, soapAction.length() - 1);
        }

        msgContext.setWSAAction(soapAction);
        msgContext.setSoapAction(soapAction);
        msgContext.setTo(new EndpointReference(requestURI));
        msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);
        msgContext.setServerSide(true);

        SOAPEnvelope envelope = HTTPTransportUtils.createEnvelopeFromGetRequest(requestURI,
                requestParameters);

        if (envelope == null) {
            return false;
        } else {
            msgContext.setDoingREST(true);
            msgContext.setEnvelope(envelope);

            AxisEngine engine = new AxisEngine(configurationContext);

            engine.receive(msgContext);

            return true;
        }
    }

    public static void processHTTPPostRequest(MessageContext msgContext, InputStream in,
                                              OutputStream out, String contentType, String soapActionHeader, String requestURI)
            throws AxisFault {
        boolean soap11 = false;

        try {

            // remove the starting and trailing " from the SOAP Action
            if ((soapActionHeader != null) && soapActionHeader.startsWith("\"")
                    && soapActionHeader.endsWith("\"")) {
                soapActionHeader = soapActionHeader.substring(1, soapActionHeader.length() - 1);
            }

            // fill up the Message Contexts
            msgContext.setWSAAction(soapActionHeader);
            msgContext.setSoapAction(soapActionHeader);
            msgContext.setTo(new EndpointReference(requestURI));
            msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);
            msgContext.setServerSide(true);

            SOAPEnvelope envelope = null;
            StAXBuilder builder = null;

            if (contentType != null) {
                if (contentType.indexOf(HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED) > -1) {

                    // It is MTOM
                    builder = TransportUtils.selectBuilderForMIME(msgContext, in, contentType);
                    envelope = (SOAPEnvelope) builder.getDocumentElement();
                } else {
                    XMLStreamReader xmlreader;

                    // Figure out the char set encoding and create the reader

                    // If charset is not specified
                    if (TransportUtils.getCharSetEncoding(contentType) == null) {
                        xmlreader = XMLInputFactory.newInstance().createXMLStreamReader(in,
                                MessageContext.DEFAULT_CHAR_SET_ENCODING);

                        // Set the encoding scheme in the message context
                        msgContext.setProperty(MessageContext.CHARACTER_SET_ENCODING,
                                MessageContext.DEFAULT_CHAR_SET_ENCODING);
                    } else {

                        // get the type of char encoding
                        String charSetEnc = TransportUtils.getCharSetEncoding(contentType);

                        xmlreader = XMLInputFactory.newInstance().createXMLStreamReader(in,
                                charSetEnc);

                        // Setting the value in msgCtx
                        msgContext.setProperty(MessageContext.CHARACTER_SET_ENCODING, charSetEnc);
                    }

                    if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
                        soap11 = false;

                        // it is SOAP 1.2
                        builder =
                                new StAXSOAPModelBuilder(xmlreader,
                                        SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
                        envelope = (SOAPEnvelope) builder.getDocumentElement();
                    } else if (contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
                        soap11 = true;

                        /**
                         * Configuration via Deployment
                         */
                        Parameter enable =
                                msgContext.getParameter(Constants.Configuration.ENABLE_REST);

                        if (((soapActionHeader == null) || (soapActionHeader.length() == 0))
                                && (enable != null)) {
                            if (Constants.VALUE_TRUE.equals(enable.getValue())) {

                                // If the content Type is text/xml (BTW which is the SOAP 1.1 Content type ) and
                                // the SOAP Action is absent it is rest !!
                                msgContext.setDoingREST(true);

                                SOAPFactory soapFactory = new SOAP11Factory();

                                builder = new StAXOMBuilder(xmlreader);
                                builder.setOmbuilderFactory(soapFactory);
                                envelope = soapFactory.getDefaultEnvelope();
                                envelope.getBody().addChild(builder.getDocumentElement());
                            }
                        } else {
                            builder = new StAXSOAPModelBuilder(
                                    xmlreader, SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
                            envelope = (SOAPEnvelope) builder.getDocumentElement();
                        }
                    } else {
                        builder = new StAXSOAPModelBuilder(
                                xmlreader, SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
                        envelope = (SOAPEnvelope) builder.getDocumentElement();
                    }
                }
            }

            String charsetEncoding = builder.getDocument().getCharsetEncoding();

            if ((charsetEncoding != null) && !"".equals(charsetEncoding)
                    && ! charsetEncoding.equalsIgnoreCase((String) msgContext.getProperty(
                    MessageContext.CHARACTER_SET_ENCODING))) {
                String faultCode;

                if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(
                        envelope.getNamespace().getName())) {
                    faultCode = SOAP12Constants.FAULT_CODE_SENDER;
                } else {
                    faultCode = SOAP11Constants.FAULT_CODE_SENDER;
                }

                throw new AxisFault(
                        "Character Set Encoding from " + "transport information do not match with "
                                + "character set encoding in the received SOAP message", faultCode);
            }

            msgContext.setEnvelope(envelope);

            AxisEngine engine = new AxisEngine(msgContext.getConfigurationContext());

            if (envelope.getBody().hasFault()) {
                engine.receiveFault(msgContext);
            } else {
                engine.receive(msgContext);
            }
        } catch (SOAPProcessingException e) {
            throw new AxisFault(e);
        } catch (AxisFault e) {
            throw e;
        } catch (OMException e) {
            throw new AxisFault(e);
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError e) {
            throw new AxisFault(e);
        } catch (UnsupportedEncodingException e) {
            throw new AxisFault(e);
        } finally {
            if ((msgContext.getEnvelope() == null) && !soap11) {
                msgContext.setEnvelope(new SOAP12Factory().createSOAPEnvelope());
            }
        }
    }

    public static boolean isDoingREST(MessageContext msgContext) {
        boolean enableREST = false;

        // check whether isDoingRest is already true in the message context
        if (msgContext.isDoingREST()) {
            return true;
        }

        Parameter parameter = msgContext.getParameter(Constants.Configuration.ENABLE_REST);

        if (parameter != null) {
            enableREST = Constants.VALUE_TRUE.equals(parameter.getValue());
        } else if (msgContext.getProperty(Constants.Configuration.ENABLE_REST) != null) {
            enableREST = Constants.VALUE_TRUE.equals(
                    msgContext.getProperty(Constants.Configuration.ENABLE_REST));
        }

        msgContext.setDoingREST(enableREST);

        return enableREST;
    }

    private static boolean isOptimised(OMElement element) {
        Iterator childrenIter = element.getChildren();
        boolean isOptimized = false;

        while (childrenIter.hasNext() && !isOptimized) {
            OMNode node = (OMNode) childrenIter.next();

            if ((OMNode.TEXT_NODE == node.getType()) && ((OMText) node).isOptimized()) {
                isOptimized = true;
            } else if (OMNode.ELEMENT_NODE == node.getType()) {
                isOptimized = isOptimised((OMElement) node);
            }
        }

        return isOptimized;
    }
}
