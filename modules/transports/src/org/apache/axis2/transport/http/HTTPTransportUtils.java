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


package org.apache.axis2.transport.http;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.Utils;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class HTTPTransportUtils {


    /**
     * @deprecated This was used only by the now deprecated processHTTPGetRequest() method.
     */
    public static SOAPEnvelope createEnvelopeFromGetRequest(String requestUrl,
                                                            Map map, ConfigurationContext configCtx)
            throws AxisFault {
        String[] values =
                Utils.parseRequestURLForServiceAndOperation(requestUrl,
                                                            configCtx.getServiceContextPath());
        if (values == null) {
            return new SOAP11Factory().getDefaultEnvelope();
        }

        if ((values[1] != null) && (values[0] != null)) {
            String srvice = values[0];
            AxisService service = configCtx.getAxisConfiguration().getService(srvice);
            if (service == null) {
                throw new AxisFault("service not found: " + srvice);
            }
            String operation = values[1];
            SOAPFactory soapFactory = new SOAP11Factory();
            SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
            OMNamespace omNs = soapFactory.createOMNamespace(service.getSchemaTargetNamespace(),
                                                             service.getSchemaTargetNamespacePrefix());
            soapFactory.createOMNamespace(service.getSchemaTargetNamespace(),
                                          service.getSchemaTargetNamespacePrefix());
            OMElement opElement = soapFactory.createOMElement(operation, omNs);
            Iterator it = map.keySet().iterator();

            while (it.hasNext()) {
                String name = (String) it.next();
                String value = (String) map.get(name);
                OMElement omEle = soapFactory.createOMElement(name, omNs);

                omEle.setText(value);
                opElement.addChild(omEle);
            }

            envelope.getBody().addChild(opElement);

            return envelope;
        } else {
            return null;
        }
    }

    /**
     * <p>
     * Checks whether MTOM needs to be enabled for the message represented by
     * the msgContext. We check value assigned to the "enableMTOM" property
     * either using the config files (axis2.xml, services.xml) or
     * programatically. Programatic configuration is given priority. If the
     * given value is "optional", MTOM will be enabled only if the incoming
     * message was an MTOM message.
     * </p>
     * 
     * @param msgContext the active MessageContext
     * @return true if SwA needs to be enabled
     */
    public static boolean doWriteMTOM(MessageContext msgContext) {
        boolean enableMTOM;
        Object enableMTOMObject = null;
        // First check the whether MTOM is enabled by the configuration
        // (Eg:Axis2.xml, services.xml)
        Parameter parameter = msgContext.getParameter(Constants.Configuration.ENABLE_MTOM);
        if (parameter != null) {
            enableMTOMObject = parameter.getValue();
        }
        // Check whether the configuration is overridden programatically..
        // Priority given to programatically setting of the value
        Object property = msgContext.getProperty(Constants.Configuration.ENABLE_MTOM);
        if (property != null) {
            enableMTOMObject = property;
        }
        enableMTOM = JavaUtils.isTrueExplicitly(enableMTOMObject);
        // Handle the optional value for enableMTOM
        // If the value for 'enableMTOM' is given as optional and if the request
        // message was a MTOM message we sent out MTOM
        if (!enableMTOM && msgContext.isDoingMTOM() && (enableMTOMObject instanceof String)) {
            if (((String) enableMTOMObject).equalsIgnoreCase(Constants.VALUE_OPTIONAL)) {
                enableMTOM = true;
            }
        }
        return enableMTOM;
    }

    /**
     * <p>
     * Checks whether SOAP With Attachments (SwA) needs to be enabled for the
     * message represented by the msgContext. We check value assigned to the
     * "enableSwA" property either using the config files (axis2.xml,
     * services.xml) or programatically. Programatic configuration is given
     * priority. If the given value is "optional", SwA will be enabled only if
     * the incoming message was SwA type.
     * </p>
     * 
     * @param msgContext the active MessageContext
     * @return true if SwA needs to be enabled
     */
    public static boolean doWriteSwA(MessageContext msgContext) {
        boolean enableSwA;
        Object enableSwAObject = null;
        // First check the whether SwA is enabled by the configuration
        // (Eg:Axis2.xml, services.xml)
        Parameter parameter = msgContext.getParameter(Constants.Configuration.ENABLE_SWA);
        if (parameter != null) {
            enableSwAObject = parameter.getValue();
        }
        // Check whether the configuration is overridden programatically..
        // Priority given to programatically setting of the value
        Object property = msgContext.getProperty(Constants.Configuration.ENABLE_SWA);
        if (property != null) {
            enableSwAObject = property;
        }
        enableSwA = JavaUtils.isTrueExplicitly(enableSwAObject);
        // Handle the optional value for enableSwA
        // If the value for 'enableSwA' is given as optional and if the request
        // message was a SwA message we sent out SwA
        if (!enableSwA && msgContext.isDoingSwA() && (enableSwAObject instanceof String)) {
            if (((String) enableSwAObject).equalsIgnoreCase(Constants.VALUE_OPTIONAL)) {
                enableSwA = true;
            }
        }
        return enableSwA;
    }

    /**
     * Utility method to query CharSetEncoding. First look in the
     * MessageContext. If it's not there look in the OpContext. Use the defualt,
     * if it's not given in either contexts.
     *
     * @param msgContext the active MessageContext
     * @return String the CharSetEncoding
     */
    public static String getCharSetEncoding(MessageContext msgContext) {
        String charSetEnc = (String) msgContext
                .getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

        if (charSetEnc == null) {
            OperationContext opctx = msgContext.getOperationContext();
            if (opctx != null) {
                charSetEnc = (String) opctx
                        .getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
            }
            /**
             * If the char set enc is still not found use the default
             */
            if (charSetEnc == null) {
                charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }
        }
        return charSetEnc;
    }

    /**
     * @param msgContext           - The MessageContext of the Request Message
     * @param out                  - The output stream of the response
     * @param soapAction           - SoapAction of the request
     * @param requestURI           - The URL that the request came to
     * @param configurationContext - The Axis Configuration Context
     * @param requestParameters    - The parameters of the request message
     * @return - boolean indication whether the operation was succesfull
     * @throws AxisFault - Thrown in case a fault occurs
     * @deprecated use RESTUtil.processURLRequest(MessageContext msgContext, OutputStream out, String contentType) instead
     */

    public static boolean processHTTPGetRequest(MessageContext msgContext,
                                                OutputStream out, String soapAction,
                                                String requestURI,
                                                ConfigurationContext configurationContext,
                                                Map requestParameters)
            throws AxisFault {
        if ((soapAction != null) && soapAction.startsWith("\"") && soapAction.endsWith("\"")) {
            soapAction = soapAction.substring(1, soapAction.length() - 1);
        }

        msgContext.setSoapAction(soapAction);
        msgContext.setTo(new EndpointReference(requestURI));
        msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);
        msgContext.setServerSide(true);
        SOAPEnvelope envelope = HTTPTransportUtils.createEnvelopeFromGetRequest(requestURI,
                                                                                requestParameters,
                                                                                configurationContext);

        if (envelope == null) {
            return false;
        } else {
            msgContext.setDoingREST(true);
            msgContext.setEnvelope(envelope);
            AxisEngine.receive(msgContext);
            return true;
        }
    }

    private static final int VERSION_UNKNOWN = 0;
    private static final int VERSION_SOAP11 = 1;
    private static final int VERSION_SOAP12 = 2;

    public static InvocationResponse processHTTPPostRequest(MessageContext msgContext,
                                                            InputStream in,
                                                            OutputStream out,
                                                            String contentType,
                                                            String soapActionHeader,
                                                            String requestURI)
            throws AxisFault {
        int soapVersion = VERSION_UNKNOWN;
        try {
            soapVersion = initializeMessageContext(msgContext, soapActionHeader, requestURI, contentType);
            msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);

            msgContext.setEnvelope(
                    TransportUtils.createSOAPMessage(
                            msgContext,
                            handleGZip(msgContext, in), 
                            contentType));
            return AxisEngine.receive(msgContext);
        } catch (SOAPProcessingException e) {
            throw AxisFault.makeFault(e);
        } catch (AxisFault e) {
            throw e;
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        } catch (OMException e) {
            throw AxisFault.makeFault(e);
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        } catch (FactoryConfigurationError e) {
            throw AxisFault.makeFault(e);
        } finally {
            if ((msgContext.getEnvelope() == null) && soapVersion != VERSION_SOAP11) {
                msgContext.setEnvelope(new SOAP12Factory().getDefaultEnvelope());
            }
        }
    }

    public static int initializeMessageContext(MessageContext msgContext,
                                                String soapActionHeader,
                                                String requestURI,
                                                String contentType) {
        int soapVersion = VERSION_UNKNOWN;
        // remove the starting and trailing " from the SOAP Action
        if ((soapActionHeader != null) 
                && soapActionHeader.length() > 0 
                && soapActionHeader.charAt(0) == '\"'
                && soapActionHeader.endsWith("\"")) {
            soapActionHeader = soapActionHeader.substring(1, soapActionHeader.length() - 1);
        }

        // fill up the Message Contexts
        msgContext.setSoapAction(soapActionHeader);
        msgContext.setTo(new EndpointReference(requestURI));
        msgContext.setServerSide(true);

        // get the type of char encoding
        String charSetEnc = BuilderUtil.getCharSetEncoding(contentType);
        if (charSetEnc == null) {
            charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }
        msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);

        if (contentType != null) {
            if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
                soapVersion = VERSION_SOAP12;
                TransportUtils.processContentTypeForAction(contentType, msgContext);
            } else if (contentType
                    .indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
                soapVersion = VERSION_SOAP11;
            } else if (isRESTRequest(contentType)) {
                // If REST, construct a SOAP11 envelope to hold the rest message and
                // indicate that this is a REST message.
                soapVersion = VERSION_SOAP11;
                msgContext.setDoingREST(true);
            }
            if (soapVersion == VERSION_SOAP11) {
                // TODO Keith : Do we need this anymore
                // Deployment configuration parameter
                Parameter enableREST = msgContext
                        .getParameter(Constants.Configuration.ENABLE_REST);
                if ((soapActionHeader == null) && (enableREST != null)) {
                    if (Constants.VALUE_TRUE.equals(enableREST.getValue())) {
                        // If the content Type is text/xml (BTW which is the
                        // SOAP 1.1 Content type ) and the SOAP Action is
                        // absent it is rest !!
                        msgContext.setDoingREST(true);
                    }
                }
            }
        }
        return soapVersion;
    }

    public static InputStream handleGZip(MessageContext msgContext, InputStream in)
            throws IOException {
        Map headers = (Map) msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);

        if (headers != null) {
            if (HTTPConstants.COMPRESSION_GZIP
                    .equals(headers.get(HTTPConstants.HEADER_CONTENT_ENCODING)) ||
                    HTTPConstants.COMPRESSION_GZIP.equals(headers.get(
                            HTTPConstants.HEADER_CONTENT_ENCODING_LOWERCASE))) {
                in = new GZIPInputStream(in);
            }
        }
        return in;
    }

    public static boolean isDoingREST(MessageContext msgContext) {
        boolean enableREST = false;

        // check whether isDoingRest is already true in the message context
        if (msgContext.isDoingREST()) {
            return true;
        }

        Object enableRESTProperty = msgContext.getProperty(Constants.Configuration.ENABLE_REST);
        if (enableRESTProperty != null) {
            enableREST = JavaUtils.isTrueExplicitly(enableRESTProperty);
        }

        msgContext.setDoingREST(enableREST);

        return enableREST;
    }

    /**
     * This will match for content types that will be regarded as REST in WSDL2.0.
     * This contains,
     * 1. application/xml
     * 2. application/x-www-form-urlencoded
     * 3. multipart/form-data
     * <p/>
     * If the request doesnot contain a content type; this will return true.
     *
     * @param contentType content type to check
     * @return Boolean
     */
    public static boolean isRESTRequest(String contentType) {
        return contentType != null &&
               (contentType.indexOf(HTTPConstants.MEDIA_TYPE_APPLICATION_XML) > -1 ||
                contentType.indexOf(HTTPConstants.MEDIA_TYPE_X_WWW_FORM) > -1 ||
                contentType.indexOf(HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA) > -1);
    }
}
