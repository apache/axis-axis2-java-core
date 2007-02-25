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
package org.apache.axis2.transport.http.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.impl.OMNodeEx;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.HTTPLocationBasedDispatcher;
import org.apache.axis2.engine.RequestURIBasedDispatcher;
import org.apache.axis2.engine.RequestURIOperationDispatcher;
import org.apache.axis2.engine.SOAPActionBasedDispatcher;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.Builder;
import org.apache.axis2.util.SchemaUtil;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.ws.commons.schema.XmlSchemaElement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 *
 */
public class RESTUtil {
    protected ConfigurationContext configurationContext;

    public RESTUtil(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public boolean processPostRequest(MessageContext msgContext,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws AxisFault {
        try {
            // 1. if the content type is text/xml or multipart/related, all the information
            // SHOULD be in HTTP body. So consruct a SOAP Envelope, out of the
            // the input stream extracted from the HTTP request,
            // set that to msgCtxt and return. Do we need to verify this
            // with the schema here ???
            String contentType = request.getContentType();
            SOAPEnvelope soapEnvelope;
            if ("".equals(contentType) || contentType == null) {
                throw new AxisFault("ContentType should be given to proceed," +
                        " according to WSDL 2.0 HTTP binding rules");
            } else if (contentType.indexOf(HTTPConstants.MEDIA_TYPE_TEXT_XML) > -1 ||
                    contentType.indexOf(HTTPConstants.MEDIA_TYPE_MULTIPART_RELATED) > -1  ||
                    contentType.indexOf(HTTPConstants.MEDIA_TYPE_APPLICATION_XML) > -1) {
                soapEnvelope = handleNonURLEncodedContentTypes(msgContext, request,
                        OMAbstractFactory.getSOAP12Factory());
            } else if (contentType.indexOf(HTTPConstants.MEDIA_TYPE_X_WWW_FORM) > -1 ||
                    contentType.indexOf(HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA) > -1) {
                // 2. Else, Dispatch and find out the operation and the service.
                // Dispatching can only be done using the RequestURI, as others can not be run in the REST case
                dispatchAndVerify(msgContext);

                // 3. extract the schema from the operation.
                AxisOperation axisOperation = msgContext.getAxisOperation();
                // get XML schema element here from the AxisOperation
                XmlSchemaElement xmlSchemaElement =
                        axisOperation.
                                getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE).getSchemaElement();

                soapEnvelope = SchemaUtil.handleMediaTypeURLEncoded(msgContext,
                        request,
                        xmlSchemaElement,
                        OMAbstractFactory.getSOAP12Factory());
            } else {
                throw new AxisFault(
                        "Content type should be one of /n " + HTTPConstants.MEDIA_TYPE_TEXT_XML +
                                "/n " + HTTPConstants.MEDIA_TYPE_X_WWW_FORM +
                        "/n " + HTTPConstants.MEDIA_TYPE_APPLICATION_XML +
                        "/n " + HTTPConstants.MEDIA_TYPE_MULTIPART_RELATED);
            }


            msgContext.setEnvelope(soapEnvelope);
            msgContext.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_METHOD,
                                   org.apache.axis2.transport.http.HTTPConstants.HTTP_METHOD_POST);
            msgContext.setProperty(org.apache.axis2.transport.http.HTTPConstants.CONTENT_TYPE,
                                   contentType);
            msgContext.setDoingREST(true);
            msgContext.setProperty(MessageContext.TRANSPORT_OUT, response.getOutputStream());

            invokeAxisEngine(msgContext);

        } catch (AxisFault axisFault) {
            throw axisFault;
        } catch (IOException ioException) {
            throw new AxisFault(ioException);
        }
        return true;
    }

