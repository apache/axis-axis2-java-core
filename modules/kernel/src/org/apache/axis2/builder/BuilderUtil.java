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

package org.apache.axis2.builder;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.attachments.lifecycle.LifecycleManager;
import org.apache.axiom.attachments.lifecycle.impl.LifecycleManagerImpl;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXParserConfiguration;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPModelBuilder;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.axis2.util.MultipleEntryHashMap;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.*;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;

public class BuilderUtil {
    private static final Log log = LogFactory.getLog(BuilderUtil.class);

    /**
     * @deprecated
     */
    public static final int BOM_SIZE = 4;

    public static SOAPEnvelope buildsoapMessage(MessageContext messageContext,
                                                MultipleEntryHashMap requestParameterMap,
                                                SOAPFactory soapFactory) throws AxisFault {

        SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
        SOAPBody body = soapEnvelope.getBody();
        XmlSchemaElement xmlSchemaElement;
        AxisOperation axisOperation = messageContext.getAxisOperation();
        if (axisOperation != null) {
            AxisMessage axisMessage =
                    axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            xmlSchemaElement = axisMessage.getSchemaElement();

            if (xmlSchemaElement == null) {
                OMElement bodyFirstChild =
                        soapFactory
                                .createOMElement(messageContext.getAxisOperation().getName(), body);

                // if there is no schema its piece of cake !! add these to the soap body in any order you like.
                // Note : if there are parameters in the path of the URL, there is no way this can add them
                // to the message.
                createSOAPMessageWithoutSchema(soapFactory, bodyFirstChild, requestParameterMap);
            } else {

                // first get the target namespace from the schema and the wrapping element.
                // create an OMElement out of those information. We are going to extract parameters from
                // url, create OMElements and add them as children to this wrapping element.
                String targetNamespace = xmlSchemaElement.getQName().getNamespaceURI();
                QName bodyFirstChildQName;
                if (targetNamespace != null && !"".equals(targetNamespace)) {
                    bodyFirstChildQName = new QName(targetNamespace, xmlSchemaElement.getName());
                } else {
                    bodyFirstChildQName = new QName(xmlSchemaElement.getName());
                }
                OMElement bodyFirstChild = soapFactory.createOMElement(bodyFirstChildQName, body);

                // Schema should adhere to the IRI style in this. So assume IRI style and dive in to
                // schema
                XmlSchemaType schemaType = xmlSchemaElement.getSchemaType();
                if (schemaType instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType complexType = ((XmlSchemaComplexType)schemaType);
                    XmlSchemaParticle particle = complexType.getParticle();
                    if (particle instanceof XmlSchemaSequence || particle instanceof XmlSchemaAll) {

                        if (particle instanceof XmlSchemaSequence) {
                            XmlSchemaSequence sequence = (XmlSchemaSequence) particle;
                            // now we need to know some information from the binding operation.

                            for (XmlSchemaSequenceMember sequenceMember : sequence.getItems()) {

                                XmlSchemaElement innerElement = (XmlSchemaElement) sequenceMember;
                                QName qName = innerElement.getQName();
                                // ignoring the elements without proper type and minoccurs zero
                                if ((innerElement.getSchemaTypeName() == null) &&
                                    (innerElement.getMinOccurs() == 0)) {
                                    continue;
                                }
                                if (qName == null && innerElement.getSchemaTypeName()
                                        .equals(org.apache.ws.commons.schema.constants.Constants.XSD_ANYTYPE)) {
                                    createSOAPMessageWithoutSchema(soapFactory, bodyFirstChild,
                                                                   requestParameterMap);
                                    break;
                                }
                                checkMinOccurs(innerElement, qName, soapFactory, requestParameterMap,
                                               bodyFirstChild);
                            }
                        } else {
                            XmlSchemaAll sequence = (XmlSchemaAll) particle;
                            // now we need to know some information from the binding operation.

                            for (XmlSchemaAllMember sequenceMember : sequence.getItems()) {

                                XmlSchemaElement innerElement = (XmlSchemaElement) sequenceMember;
                                QName qName = innerElement.getQName();
                                // ignoring the elements without proper type and minoccurs zero
                                if ((innerElement.getSchemaTypeName() == null) &&
                                    (innerElement.getMinOccurs() == 0)) {
                                    continue;
                                }
                                if (qName == null && innerElement.getSchemaTypeName()
                                        .equals(org.apache.ws.commons.schema.constants.Constants.XSD_ANYTYPE)) {
                                    createSOAPMessageWithoutSchema(soapFactory, bodyFirstChild,
                                                                   requestParameterMap);
                                    break;
                                }
                                checkMinOccurs(innerElement, qName, soapFactory, requestParameterMap,
                                               bodyFirstChild);
                            }
                        }
                    }
                }else {
                    throw new AxisFault(xmlSchemaElement.getName() + " message element in schema should follow the " +
                            "(Internationalized Resource Identifier)IRI ) style to process request in REST style");
                }
            }
        }
        return soapEnvelope;
    }

