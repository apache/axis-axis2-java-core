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


    public static SOAPEnvelope createEnvelopeFromGetRequest(String requestUrl,
                                   Map map,ConfigurationContext configCtx) throws AxisFault {
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
            OMNamespace omNs = soapFactory.createOMNamespace(service.getSchematargetNamespace(),
                                                             service.getSchematargetNamespacePrefix());
            soapFactory.createOMNamespace(service.getSchematargetNamespace(),
                                          service.getSchematargetNamespacePrefix());
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

    public static boolean doWriteMTOM(MessageContext msgContext) {
        // check whether isDoingMTOM is already true in the message context
//        if (msgContext.isDoingMTOM()) {
//            return true;
//        }
        
        boolean enableMTOM = false;
        Parameter parameter = msgContext.getParameter(Constants.Configuration.ENABLE_MTOM);
        if (parameter != null) {
            enableMTOM = JavaUtils.isTrueExplicitly(
                    parameter.getValue());
        }
        Object property = msgContext.getProperty(Constants.Configuration.ENABLE_MTOM);
        if (property != null) {
            enableMTOM = JavaUtils.isTrueExplicitly(property);
        }
        return enableMTOM;
    }
    
    public static boolean doWriteSwA(MessageContext msgContext) {
        // check whether isDoingSWA is already true in the message context
//        if (msgContext.isDoingSwA()) {
//            return true;
//        }
        boolean enableSwA = false;
        Parameter parameter = msgContext.getParameter(Constants.Configuration.ENABLE_SWA);
        if (parameter != null) {
            enableSwA = JavaUtils.isTrueExplicitly(
                    parameter.getValue());
        }
        Object property = msgContext.getProperty(Constants.Configuration.ENABLE_SWA);
        if (property != null) {
            enableSwA = JavaUtils.isTrueExplicitly(property);
        }
        return enableSwA;
    }
    
    /**
     * Utility method to query CharSetEncoding. First look in the
     * MessageContext. If it's not there look in the OpContext. Use the defualt,
     * if it's not given in either contexts.
     * 
     * @param msgContext
     * @return CharSetEncoding
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

//    public static boolean processHTTPGetRequest(MessageContext msgContext,
//                                                OutputStream out, String soapAction, String requestURI,
//                                                ConfigurationContext configurationContext, Map requestParameters)
//            throws AxisFault {
//        if ((soapAction != null) && soapAction.startsWith("\"") && soapAction.endsWith("\"")) {
//            soapAction = soapAction.substring(1, soapAction.length() - 1);
//        }
//
//        msgContext.setSoapAction(soapAction);
//        msgContext.setTo(new EndpointReference(requestURI));
//        msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);
//        msgContext.setServerSide(true);
//        SOAPEnvelope envelope = HTTPTransportUtils.createEnvelopeFromGetRequest(requestURI,
//                                                                                requestParameters, configurationContext);
//
//        if (envelope == null) {
//            return false;
//        } else {
//            msgContext.setDoingREST(true);
//            msgContext.setEnvelope(envelope);
//            AxisEngine engine = new AxisEngine(configurationContext);
//            engine.receive(msgContext);
//            return true;
//        }
//    }

    private static final int VERSION_UNKNOWN = 0;
    private static final int VERSION_SOAP11 = 1;
    private static final int VERSION_SOAP12 = 2;

    public static InvocationResponse processHTTPPostRequest(MessageContext msgContext, InputStream in,
                                              OutputStream out, String contentType, String soapActionHeader, String requestURI)
            throws AxisFault {
        int soapVersion = VERSION_UNKNOWN;
        InvocationResponse pi = InvocationResponse.CONTINUE;
        
        try {

            in = handleGZip(msgContext, in);

            // remove the starting and trailing " from the SOAP Action
            if ((soapActionHeader != null) && soapActionHeader.charAt(0) == '\"'
                && soapActionHeader.endsWith("\"")) {
                soapActionHeader = soapActionHeader.substring(1, soapActionHeader.length() - 1);
            }

            // fill up the Message Contexts
            msgContext.setSoapAction(soapActionHeader);
            msgContext.setTo(new EndpointReference(requestURI));
            msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);
            msgContext.setServerSide(true);

            SOAPEnvelope envelope = null;
            boolean isMIME=false;

            // get the type of char encoding
            String charSetEnc = BuilderUtil.getCharSetEncoding(contentType);
            if(charSetEnc == null){
                charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }
            msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);

            if (contentType != null) {
				if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
					soapVersion = VERSION_SOAP12;
					processContentTypeForAction(contentType, msgContext);
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
            envelope = TransportUtils.createSOAPMessage(msgContext,in,contentType);
            msgContext.setEnvelope(envelope);
            AxisEngine engine = new AxisEngine(msgContext.getConfigurationContext());

            if (envelope.getBody().hasFault()) {
                pi = engine.receiveFault(msgContext);
            } else {
                pi = engine.receive(msgContext);
            }
            
            return pi;
        } catch (SOAPProcessingException e) {
            throw new AxisFault(e);
        } catch (AxisFault e) {
            throw e;
        } catch (IOException e) {
            throw new AxisFault(e);
        } catch (OMException e) {
            throw new AxisFault(e);
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError e) {
            throw new AxisFault(e);
        } finally {
            if ((msgContext.getEnvelope() == null) && soapVersion != VERSION_SOAP11) {
                msgContext.setEnvelope(new SOAP12Factory().getDefaultEnvelope());
            }
        }
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

    private static void processContentTypeForAction(String contentType, MessageContext msgContext) {
        //Check for action header and set it in as soapAction in MessageContext
        int index = contentType.indexOf("action");
        if (index > -1) {
            String transientString = contentType.substring(index, contentType.length());
            int equal = transientString.indexOf("=");
            int firstSemiColon = transientString.indexOf(";");
            String soapAction; // This will contain "" in the string
            if (firstSemiColon > -1) {
                soapAction = transientString.substring(equal + 1, firstSemiColon);
            } else {
                soapAction = transientString.substring(equal + 1, transientString.length());
            }
            if ((soapAction != null) && soapAction.startsWith("\"")
                && soapAction.endsWith("\"")) {
                soapAction = soapAction
                        .substring(1, soapAction.length() - 1);
            }
            msgContext.setSoapAction(soapAction);
        }
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
     * @param contentType
     * @return Boolean
     */
    public static boolean isRESTRequest(String contentType) {
        if (contentType == null) {
            return false;
        }
        return ( contentType.indexOf(HTTPConstants.MEDIA_TYPE_APPLICATION_XML) > -1 ||
                 contentType.indexOf(HTTPConstants.MEDIA_TYPE_X_WWW_FORM) > -1 ||
                 contentType.indexOf(HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA) > -1);
    }
}