    public boolean processGetRequest(MessageContext msgContext,
                                     HttpServletRequest request,
                                     HttpServletResponse response) throws AxisFault {
        // here, only the parameters in the URI are supported. Others will be discarded.
        try {

            // when using the wsdl2 soap response MEP it can contain soap action. We better look for it here,
            // if its there put it into msgContext so that we can use it later for dispatching purposes.

            String contentType = request.getContentType();

            if (contentType != null) {

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

            // set the required properties so that even if there is an error during the dispatch
            // phase the response message will be passed to the client well. 
            msgContext.setDoingREST(true);
            msgContext.setProperty(MessageContext.TRANSPORT_OUT, response.getOutputStream());

            // 1. First dispatchAndVerify and find out the service and the operation.
            dispatchAndVerify(msgContext);

            // 2. extract the schema from the operation and construct the SOAP message out of it.
            // 3. extract the schema from the operation.
            AxisOperation axisOperation = msgContext.getAxisOperation();

            XmlSchemaElement xmlSchemaElement = null;
            if (axisOperation != null) {
                AxisMessage axisMessage =
                        axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                xmlSchemaElement = axisMessage.getSchemaElement();
            }

            SOAPEnvelope soapEnvelope = SchemaUtil.handleMediaTypeURLEncoded(msgContext,
                    request,
                    xmlSchemaElement,
                    OMAbstractFactory.getSOAP12Factory());
            msgContext.setEnvelope(soapEnvelope);

            invokeAxisEngine(msgContext);

        } catch (AxisFault axisFault) {
            throw axisFault;
        }
        catch (IOException e) {
            throw new AxisFault(e);
        }
        return true;
    }

    private void invokeAxisEngine(MessageContext messageContext) throws AxisFault {
        AxisEngine axisEngine = new AxisEngine(configurationContext);
        axisEngine.receive(messageContext);

    }

    private void dispatchAndVerify(MessageContext msgContext) throws AxisFault {
        RequestURIBasedDispatcher requestDispatcher = new RequestURIBasedDispatcher();
        requestDispatcher.invoke(msgContext);
        AxisService axisService = msgContext.getAxisService();
        if (axisService != null) {
            RequestURIOperationDispatcher requestURIOperationDispatcher =
                    new RequestURIOperationDispatcher();
            requestURIOperationDispatcher.invoke(msgContext);

            if (msgContext.getAxisOperation() == null) {
                HTTPLocationBasedDispatcher httpLocationBasedDispatcher =
                        new HTTPLocationBasedDispatcher();
                httpLocationBasedDispatcher.invoke(msgContext);
            }

            if (msgContext.getAxisOperation() == null) {
                SOAPActionBasedDispatcher soapActionBasedDispatcher =
                        new SOAPActionBasedDispatcher();
                soapActionBasedDispatcher.invoke(msgContext);
            }
            AxisOperation axisOperation;
            if ((axisOperation = msgContext.getAxisOperation()) != null) {
                AxisEndpoint axisEndpoint =
                        (AxisEndpoint) msgContext.getProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME);
                if (axisEndpoint != null) {
                    AxisBindingOperation axisBindingOperation = (AxisBindingOperation) axisEndpoint
                            .getBinding().getChild(axisOperation.getName());
                    msgContext.setProperty(Constants.AXIS_BINDING_OPERATION, axisBindingOperation);
                }
                msgContext.setAxisOperation(axisOperation);
            }

            // check for the dispatching result
            if (msgContext.getAxisOperation() == null) {
                throw new AxisFault("I can not find a service for this request to be serviced." +
                        " Check the WSDL and the request URI");
            }
        } else {
            throw new AxisFault("I can not find a service for this request to be serviced." +
                    " Check the WSDL and the request URI");
        }
    }

    private SOAPEnvelope handleNonURLEncodedContentTypes(MessageContext msgCtxt,
                                                         HttpServletRequest request,
                                                         SOAPFactory soapFactory) throws AxisFault {
        try {

            SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
            SOAPBody body = soapEnvelope.getBody();

            InputStream inputStream = new BufferedInputStream(request.getInputStream());
            Map headers = (Map) msgCtxt.getProperty(MessageContext.TRANSPORT_HEADERS);
            if (headers != null) {
                if (HTTPConstants.COMPRESSION_GZIP.equals(headers.get(HTTPConstants.HEADER_CONTENT_ENCODING)) ||
                        HTTPConstants.COMPRESSION_GZIP.equals(headers.get(HTTPConstants.HEADER_CONTENT_ENCODING_LOWERCASE))) {
                    inputStream = new GZIPInputStream(inputStream);
                }
                }
            String contentType = request.getContentType();

            // irrespective of the schema, if the media type is text/xml, all the information
            // should be in the body.
            // I'm assuming here that the user is sending this data according to the schema.
            if (checkContentType(
                    org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_MULTIPART_RELATED,
                    contentType)) {
                body.addChild(Builder.getAttachmentsBuilder(msgCtxt,
                                                            inputStream,
                                                            contentType,
                                                            false).getDocumentElement());
            } else if (checkContentType(
                    org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_TEXT_XML,
                    contentType) ||
                    checkContentType(
                            org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_APPLICATION_XML,
                            contentType)) {

                String charSetEnc = Builder.getCharSetEncoding(contentType);
                if (charSetEnc == null) {
                    // If charset is not specified
                    charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
                }
                // Setting the value in msgCtx
                msgCtxt.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);

                // Create documentElement only if the content length is greator than 0
                if (request.getContentLength() != 0) {
                    StAXBuilder builder = Builder.getPOXBuilder(inputStream, charSetEnc, null);
                    OMNodeEx documentElement = (OMNodeEx) builder.getDocumentElement();
                    documentElement.setParent(null);
                    body.addChild(documentElement);
                }
            }

            return soapEnvelope;


        } catch (Exception e) {
            e.printStackTrace();
            throw new AxisFault("Error in creating a SOAPEnvelope from the REST request");
        }

    }

    private boolean checkContentType(String contentType, String contentTypeStringFromRequest) {
        if (contentTypeStringFromRequest == null) {
            return false;
        }
        return contentTypeStringFromRequest.indexOf(contentType) > -1;
    }

    public static String getConstantFromHTTPLocation(String httpLocation) {
        if (httpLocation.charAt(0) != '?') {
            httpLocation = "/" + httpLocation;
        }
        int index = httpLocation.indexOf("{");
        if (index > -1) {
            httpLocation = httpLocation.substring(0, index);
        }
        return httpLocation;
    }

}