    private static void checkMinOccurs(XmlSchemaElement innerElement, QName qName,
                                       SOAPFactory soapFactory,
                                       MultipleEntryHashMap requestParameterMap,
                                       OMElement bodyFirstChild)
            throws AxisFault {
        long minOccurs = innerElement.getMinOccurs();
        boolean nillable = innerElement.isNillable();
        String name =
                qName != null ? qName.getLocalPart() : innerElement.getName();
        Object value;
        OMNamespace ns = (qName == null ||
                          qName.getNamespaceURI() == null
                          || qName.getNamespaceURI().length() == 0) ?
                         null : soapFactory.createOMNamespace(
                qName.getNamespaceURI(), null);

        // FIXME changed
        while ((value = requestParameterMap.get(name)) != null) {
            addRequestParameter(soapFactory,
                                bodyFirstChild, ns, name, value);
            minOccurs--;
        }
        if (minOccurs > 0) {
            if (nillable) {

                OMNamespace xsi = soapFactory.createOMNamespace(
                        Constants.URI_DEFAULT_SCHEMA_XSI,
                        Constants.NS_PREFIX_SCHEMA_XSI);
                OMAttribute omAttribute =
                        soapFactory.createOMAttribute("nil", xsi, "true");
                soapFactory.createOMElement(name, ns,
                                            bodyFirstChild)
                        .addAttribute(omAttribute);

            } else {
                throw new AxisFault("Required element " + qName +
                                    " defined in the schema can not be" +
                                    " found in the request");
            }
        }
    }

    public static void createSOAPMessageWithoutSchema(SOAPFactory soapFactory,
                                                       OMElement bodyFirstChild,
                                                       MultipleEntryHashMap requestParameterMap) {

        // first add the parameters in the URL
        if (requestParameterMap != null) {
            for (Object o : requestParameterMap.keySet()) {
                String key = (String) o;
                Object value;
                while ((value = requestParameterMap.get(key)) != null) {
                    addRequestParameter(soapFactory, bodyFirstChild, null, key, value);
                }
            }
        }
    }

    private static void addRequestParameter(SOAPFactory soapFactory,
                                            OMElement bodyFirstChild,
                                            OMNamespace ns,
                                            String key,
                                            Object parameter) {
        if (parameter instanceof DataHandler) {
            DataHandler dataHandler = (DataHandler)parameter;
            OMText dataText = bodyFirstChild.getOMFactory().createOMText(
                    dataHandler, true);
            soapFactory.createOMElement(key, ns, bodyFirstChild).addChild(
                    dataText);
        } else {
            String textValue = parameter.toString();
            soapFactory.createOMElement(key, ns, bodyFirstChild).setText(
                    textValue);
        }
    }

    /**
     * @deprecated Please use {@link #createPOXBuilder(InputStream, String)} to enable usage of non
     *             standard Axiom implementations.
     */
    public static StAXBuilder getPOXBuilder(InputStream inStream, String charSetEnc)
            throws XMLStreamException {
        StAXBuilder builder;
        // We use the StAXParserConfiguration.SOAP here as well because we don't want to allow
        // document type declarations (that potentially reference external entities), even
        // in plain XML messages.
        XMLStreamReader xmlreader =
                StAXUtils.createXMLStreamReader(StAXParserConfiguration.SOAP, inStream, charSetEnc);
        builder = new StAXOMBuilder(xmlreader);
        return builder;
    }

