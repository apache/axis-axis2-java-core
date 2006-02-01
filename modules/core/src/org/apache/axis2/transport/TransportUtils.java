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


package org.apache.axis2.transport;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.ws.commons.attachments.MIMEHelper;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.i18n.Messages;
import org.apache.ws.commons.om.OMException;
import org.apache.ws.commons.om.impl.MTOMConstants;
import org.apache.ws.commons.om.impl.llom.builder.StAXBuilder;
import org.apache.ws.commons.om.impl.llom.builder.StAXOMBuilder;
import org.apache.ws.commons.om.impl.llom.mtom.MTOMStAXSOAPModelBuilder;
import org.apache.ws.commons.soap.SOAP11Constants;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.ws.commons.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class TransportUtils {
    private static final int BOM_SIZE = 4;

    public static SOAPEnvelope createSOAPMessage(MessageContext msgContext, String soapNamespaceURI)
            throws AxisFault {
        InputStream inStream = (InputStream) msgContext.getProperty(MessageContext.TRANSPORT_IN);

        msgContext.setProperty(MessageContext.TRANSPORT_IN, null);

        // this inputstram is set by the TransportSender represents a two way transport or
        // by a Transport Recevier
        if (inStream == null) {
            throw new AxisFault(Messages.getMessage("inputstreamNull"));
        }

        return createSOAPMessage(msgContext, inStream, soapNamespaceURI);
    }

    private static SOAPEnvelope createSOAPMessage(MessageContext msgContext, InputStream inStream,
                                                  String soapNamespaceURI)
            throws AxisFault {
        try {
            Object contentType = null;
            OperationContext opContext = msgContext.getOperationContext();

            if (opContext != null) {
                contentType = opContext.getProperty(HTTPConstants.MTOM_RECIVED_CONTENT_TYPE);
            } else {
                throw new AxisFault(Messages.getMessage("cannotBeNullOperationContext"));
            }

            StAXBuilder builder = null;
            SOAPEnvelope envelope = null;
            String charSetEnc =
                    (String) msgContext.getProperty(MessageContext.CHARACTER_SET_ENCODING);

            if (charSetEnc == null) {
                charSetEnc = (String) opContext.getProperty(MessageContext.CHARACTER_SET_ENCODING);
            }

            if (charSetEnc == null) {
                charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }

            if (contentType != null) {
                msgContext.setDoingMTOM(true);
                builder = selectBuilderForMIME(msgContext, inStream,
                        (String) contentType);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
            } else if (msgContext.isDoingREST()) {
                XMLStreamReader xmlreader =
                        XMLInputFactory.newInstance().createXMLStreamReader(inStream, charSetEnc);
                SOAPFactory soapFactory = new SOAP11Factory();

                builder = new StAXOMBuilder(xmlreader);
                builder.setOmbuilderFactory(soapFactory);
                envelope = soapFactory.getDefaultEnvelope();
                envelope.getBody().addChild(builder.getDocumentElement());
            } else {
                XMLStreamReader xmlreader =
                        XMLInputFactory.newInstance().createXMLStreamReader(inStream, charSetEnc);

                builder = new StAXSOAPModelBuilder(xmlreader, soapNamespaceURI);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
            }

            return envelope;
        } catch (Exception e) {
            throw new AxisFault(e);
        }
    }

    /**
     * Extracts and returns the character set encoding from the
     * Content-type header
     * Example:
     * Content-Type: text/xml; charset=utf-8
     *
     * @param contentType
     */
    public static String getCharSetEncoding(String contentType) {
        int index = contentType.indexOf(HTTPConstants.CHAR_SET_ENCODING);

        if (index == -1) {    // Charset encoding not found in the content-type header
            // Using the default UTF-8
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
        value = value.replaceAll("\"", "");

        if ("null".equalsIgnoreCase(value)) {
            return null;
        }

        return value.trim();
    }

    public static StAXBuilder selectBuilderForMIME(MessageContext msgContext, InputStream inStream,
                                                   String contentTypeString)
            throws OMException, XMLStreamException, FactoryConfigurationError,
            UnsupportedEncodingException {
        StAXBuilder builder = null;
        Parameter parameter_cache_attachment =
                msgContext.getParameter(Constants.Configuration.CACHE_ATTACHMENTS);
        boolean fileCacheForAttachments;

        if (parameter_cache_attachment == null) {
            fileCacheForAttachments = false;
        } else {
            fileCacheForAttachments =
                    (Constants.VALUE_TRUE.equals(parameter_cache_attachment.getValue()));
        }

        String attachmentRepoDir = null;
        String attachmentSizeThreshold = null;
        Parameter parameter;

        if (fileCacheForAttachments) {
            parameter =
                    msgContext.getParameter(Constants.Configuration.ATTACHMENT_TEMP_DIR);
            attachmentRepoDir = (parameter == null)
                    ? ""
                    : parameter.getValue().toString();
            parameter =
                    msgContext.getParameter(Constants.Configuration.FILE_SIZE_THRESHOLD);
            attachmentSizeThreshold = (parameter == null)
                    ? ""
                    : parameter.getValue().toString();
        }

        MIMEHelper mimeHelper = new MIMEHelper(inStream, contentTypeString,
                fileCacheForAttachments, attachmentRepoDir,
                attachmentSizeThreshold);
        String charSetEncoding =
                getCharSetEncoding(mimeHelper.getSOAPPartContentType());
        XMLStreamReader streamReader;

        if ((charSetEncoding == null) || "null".equalsIgnoreCase(charSetEncoding)) {
            charSetEncoding = MessageContext.UTF_8;
        }

        try {
            streamReader = XMLInputFactory.newInstance().createXMLStreamReader(
                    getReader(mimeHelper.getSOAPPartInputStream(), charSetEncoding));
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }

        msgContext.setProperty(MessageContext.CHARACTER_SET_ENCODING, charSetEncoding);

        /*
        * put a reference to Attachments in to the message context
        */
        msgContext.setProperty(MTOMConstants.ATTACHMENTS, mimeHelper);

        if (mimeHelper.getAttachmentSpecType().equals(MTOMConstants.MTOM_TYPE)) {

            /*
            * Creates the MTOM specific MTOMStAXSOAPModelBuilder
            */
            builder = new MTOMStAXSOAPModelBuilder(streamReader, mimeHelper, null);
        } else if (mimeHelper.getAttachmentSpecType().equals(MTOMConstants.SWA_TYPE)) {
            builder = new StAXSOAPModelBuilder(streamReader,
                    SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        }

        return builder;
    }

    /**
     * Use the BOM Mark to identify the encoding to be used. Fall back to default encoding specified
     *
     * @param is
     * @param charSetEncoding
     * @return
     * @throws java.io.IOException
     */
    private static Reader getReader(InputStream is, String charSetEncoding) throws IOException {
        PushbackInputStream is2 = new PushbackInputStream(is, BOM_SIZE);
        String encoding;
        byte bom[] = new byte[BOM_SIZE];
        int n, unread;

        n = is2.read(bom, 0, bom.length);

        if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
            encoding = "UTF-8";
            unread = n - 3;
        } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
            encoding = "UTF-16BE";
            unread = n - 2;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
            encoding = "UTF-16LE";
            unread = n - 2;
        } else if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) && (bom[2] == (byte) 0xFE)
                && (bom[3] == (byte) 0xFF)) {
            encoding = "UTF-32BE";
            unread = n - 4;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE) && (bom[2] == (byte) 0x00)
                && (bom[3] == (byte) 0x00)) {
            encoding = "UTF-32LE";
            unread = n - 4;
        } else {

            // Unicode BOM mark not found, unread all bytes
            encoding = charSetEncoding;
            unread = n;
        }

        if (unread > 0) {
            is2.unread(bom, (n - unread), unread);
        }

        return new BufferedReader(new InputStreamReader(is2, encoding));
    }
}