    /**
     * Create a builder suitable for an XML message. This method uses
     * {@link StAXParserConfiguration#SOAP} to disallow document type declarations (that potentially
     * reference external entities).
     * 
     * @param in
     *            the input stream containing the plain XML message
     * @param encoding
     *            the charset encoding of the message or <code>null</code> if the parser should
     *            determine the charset encoding
     * @return the builder
     */
    public static OMXMLParserWrapper createPOXBuilder(InputStream in, String encoding) {
        return OMXMLBuilderFactory.createOMBuilder(StAXParserConfiguration.SOAP, in, encoding);
    }

    /**
     * @deprecated Instead of using this method, you should probably pass the {@link InputStream}
     *             directly to the XML parser. If the stream is not XML, you shouldn't be using this
     *             method anyway.
     */
    public static Reader getReader(final InputStream is, final String charSetEncoding)
            throws IOException {
        final PushbackInputStream is2 = getPushbackInputStream(is);
        final String encoding = getCharSetEncoding(is2, charSetEncoding);
        InputStreamReader inputStreamReader;
        try {
            inputStreamReader = (InputStreamReader)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws UnsupportedEncodingException {
                            return new InputStreamReader(is2, encoding);
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            throw (UnsupportedEncodingException)e.getException();
        }
        return new BufferedReader(inputStreamReader);
    }

    /**
     * @deprecated If you need a {@link PushbackInputStream} just construct one (with the
     *             appropriate size).
     */
    public static PushbackInputStream getPushbackInputStream(InputStream is) {
        return new PushbackInputStream(is, BOM_SIZE);
    }

    /**
     * @deprecated It's the role of the XML parser to determine the charset encoding and/or byte
     *             order using the algorithm described in the "Autodetection of Character Encodings"
     *             appendix of the XML spec. If you need this method, then something is wrong:
     *             probably you are using a {@link Reader} where you should use an
     *             {@link InputStream}.
     */
    public static String getCharSetEncoding(PushbackInputStream is2, String defaultEncoding)
            throws IOException {
        String encoding;
        byte bom[] = new byte[BOM_SIZE];
        int n, unread;

        n = is2.read(bom, 0, bom.length);

        if ((bom[0] == (byte)0xEF) && (bom[1] == (byte)0xBB) && (bom[2] == (byte)0xBF)) {
            encoding = "UTF-8";
            if (log.isDebugEnabled()) {
                log.debug("char set encoding set from BOM =" + encoding);
            }
            unread = n - 3;
        } else if ((bom[0] == (byte)0xFE) && (bom[1] == (byte)0xFF)) {
            encoding = "UTF-16BE";
            if (log.isDebugEnabled()) {
                log.debug("char set encoding set from BOM =" + encoding);
            }
            unread = n - 2;
        } else if ((bom[0] == (byte)0xFF) && (bom[1] == (byte)0xFE)) {
            encoding = "UTF-16LE";
            if (log.isDebugEnabled()) {
                log.debug("char set encoding set from BOM =" + encoding);
            }
            unread = n - 2;
        } else if ((bom[0] == (byte)0x00) && (bom[1] == (byte)0x00) && (bom[2] == (byte)0xFE)
                   && (bom[3] == (byte)0xFF)) {
            encoding = "UTF-32BE";
            if (log.isDebugEnabled()) {
                log.debug("char set encoding set from BOM =" + encoding);
            }
            unread = n - 4;
        } else if ((bom[0] == (byte)0xFF) && (bom[1] == (byte)0xFE) && (bom[2] == (byte)0x00)
                   && (bom[3] == (byte)0x00)) {
            encoding = "UTF-32LE";
            if (log.isDebugEnabled()) {
                log.debug("char set encoding set from BOM =" + encoding);
            }
            unread = n - 4;
        } else {

            // Unicode BOM mark not found, unread all bytes
            encoding = defaultEncoding;
            if (log.isDebugEnabled()) {
                log.debug("char set encoding set from default =" + encoding);
            }
            unread = n;
        }

        if (unread > 0) {
            is2.unread(bom, (n - unread), unread);
        }
        return encoding;
    }


    public static String getEnvelopeNamespace(String contentType) {
        String soapNS = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        if (contentType != null) {
            if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
                // it is SOAP 1.2
                soapNS = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            } else if (contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
                // SOAP 1.1
                soapNS = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            }
        }
        return soapNS;
    }

    /**
     * Extracts and returns the character set encoding from the Content-type header
     * <p/>
     * Example: "Content-Type: text/xml; charset=utf-8" would return "utf-8"
     *
     * @param contentType a content-type (from HTTP or MIME, for instance)
     * @return the character set encoding if found, or MessageContext.DEFAULT_CHAR_SET_ENCODING
     */
    public static String getCharSetEncoding(String contentType) {
        if (log.isDebugEnabled()) {
            log.debug("Input contentType (" + contentType + ")");
        }
        if (contentType == null) {
            // Using the default UTF-8
            if (log.isDebugEnabled()) {
                log.debug("CharSetEncoding defaulted (" + MessageContext.DEFAULT_CHAR_SET_ENCODING +
                          ")");
            }
            return MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }

        int index = contentType.indexOf(HTTPConstants.CHAR_SET_ENCODING);

        if (index == -1) {    // Charset encoding not found in the content-type header
            // Using the default UTF-8
            if (log.isDebugEnabled()) {
                log.debug("CharSetEncoding defaulted (" + MessageContext.DEFAULT_CHAR_SET_ENCODING +
                          ")");
            }
            return MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }

        // If there are spaces around the '=' sign
        int indexOfEq = contentType.indexOf("=", index);

        // There can be situations where "charset" is not the last parameter of the Content-Type header
        int indexOfSemiColon = contentType.indexOf(";", indexOfEq);
        String value;

        if (indexOfSemiColon > 0) {
            value = (contentType.substring(indexOfEq + 1, indexOfSemiColon));
        } else {
            value = (contentType.substring(indexOfEq + 1, contentType.length())).trim();
        }

        // There might be "" around the value - if so remove them
        if (value.indexOf('\"') != -1) {
            value = value.replaceAll("\"", "");
        }
        value = value.trim();
        if (log.isDebugEnabled()) {
            log.debug("CharSetEncoding from content-type (" + value + ")");
        }
        return value;
    }

    public static StAXBuilder getAttachmentsBuilder(MessageContext msgContext,
                                                    InputStream inStream, String contentTypeString,
                                                    boolean isSOAP)
            throws OMException, XMLStreamException, FactoryConfigurationError {
        StAXBuilder builder = null;
        XMLStreamReader streamReader;

        Attachments attachments = createAttachmentsMap(msgContext, inStream, contentTypeString);
        String charSetEncoding = getCharSetEncoding(attachments.getRootPartContentType());

        if ((charSetEncoding == null)
            || "null".equalsIgnoreCase(charSetEncoding)) {
            charSetEncoding = MessageContext.UTF_8;
        }
        msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING,
                               charSetEncoding);

        streamReader = StAXUtils.createXMLStreamReader(attachments.getRootPartInputStream(), charSetEncoding);

        // Setting the Attachments map to new SwA API
        msgContext.setAttachmentMap(attachments);

        String soapEnvelopeNamespaceURI = getEnvelopeNamespace(contentTypeString);

        return MessageProcessorSelector.getAttachmentBuilder(msgContext, attachments, streamReader, soapEnvelopeNamespaceURI, isSOAP);

    }

    protected static Attachments createAttachmentsMap(MessageContext msgContext,
                                                      InputStream inStream,
                                                      String contentTypeString) {
        boolean fileCacheForAttachments = isAttachmentsCacheEnabled(msgContext);

        String attachmentRepoDir = null;
        String attachmentSizeThreshold = null;

        if (fileCacheForAttachments) {
            Object attachmentRepoDirProperty = msgContext
                    .getProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR);

            if (attachmentRepoDirProperty != null) {
                attachmentRepoDir = (String)attachmentRepoDirProperty;
            } else {
                Parameter attachmentRepoDirParameter = msgContext
                        .getParameter(Constants.Configuration.ATTACHMENT_TEMP_DIR);
                attachmentRepoDir =
                        (attachmentRepoDirParameter != null) ? (String)attachmentRepoDirParameter
                                .getValue()
                                : null;
            }

            Object attachmentSizeThresholdProperty = msgContext
                    .getProperty(Constants.Configuration.FILE_SIZE_THRESHOLD);
            if (attachmentSizeThresholdProperty != null
                && attachmentSizeThresholdProperty instanceof String) {
                attachmentSizeThreshold = (String)attachmentSizeThresholdProperty;
            } else {
                Parameter attachmentSizeThresholdParameter = msgContext
                        .getParameter(Constants.Configuration.FILE_SIZE_THRESHOLD);
                attachmentSizeThreshold = attachmentSizeThresholdParameter != null ?
                        attachmentSizeThresholdParameter.getValue().toString() : null;
            }
        }

        // Get the content-length if it is available
        int contentLength = 0;
        Map headers = (Map)msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
        if (headers != null) {
            String contentLengthValue = (String)headers.get(HTTPConstants.HEADER_CONTENT_LENGTH);
            if (contentLengthValue != null) {
                try {
                    contentLength = new Integer(contentLengthValue);
                } catch (NumberFormatException e) {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "Content-Length is not a valid number.  Will assume it is not set:" +
                                e);
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            if (contentLength > 0) {
                log.debug("Creating an Attachments map.  The content-length is" + contentLength);
            } else {
                log.debug("Creating an Attachments map.");
            }
        }
        return createAttachments(msgContext,
                                 inStream,
                                 contentTypeString,
                                 fileCacheForAttachments,
                                 attachmentRepoDir,
                                 attachmentSizeThreshold,
                                 contentLength);
    }

    public static boolean isAttachmentsCacheEnabled(MessageContext msgContext) {
        Object cacheAttachmentProperty = msgContext
                .getProperty(Constants.Configuration.CACHE_ATTACHMENTS);
        String cacheAttachmentString;
        boolean fileCacheForAttachments;

        if (cacheAttachmentProperty != null && cacheAttachmentProperty instanceof String) {
            cacheAttachmentString = (String)cacheAttachmentProperty;
        } else {
            Parameter parameter_cache_attachment =
                    msgContext.getParameter(Constants.Configuration.CACHE_ATTACHMENTS);
            cacheAttachmentString = (parameter_cache_attachment != null) ?
                    (String)parameter_cache_attachment.getValue() : null;
        }
        fileCacheForAttachments = (Constants.VALUE_TRUE.equals(cacheAttachmentString));
        return fileCacheForAttachments;
    }

    public static Attachments createAttachments(MessageContext msgContext,
                                                InputStream inStream,
                                                String contentTypeString,
                                                boolean fileCacheForAttachments,
                                                String attachmentRepoDir,
                                                String attachmentSizeThreshold,
                                                int contentLength) {
        LifecycleManager manager = null;
        try {
            AxisConfiguration configuration = msgContext.getRootContext().getAxisConfiguration();
            manager = (LifecycleManager)configuration
                    .getParameterValue(DeploymentConstants.ATTACHMENTS_LIFECYCLE_MANAGER);
            if (manager == null) {
                manager = new LifecycleManagerImpl();
                configuration.addParameter(DeploymentConstants.ATTACHMENTS_LIFECYCLE_MANAGER,
                                           manager);
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception getting Attachments LifecycleManager", e);
            }
        }
        return new Attachments(manager,
                               inStream,
                               contentTypeString,
                               fileCacheForAttachments,
                               attachmentRepoDir,
                               attachmentSizeThreshold,
                               contentLength);
    }

    /**
     * Utility method to get a StAXBuilder
     *
     * @param in an InputStream
     * @return a StAXSOAPModelBuilder for the given InputStream
     * @throws XMLStreamException
     * @deprecated If some one really need this method, please shout.
     */
    public static StAXBuilder getBuilder(Reader in) throws XMLStreamException {
        XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader(in);
        return new StAXSOAPModelBuilder(xmlreader, null);
    }

    /**
     * @deprecated Please use {@link OMXMLBuilderFactory#createOMBuilder(InputStream)} instead.
     */
    public static StAXBuilder getBuilder(InputStream inStream) throws XMLStreamException {
        XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(inStream);
        return new StAXOMBuilder(xmlReader);
    }

    /**
     * Create a SOAP model builder. This method delegates to
     * {@link OMXMLBuilderFactory#createSOAPModelBuilder(InputStream, String)} but generates
     * additional logging if an error occurs.
     * 
     * @param in
     *            the input stream containing the SOAP message
     * @param encoding
     *            the charset encoding of the SOAP message or <code>null</code> if the parser should
     *            determine the charset encoding
     * @return the builder
     */
    public static SOAPModelBuilder createSOAPModelBuilder(InputStream in, String encoding) {
        try {
            return OMXMLBuilderFactory.createSOAPModelBuilder(in, encoding);
        } catch (OMException e) {
            log.info("OMException in getSOAPBuilder", e);
            try {
                log.info("Remaining input stream :[" +
                         new String(IOUtils.toByteArray(in), encoding) + "]");
            } catch (IOException e1) {
                // Nothing here?
            }
            throw e;
        }
    }

    /**
     * @deprecated Please use {@link #createSOAPModelBuilder(InputStream, String)} to enable usage
     *             of non standard Axiom implementations.
     */
    public static StAXBuilder getBuilder(InputStream inStream, String charSetEnc)
            throws XMLStreamException {
        XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(inStream, charSetEnc);
        try {
            return new StAXSOAPModelBuilder(xmlReader);
        } catch (OMException e) {
            log.info("OMException in getSOAPBuilder", e);
            try {
                log.info("Remaining input stream :[" +
                         new String(IOUtils.toByteArray(inStream), charSetEnc) + "]");
            } catch (IOException e1) {
                // Nothing here?
            }
            throw e;
        }
    }

    /**
     * @deprecated Please use {@link #createSOAPModelBuilder(InputStream, String)} to enable usage
     *             of non standard Axiom implementations.
     */
    public static StAXBuilder getSOAPBuilder(InputStream inStream) throws XMLStreamException {
        XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(inStream);
        try {
            return new StAXSOAPModelBuilder(xmlReader);
        } catch (OMException e) {
            log.info("OMException in getSOAPBuilder", e);
            try {
                log.info("Remaining input stream :[" +
                         new String(IOUtils.toByteArray(inStream)) + "]");
            } catch (IOException e1) {
                // Nothing here?
            }
            throw e;
        }
    }

    /**
     * @deprecated Please use {@link #createSOAPModelBuilder(InputStream, String)} to enable usage
     *             of non standard Axiom implementations.
     */
    public static StAXBuilder getSOAPBuilder(InputStream inStream, String charSetEnc)
            throws XMLStreamException {
        XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(inStream, charSetEnc);
        try {
            return new StAXSOAPModelBuilder(xmlReader);
        } catch (OMException e) {
            log.info("OMException in getSOAPBuilder", e);
            try {
                log.info("Remaining input stream :[" +
                         new String(IOUtils.toByteArray(inStream), charSetEnc) + "]");
            } catch (IOException e1) {
                // Nothing here?
            }
            throw e;
        }
    }

    public static StAXBuilder getBuilder(SOAPFactory soapFactory, InputStream in, String charSetEnc)
            throws XMLStreamException {
        StAXBuilder builder;
        XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader(in, charSetEnc);
        builder = new StAXOMBuilder(soapFactory, xmlreader);
        return builder;
    }

    /**
     * Initial work for a builder selector which selects the builder for a given message format
     * based on the the content type of the recieved message. content-type to builder mapping can be
     * specified through the Axis2.xml.
     *
     * @param type       content-type
     * @param msgContext the active MessageContext
     * @return the builder registered against the given content-type
     * @throws AxisFault
     */
    public static Builder getBuilderFromSelector(String type, MessageContext msgContext)
            throws AxisFault {
    	boolean useFallbackBuilder = false;
        AxisConfiguration configuration =
                msgContext.getConfigurationContext().getAxisConfiguration();
        Parameter useFallbackParameter = configuration.getParameter(Constants.Configuration.USE_DEFAULT_FALLBACK_BUILDER);
        if (useFallbackParameter !=null){
        	useFallbackBuilder = JavaUtils.isTrueExplicitly(useFallbackParameter.getValue(),useFallbackBuilder);
        }
        Builder builder = configuration.getMessageBuilder(type,useFallbackBuilder);
        if (builder != null) {
            // Check whether the request has a Accept header if so use that as the response
            // message type.
            // If thats not present,
            // Setting the received content-type as the messageType to make
            // sure that we respond using the received message serialization format.

            Object contentNegotiation = configuration
                    .getParameterValue(Constants.Configuration.ENABLE_HTTP_CONTENT_NEGOTIATION);
            if (JavaUtils.isTrueExplicitly(contentNegotiation)) {
                Map transportHeaders =
                        (Map)msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
                if (transportHeaders != null) {
                    String acceptHeader = (String)transportHeaders.get(HTTPConstants.HEADER_ACCEPT);
                    if (acceptHeader != null) {
                        int index = acceptHeader.indexOf(";");
                        if (index > 0) {
                            acceptHeader = acceptHeader.substring(0, index);
                        }
                        String[] strings = acceptHeader.split(",");
                        for (String string : strings) {
                            String accept = string.trim();
                            // We dont want dynamic content negotoatin to work on text.xml as its
                            // ambiguos as to whether the user requests SOAP 1.1 or POX response
                            if (!HTTPConstants.MEDIA_TYPE_TEXT_XML.equals(accept) &&
                                configuration.getMessageFormatter(accept) != null) {
                                type = string;
                                break;
                            }
                        }
                    }
                }
            }

            msgContext.setProperty(Constants.Configuration.MESSAGE_TYPE, type);
        }
        return builder;
    }

    public static void validateSOAPVersion(String soapNamespaceURIFromTransport,
                                           SOAPEnvelope envelope) {
        if (soapNamespaceURIFromTransport != null) {
            OMNamespace envelopeNamespace = envelope.getNamespace();
            String namespaceName = envelopeNamespace.getNamespaceURI();
            if (!(soapNamespaceURIFromTransport.equals(namespaceName))) {
                throw new SOAPProcessingException(
                        "Transport level information does not match with SOAP" +
                        " Message namespace URI", envelopeNamespace.getPrefix() + ":" +
                                                  SOAPConstants.FAULT_CODE_VERSION_MISMATCH);
            }
        }
    }

    public static void validateCharSetEncoding(String charsetEncodingFromTransport,
                                               String charsetEncodingFromXML,
                                               String soapNamespaceURI) throws AxisFault {
        if ((charsetEncodingFromXML != null)
            && !"".equals(charsetEncodingFromXML)
            && (charsetEncodingFromTransport != null)
            && !charsetEncodingFromXML.equalsIgnoreCase(charsetEncodingFromTransport)
            && !compatibleEncodings(charsetEncodingFromXML, charsetEncodingFromTransport)) {
            /**
             * WS-BP Rule 1019 requires toleration if the character sets mismatch
             * I am changing this to simply a debug statement.
            String faultCode;

            if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapNamespaceURI)) {
                faultCode = SOAP12Constants.FAULT_CODE_SENDER;
            } else {
                faultCode = SOAP11Constants.FAULT_CODE_SENDER;
            }

            throw new AxisFault("Character Set Encoding from "
                    + "transport information [" + charsetEncodingFromTransport + "] does not match with "
                    + "character set encoding in the received SOAP message [" + charsetEncodingFromXML + "]", faultCode);
            **/
            if (log.isDebugEnabled()) {
                log.debug("Character Set Encoding from "
                        + "transport information [" + charsetEncodingFromTransport + "] does not match with "
                        + "character set encoding in the received SOAP message [" + charsetEncodingFromXML + "]");
            }
        }
    }

    /**
     * check if the pair is [UTF-16,UTF-16LE] [UTF-32, UTF-32LE],[UTF-16,UTF-16BE] [UTF-32,
     * UTF-32BE] etc.
     *
     * @param enc1 encoding style
     * @param enc2 encoding style
     * @return true if the encoding styles are compatible, or false otherwise
     */
    private static boolean compatibleEncodings(String enc1, String enc2) {
        enc1 = enc1.toLowerCase();
        enc2 = enc2.toLowerCase();
        if (enc1.endsWith("be") || enc1.endsWith("le")) {
            enc1 = enc1.substring(0, enc1.length() - 2);
        }
        if (enc2.endsWith("be") || enc2.endsWith("le")) {
            enc2 = enc2.substring(0, enc2.length() - 2);
        }
        return enc1.equals(enc2);
    }
}
